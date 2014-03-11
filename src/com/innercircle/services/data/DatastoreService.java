package com.innercircle.services.data;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.innercircle.services.Constants;
import com.innercircle.services.Utils;
import com.innercircle.services.model.InnerCircleToken;
import com.innercircle.services.model.InnerCircleUser;

@Repository
public class DatastoreService {
    @Autowired
    private MongoTemplate mongoTemplate;

    private boolean emailExists(final String email) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.EMAIL).is(email);
        final Query query = new Query(criteria);
        if (mongoTemplate.exists(query, Constants.COLLECTION_NAME_USER)) {
            return true;
        }
        return false;
    }

    private boolean uidExists(final String uid) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.UID).is(uid);
        final Query query = new Query(criteria);
        if (mongoTemplate.exists(query, Constants.COLLECTION_NAME_USER)) {
            return true;
        }
        return false;
    }

    public boolean verifyUidAccessToken(final String uid, final String accessToken) {
        createCollectionByClass(InnerCircleToken.class);
        final Criteria criteria = Criteria.where(Constants.UID).is(uid)
                .and(Constants.ACCESS_TOKEN).is(accessToken)
                .and(Constants.TIMESTAMP).gte(System.currentTimeMillis() - Constants.VALID_PERIOD);
        final Query query = new Query(criteria);

        final InnerCircleToken token = (InnerCircleToken) mongoTemplate.findOne(
                query, InnerCircleToken.class, Constants.COLLECTION_NAME_TOKEN);
        return (null != token) ? true : false;
    }

    public String verifyEmailPassword(final String email, final String password) {
        createCollectionByClass(InnerCircleUser.class);
        final Criteria criteria = Criteria.where(Constants.EMAIL).is(email)
                .and(Constants.PASSWORD).is(password);
        final Query query = new Query(criteria);

        final InnerCircleUser user = (InnerCircleUser) mongoTemplate.findOne(
                query, InnerCircleUser.class, Constants.COLLECTION_NAME_USER);
        return user.getId();
    }

    public String addUser(final String email, final String password, final String VIPCode) {
        if (emailExists(email)) {
            return null;
        }
        String uid;
        while (true) {
            uid = UUID.randomUUID().toString();
            if (!uidExists(uid)) {
                break;
            }
        }
        final InnerCircleUser user = new InnerCircleUser();
        user.setId(uid);
        user.setEmail(email);
        user.setPassword(password);
        user.setVIPCode(VIPCode);
        mongoTemplate.insert(user, Constants.COLLECTION_NAME_USER);
        return uid;
    }

    public InnerCircleToken addOrUpdateToken(final String uid) {
        createCollectionByClass(InnerCircleToken.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);
        mongoTemplate.remove(query, Constants.COLLECTION_NAME_TOKEN);

        final InnerCircleToken token = new InnerCircleToken();
        token.setUid(uid);
        token.setAccessToken(Utils.tokenGeneratorByUID(uid));
        token.setRefreshToken(Utils.tokenGeneratorByUID(uid));
        token.setTimestamp(System.currentTimeMillis());
        mongoTemplate.insert(token, Constants.COLLECTION_NAME_TOKEN);

        return token;
    }

    public InnerCircleToken updateAccessToken(final String uid, final String refreshToken) {
        createCollectionByClass(InnerCircleToken.class);
        final String newAccessToken = Utils.tokenGeneratorByUID(uid);
        final long newTimestamp = System.currentTimeMillis();

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid)
                .and(Constants.REFRESH_TOKEN).is(refreshToken);
        final Query query = new Query(criteria);
        final Update update = new Update();
        update.set(Constants.ACCESS_TOKEN, newAccessToken);
        update.set(Constants.TIMESTAMP, newTimestamp);

        final InnerCircleToken token = (InnerCircleToken) mongoTemplate.findAndModify(
                query, update, InnerCircleToken.class, Constants.COLLECTION_NAME_TOKEN);
        if (null == token) {
            return null;
        }
        token.setAccessToken(newAccessToken);
        token.setTimestamp(newTimestamp);
        // avoid sending refreshToken again and again
        token.setRefreshToken(null);
        return token;
    }

    private void createCollectionByClass(Class<?> runtimeClass) {
        if (!mongoTemplate.collectionExists(runtimeClass)) {
            mongoTemplate.createCollection(runtimeClass);
        }
    }
}
