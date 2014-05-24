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
import com.innercircle.services.model.InnerCircleRelation;
import com.innercircle.services.model.InnerCircleRelationList;
import com.innercircle.services.model.CarPoolCallResponse;
import com.innercircle.services.model.RiderToken;
import com.innercircle.services.model.InnerCircleUser;
import com.innercircle.services.model.Rider;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

@Repository
public class DatastoreService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private MongoTemplate mongoTemplateChats;
    @Autowired
    private GridFsTemplate gridFsTemplateChats;

    public void saveFile(final MultipartFile file, final String filename, final int imageUsage) throws IOException {
        final InputStream inputStream = file.getInputStream();

        DBObject metaData = new BasicDBObject();
        metaData.put(Constants.ORIGINAL_FILE_NAME, file.getOriginalFilename());
        metaData.put(Constants.IMAGE_USAGE, imageUsage);

        final Criteria criteria = Criteria.where(Constants.FILE_NAME).is(filename);
        final Query query = new Query(criteria);

        GridFsTemplate gridTemp;
        switch (imageUsage) {
        case Constants.IMAGE_USAGE_FOR_SETTINGS:
            gridTemp = gridFsTemplate;
            break;
        case Constants.IMAGE_USAGE_FOR_TALKS:
            gridTemp = gridFsTemplateChats;
            break;
        default:
            throw new IOException("Invalid image usage type");
        }
        gridTemp.delete(query);
        gridTemp.store(inputStream, filename, metaData);
    }

    public GridFSDBFile readFile(String filename) {
        final Criteria criteria = Criteria.where(Constants.FILE_NAME).is(filename);
        final Query query = new Query(criteria);
        final List<GridFSDBFile> files = gridFsTemplate.find(query);
        if (files.size() < 0) {
            return null;
        }
        return files.get(0);
    }

    private boolean emailExists(final String email) {
        final Criteria criteria = Criteria.where(Constants.EMAIL).is(email);
        final Query query = new Query(criteria);
        if (mongoTemplate.exists(query, Constants.COLLECTION_NAME_RIDERS)) {
            return true;
        }
        return false;
    }

    private boolean uidExists(final String uid) {
        final Criteria criteria = Criteria.where(Constants.UID).is(uid);
        final Query query = new Query(criteria);
        if (mongoTemplate.exists(query, Constants.COLLECTION_NAME_RIDERS)) {
            return true;
        }
        return false;
    }

    public CarPoolCallResponse.Status verifyUidAccessToken(final String uid, final String accessToken) {
        createCollectionByClass(RiderToken.class);
        final Criteria criteria = Criteria.where(Constants.UID).is(uid)
                .and(Constants.ACCESS_TOKEN).is(accessToken);
                // .and(Constants.TIMESTAMP).gte(System.currentTimeMillis() - Constants.VALID_PERIOD);
        final Query query = new Query(criteria);

        final RiderToken token = (RiderToken) mongoTemplate.findOne(
                query, RiderToken.class, Constants.COLLECTION_NAME_TOKENS);
        if (null == token) {
            return CarPoolCallResponse.Status.ERROR_MISMATCH;
        } else if (token.getTimestamp() < System.currentTimeMillis() - Constants.VALID_PERIOD) {
            return CarPoolCallResponse.Status.ERROR_EXPIRED;
        } else {
            return CarPoolCallResponse.Status.SUCCESS;
        }
    }

    public String verifyEmailPassword(final String email, final String password) {
        final Criteria criteria = Criteria.where(Constants.EMAIL).is(email).and(Constants.PASSWORD).is(password);
        final Query query = new Query(criteria);

        final Rider rider = (Rider) mongoTemplate.findOne(query, Rider.class, Constants.COLLECTION_NAME_RIDERS);
        return (null == rider) ? null : rider.getUid();
    }

    public String addRider(final String email, final String password) {
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
        final Rider user = new Rider();
        user.setUid(uid);
        user.setEmail(email);
        user.setPassword(password);
        mongoTemplate.insert(user, Constants.COLLECTION_NAME_RIDERS);

        return uid;
    }

    public void setFollowingRelation(final String uid, final String theOtherUid, final boolean isFollowing) {
        createCollectionByClass(InnerCircleRelation.class);

        final Criteria criteria = Criteria.where(Constants.UID).is(uid).and(Constants.THE_OTHER_UID).is(theOtherUid);
        final Query query = new Query(criteria);

        final Update update = new Update();
        update.set(Constants.UID, uid);
        update.set(Constants.THE_OTHER_UID, theOtherUid);
        update.set(Constants.IS_FOLLOWING, isFollowing);

        mongoTemplate.upsert(query, update, InnerCircleRelation.class, Constants.COLLECTION_NAME_RELATIONS);

        // remove useless relations
        removeUselessRelationsByUID(uid);
        removeUselessRelationsByUID(theOtherUid);
    }

    public void setIsBlockedRelation(final String uid, final String theOtherUid, final boolean isBlocked) {
        createCollectionByClass(InnerCircleRelation.class);

        final Criteria criteria = Criteria.where(Constants.UID).is(uid).and(Constants.THE_OTHER_UID).is(theOtherUid);
        final Query query = new Query(criteria);

        final Update update = new Update();
        update.set(Constants.UID, uid);
        update.set(Constants.THE_OTHER_UID, theOtherUid);
        update.set(Constants.IS_BLOCKED, isBlocked);

        mongoTemplate.upsert(query, update, InnerCircleRelation.class, Constants.COLLECTION_NAME_RELATIONS);

        // remove useless relations
        removeUselessRelationsByUID(uid);
        removeUselessRelationsByUID(theOtherUid);
    }

    public InnerCircleRelationList getFollowedByUID(final String uid, final int skip, final int limit) {
        createCollectionByClass(InnerCircleRelation.class);

        final Criteria criteria = Criteria.where(Constants.UID).is(uid)
                .and(Constants.IS_FOLLOWING).is(true);
        final Query query = new Query(criteria);
        query.skip(skip);
        query.limit(limit);

        final List<InnerCircleRelation> relationsList =
                (List<InnerCircleRelation>) mongoTemplate.find(query, InnerCircleRelation.class, Constants.COLLECTION_NAME_RELATIONS);

        final List<String> followedList = new LinkedList<String>();
        for (InnerCircleRelation relation : relationsList) {
            System.out.println(relation.toString());
            if (!relation.getIsBlocked()) {
                followedList.add(relation.getTheOtherUid());
            }
        }

        final InnerCircleRelationList relationList = new InnerCircleRelationList();
        relationList.setRelationList(followedList);
        relationList.setUid(uid);

        return relationList;
    }

    public InnerCircleRelationList getBlockedByUID(final String theOtherUid, final int skip, final int limit) {
        createCollectionByClass(InnerCircleRelation.class);

        final Criteria criteria = Criteria.where(Constants.THE_OTHER_UID).is(theOtherUid)
                .and(Constants.IS_BLOCKED).is(true);
        final Query query = new Query(criteria);
        query.skip(skip);
        query.limit(limit);

        final List<InnerCircleRelation> relationsList =
                (List<InnerCircleRelation>) mongoTemplate.find(query, InnerCircleRelation.class, Constants.COLLECTION_NAME_RELATIONS);

        final List<String> blockedList = new LinkedList<String>();
        for (InnerCircleRelation relation : relationsList) {
            System.out.println(relation.toString());
            blockedList.add(relation.getUid());
        }

        final InnerCircleRelationList relationList = new InnerCircleRelationList();
        relationList.setRelationList(blockedList);
        relationList.setUid(theOtherUid);

        // remove useless relations
        removeUselessRelationsByUID(theOtherUid);

        return relationList;
    }

    public RiderToken addOrUpdateToken(final String uid) {
        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid);
        final Query query = new Query(criteria);
        RiderToken token = mongoTemplate.findAndRemove(query,RiderToken.class, Constants.COLLECTION_NAME_TOKENS);

        final String newAccessToken = Utils.tokenGeneratorByUID(uid);
        final String newRefreshToken = Utils.tokenGeneratorByUID(uid);
        final long newTimestamp = System.currentTimeMillis();

        if (null == token) {
            token = new RiderToken();
            token.setUid(uid);
        }
        token.setAccessToken(newAccessToken);
        token.setRefreshToken(newRefreshToken);
        token.setTimestamp(newTimestamp);

        mongoTemplate.insert(token, Constants.COLLECTION_NAME_TOKENS);

        return token;
    }

    public RiderToken updateAccessToken(final String uid, final String refreshToken) {
        createCollectionByClass(RiderToken.class);
        final String newAccessToken = Utils.tokenGeneratorByUID(uid);
        final long newTimestamp = System.currentTimeMillis();

        final Criteria criteria = Criteria.where(Constants.KEY_UID).is(uid)
                .and(Constants.REFRESH_TOKEN).is(refreshToken);
        final Query query = new Query(criteria);
        final Update update = new Update();
        update.set(Constants.ACCESS_TOKEN, newAccessToken);
        update.set(Constants.TIMESTAMP, newTimestamp);

        final RiderToken token = (RiderToken) mongoTemplate.findAndModify(
                query, update, RiderToken.class, Constants.COLLECTION_NAME_TOKENS);
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
                query, update, InnerCircleUser.class, Constants.COLLECTION_NAME_RIDERS);
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
                query, update, InnerCircleUser.class, Constants.COLLECTION_NAME_RIDERS);
        if (null == user) {
            return null;
        }
        // user returned is before modification
        user.setUsername(username);
        return user;
    }

    public List<InnerCircleUser> getInnerCircleUsers(final List<String> uidList) {
        createCollectionByClass(InnerCircleUser.class);

        final Criteria criteria = Criteria.where(Constants.KEY_UID).in(uidList);
        final Query query = new Query(criteria);

        final List<InnerCircleUser> users = (List<InnerCircleUser>) mongoTemplate.find(query, InnerCircleUser.class, Constants.COLLECTION_NAME_RIDERS);
        return users;
    }

    private void removeUselessRelationsByUID(final String uid) {
        final Criteria criteriaUid = Criteria.where(Constants.UID).is(uid);
        final Criteria criteriaTheOtherUid = Criteria.where(Constants.THE_OTHER_UID).is(uid);

        final Criteria criteriaBothFalse = Criteria.where(Constants.IS_FOLLOWING).is(false).and(Constants.IS_BLOCKED).is(false);
        final Criteria criteriaBlockedNonexists = Criteria.where(Constants.IS_FOLLOWING).is(false)
                .and(Constants.IS_BLOCKED).nin(true, false);
        final Criteria criteriaFollowingNonexists = Criteria.where(Constants.IS_BLOCKED).is(false)
                .and(Constants.IS_FOLLOWING).nin(true, false);

        final Criteria criteriaUser = new Criteria();
        criteriaUser.orOperator(criteriaUid, criteriaTheOtherUid);

        final Criteria criteriaCond = new Criteria();
        criteriaCond.orOperator(criteriaBothFalse, criteriaBlockedNonexists, criteriaFollowingNonexists);

        final Criteria criteria = new Criteria();
        criteria.andOperator(criteriaUser, criteriaCond);
        final Query query = new Query(criteria);
        mongoTemplate.remove(query, InnerCircleRelation.class, Constants.COLLECTION_NAME_RELATIONS);
    }

    private void createCollectionByClass(Class<?> runtimeClass) {
        if (!mongoTemplate.collectionExists(runtimeClass)) {
            mongoTemplate.createCollection(runtimeClass);
        }
    }
}
