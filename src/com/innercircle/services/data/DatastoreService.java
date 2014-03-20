package com.innercircle.services.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.innercircle.services.Constants;
import com.innercircle.services.Utils;
import com.innercircle.services.model.InnerCircleResponse;
import com.innercircle.services.model.InnerCircleTestInner;
import com.innercircle.services.model.InnerCircleTestOuter;
import com.innercircle.services.model.InnerCircleToken;
import com.innercircle.services.model.InnerCircleUser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

@Repository
public class DatastoreService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    public void saveFile(MultipartFile file) throws IOException {
        final InputStream inputStream = file.getInputStream();
        final String fileName = file.getOriginalFilename();

        DBObject metaData = new BasicDBObject();
        metaData.put("extra1", "anything 1");
        metaData.put("extra2", "anything 2");

        gridFsTemplate.store(inputStream, fileName, metaData);
    }

    public GridFSDBFile readFile(String filename) {
        final Criteria criteria = Criteria.where(Constants.FILE_NAME).is(filename);
        final Query query = new Query(criteria);
        final List<GridFSDBFile> files = gridFsTemplate.find(query);
        return files.get(0);
    }

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

    public InnerCircleResponse.Status verifyUidAccessToken(final String uid, final String accessToken) {
        createCollectionByClass(InnerCircleToken.class);
        final Criteria criteria = Criteria.where(Constants.UID).is(uid)
                .and(Constants.ACCESS_TOKEN).is(accessToken);
                // .and(Constants.TIMESTAMP).gte(System.currentTimeMillis() - Constants.VALID_PERIOD);
        final Query query = new Query(criteria);

        final InnerCircleToken token = (InnerCircleToken) mongoTemplate.findOne(
                query, InnerCircleToken.class, Constants.COLLECTION_NAME_TOKEN);
        if (null == token) {
            return InnerCircleResponse.Status.TOKEN_MISMATCH;
        } else if (token.getTimestamp() < System.currentTimeMillis() - Constants.VALID_PERIOD) {
            return InnerCircleResponse.Status.TOKEN_EXPIRE_ERROR;
        } else {
            return InnerCircleResponse.Status.SUCCESS;
        }
    }

    public String verifyEmailPassword(final String email, final String password) {
        createCollectionByClass(InnerCircleUser.class);
        final Criteria criteria = Criteria.where(Constants.EMAIL).is(email)
                .and(Constants.PASSWORD).is(password);
        final Query query = new Query(criteria);

        final InnerCircleUser user = (InnerCircleUser) mongoTemplate.findOne(
                query, InnerCircleUser.class, Constants.COLLECTION_NAME_USER);
        return (null == user) ? null : user.getId();
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

    public void addTestObject() {
        final List<InnerCircleTestInner> innerList = new LinkedList<InnerCircleTestInner>();
        for (int i = 0; i < 10; i++) {
            InnerCircleTestInner inner = new InnerCircleTestInner();
            inner.setField3(UUID.randomUUID().toString());
            inner.setField4(UUID.randomUUID().toString());
            innerList.add(inner);
        }
        final InnerCircleTestOuter outter = new InnerCircleTestOuter();
        outter.setField1(UUID.randomUUID().toString());
        outter.setField2(UUID.randomUUID().toString());
        outter.setInnerList(innerList);
        mongoTemplate.insert(outter, "test_collection");
    }

    public InnerCircleToken addOrUpdateToken(final String uid) {
        createCollectionByClass(InnerCircleToken.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);
        InnerCircleToken token = mongoTemplate.findAndRemove(query,InnerCircleToken.class, Constants.COLLECTION_NAME_TOKEN);

        final String newAccessToken = Utils.tokenGeneratorByUID(uid);
        final String newRefreshToken = Utils.tokenGeneratorByUID(uid);
        final long newTimestamp = System.currentTimeMillis();

        if (null == token) {
            token = new InnerCircleToken();
            token.setUid(uid);
        }
        token.setAccessToken(newAccessToken);
        token.setRefreshToken(newRefreshToken);
        token.setTimestamp(newTimestamp);

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

    public InnerCircleUser updateGender(final String uid, final char gender) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);
        final Update update = new Update();
        update.set(Constants.GENDER, gender);

        final InnerCircleUser user = (InnerCircleUser) mongoTemplate.findAndModify(
                query, update, InnerCircleUser.class, Constants.COLLECTION_NAME_USER);
        if (null == user) {
            return null;
        }
        // user returned is before modification
        user.setGender(gender);
        return user;
    }

    public InnerCircleUser updateUsername(final String uid, final String username) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);
        final Update update = new Update();
        update.set(Constants.USERNAME, username);

        final InnerCircleUser user = (InnerCircleUser) mongoTemplate.findAndModify(
                query, update, InnerCircleUser.class, Constants.COLLECTION_NAME_USER);
        if (null == user) {
            return null;
        }
        // user returned is before modification
        user.setUsername(username);
        return user;
    }

    public InnerCircleUser getInnerCircleUser(final String uid) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);

        final InnerCircleUser user = (InnerCircleUser) mongoTemplate.findOne(query, InnerCircleUser.class, Constants.COLLECTION_NAME_USER);
        return user;
    }

    private void createCollectionByClass(Class<?> runtimeClass) {
        if (!mongoTemplate.collectionExists(runtimeClass)) {
            mongoTemplate.createCollection(runtimeClass);
        }
    }
}
