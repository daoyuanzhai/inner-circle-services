package com.innercircle.services;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import com.innercircle.services.data.DatastoreService;
import com.innercircle.services.messaging.MessagingManager;
import com.innercircle.services.model.InnerCircleFileUpload;
import com.innercircle.services.model.InnerCircleRelationList;
import com.innercircle.services.model.CarPoolCallResponse;
import com.innercircle.services.model.CarPoolCallResponse.Status;
import com.innercircle.services.model.RiderToken;
import com.innercircle.services.model.InnerCircleUser;
import com.innercircle.services.model.InnerCircleUserList;
import com.mongodb.gridfs.GridFSDBFile;

@Controller
public class ServicesConsoleController {
    // private ModelMap mModel;
    @Autowired
    private DatastoreService datastoreService;
    private MessagingManager messagingManager;

    @Autowired
    private InnerCircleFileUpload fileUpload;

    @PostConstruct
    public void init() throws IOException {
        messagingManager = new MessagingManager();
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView request () {
        return new ModelAndView ("index");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody Object register(
            @RequestParam(Constants.EMAIL) String email,
            @RequestParam(Constants.PASSWORD) String password,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();

        email = HtmlUtils.htmlUnescape(email);
        password = HtmlUtils.htmlUnescape(password);

        System.out.println(Constants.EMAIL + ": " + email);
        System.out.println(Constants.PASSWORD + ": " + password);

        final String uid = datastoreService.addRider(email, password);
        if (null != uid) {
            response.setStatus(CarPoolCallResponse.Status.SUCCESS);
        } else {
            response.setStatus(CarPoolCallResponse.Status.ERROR_IN_USE);
        }
        return response;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody Object login(
            @RequestParam(Constants.EMAIL) String email,
            @RequestParam(Constants.PASSWORD) String password,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        email = HtmlUtils.htmlUnescape(email);
        password = HtmlUtils.htmlUnescape(password);

        System.out.println(Constants.EMAIL + ": " + email);
        System.out.println(Constants.PASSWORD + ": " + password);

        final String uid = datastoreService.verifyEmailPassword(email, password);
        if (null != uid) {
            System.out.println("email & password verification successful, retrieving token now...");
            final RiderToken token = datastoreService.addOrUpdateToken(uid);
            response.setStatus(CarPoolCallResponse.Status.SUCCESS);
            response.setData(token);
        } else {
            System.out.println("email & password mismatch...");
            response.setStatus(CarPoolCallResponse.Status.ERROR_MISMATCH);;
        }
        return response;
    }

    /*
    @RequestMapping(value = "/getCounter", method = RequestMethod.POST)
    public @ResponseBody Object getCounter(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.RECEIVER_UID) String receiverUid,
            ModelMap model) {
        final InnerCircleResponse response = new InnerCircleResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);
        receiverUid = HtmlUtils.htmlUnescape(receiverUid);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.RECEIVER_UID + ": " + receiverUid);

        InnerCircleResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            try {
                InnerCircleCounter counter = datastoreService.getCounterByUID(uid, receiverUid);
                if (null != counter && counter.getCount() > -1) {
                    response.setData(counter);
                } else {
                    response.setStatus(InnerCircleResponse.Status.FAILED);
                }
            } catch (Exception e) {
                System.out.println(e.toString());
                response.setStatus(InnerCircleResponse.Status.FAILED);
            }
        }
        System.out.println("getCounter response status: " + response.getStatus().toString());
        return response;
    }
    */

    @RequestMapping(value = "/refreshAccessToken", method = RequestMethod.POST)
    public @ResponseBody Object refreshAccessToken(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.REFRESH_TOKEN) String refreshToken,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        refreshToken = HtmlUtils.htmlUnescape(refreshToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.REFRESH_TOKEN + ": " + refreshToken);

        final RiderToken token = datastoreService.updateAccessToken(uid, refreshToken);
        if (null != token) {
            response.setStatus(Status.SUCCESS);
            response.setData(token);
        } else {
            response.setStatus(Status.ERROR_MISMATCH);
        }
        return response;
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public @ResponseBody Object sendMessage(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        System.out.println(jsonString);
        try {
            final JSONObject registerJsonObject = new JSONObject(jsonString);

            final String uid = registerJsonObject.getString(Constants.UID);
            final String accessToken = registerJsonObject.getString(Constants.ACCESS_TOKEN);
            final String receiverUid = registerJsonObject.getString(Constants.RECEIVER_UID);
            final String message = registerJsonObject.getString(Constants.MESSAGE);
            System.out.println(Constants.UID + ": " + uid);
            System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
            System.out.println(Constants.RECEIVER_UID + ": " + receiverUid);
            System.out.println(Constants.MESSAGE + ": " + message);

            final CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
            response.setStatus(status);
            if (CarPoolCallResponse.Status.SUCCESS == status) {
                messagingManager.sendMessage(receiverUid, message);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(CarPoolCallResponse.Status.FAILED);;
            return response;
        }
    }

    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public @ResponseBody Object fileUpload(@ModelAttribute("uploadForm") InnerCircleFileUpload uploadForm, Model map) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        final String uid = HtmlUtils.htmlUnescape(uploadForm.getUid());
        final String accessToken = HtmlUtils.htmlUnescape(uploadForm.getAccessToken());
        final int imageUsage = Integer.valueOf(HtmlUtils.htmlUnescape(uploadForm.getImageUsage()));
        final String filename = HtmlUtils.htmlUnescape(uploadForm.getFilename());

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.IMAGE_USAGE + ": " + imageUsage);
        System.out.println(Constants.FILE_NAME + ": " + filename);

        final CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        response.setStatus(status);
        if (status == CarPoolCallResponse.Status.SUCCESS) {
            MultipartFile file = uploadForm.getFile();
            try {
                datastoreService.saveFile(file, filename, imageUsage);
                response.setStatus(Status.SUCCESS);
                return response;
            } catch (IOException e) {
                e.printStackTrace();
                response.setStatus(Status.FAILED);
                return response;
            }
        }
        System.out.println("fileUpload response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/fileDownload", method = RequestMethod.POST)
    public void fileDownload(@RequestParam("fileDownloadUid") String uid,
            @RequestParam("fileDownloadAccessToken") String accessToken,
            @RequestParam("fileDownloadFilename") String filename,
            HttpServletResponse response) throws IOException {
        final String uidUnescaped = HtmlUtils.htmlUnescape(uid);
        final String accessTokenUnescaped = HtmlUtils.htmlUnescape(accessToken);
        final String filenameUnescaped = HtmlUtils.htmlUnescape(filename);
        System.out.println(Constants.UID + ": " + uidUnescaped);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessTokenUnescaped);
        System.out.println(Constants.FILE_NAME + ": " + filenameUnescaped);

        final GridFSDBFile file = datastoreService.readFile(filenameUnescaped);
        if (file != null) {
            try {
                response.setContentType(file.getContentType());
                response.setContentLength((new Long(file.getLength()).intValue()));
                response.setHeader("content-Disposition", "attachment; filename=" + file.getFilename());
                // copy it to response's OutputStream
                IOUtils.copyLarge(file.getInputStream(), response.getOutputStream());
            } catch (IOException ex) {
                throw new RuntimeException("IOError writing file to output stream");
            }
        }
    }

    @RequestMapping(value = "/setGender", method = RequestMethod.POST)
    public @ResponseBody Object setGender(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.GENDER) char gender,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.GENDER + ": " + gender);

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            final InnerCircleUser user = datastoreService.updateGender(uid, gender);
            if (null != user) {
                response.setData(user);
            } else {
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("setGender response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/setUsername", method = RequestMethod.POST)
    public @ResponseBody Object setUsername(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.USERNAME) String username,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.USERNAME + ": " + username);

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            final InnerCircleUser user = datastoreService.updateUsername(uid, username);
            if (null != user) {
                response.setData(user);
            } else {
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("setUsername response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/getUserAccounts", method = RequestMethod.POST)
    public @ResponseBody Object getUserAccounts(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.OTHER_UIDS) String otherUids,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);
        otherUids = HtmlUtils.htmlUnescape(otherUids);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.OTHER_UIDS + ": " + otherUids);

        final List<String> uidList = new LinkedList<String>();
        try{
            final JSONArray uidArray = new JSONArray(otherUids);
            for (int i = 0; i < uidArray.length(); i++) {
                uidList.add(uidArray.getString(i));
            }
        } catch (JSONException e) {
            response.setStatus(CarPoolCallResponse.Status.FAILED);
            return response;
        }
        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        System.out.println("uid and accessToken verification result: " + status.toString());
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            final List<InnerCircleUser> users = datastoreService.getInnerCircleUsers(uidList);
            if (null != users) {
                final InnerCircleUserList userList = new InnerCircleUserList();
                userList.setUid(uid);
                userList.setUserList(users);
                response.setData(userList);
            } else {
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("getUserAccounts response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/setFollowing", method = RequestMethod.POST)
    public @ResponseBody Object setFollowing(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.THE_OTHER_UID) String theOtherUid,
            @RequestParam(Constants.IS_FOLLOWING) String isFollowingString,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);
        theOtherUid = HtmlUtils.htmlUnescape(theOtherUid);
        final boolean isFollowing = Boolean.parseBoolean(HtmlUtils.htmlUnescape(isFollowingString));

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.THE_OTHER_UID + ": " + theOtherUid);
        System.out.println(Constants.IS_FOLLOWING + ": " + String.valueOf(isFollowing));

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        System.out.println("uid and accessToken verification result: " + status.toString());
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            try {
                datastoreService.setFollowingRelation(uid, theOtherUid, isFollowing);
            } catch (Exception e) {
                System.out.println(e.toString());
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("setFollowing response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/getFollowed", method = RequestMethod.POST)
    public @ResponseBody Object getFollowed(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.SKIP) int skip,
            @RequestParam(Constants.LIMIT) int limit,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.SKIP + ": " + String.valueOf(skip));
        System.out.println(Constants.LIMIT + ": " + String.valueOf(limit));

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        System.out.println("uid and accessToken verification result: " + status.toString());
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            try {
                final InnerCircleRelationList followedList = datastoreService.getFollowedByUID(uid, skip, limit);
                response.setData(followedList);
            } catch (Exception e) {
                System.out.println(e.toString());
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("getFollowed response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/setIsBlocked", method = RequestMethod.POST)
    public @ResponseBody Object setIsBlocked(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.THE_OTHER_UID) String theOtherUid,
            @RequestParam(Constants.IS_BLOCKED) String isBlockedString,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);
        theOtherUid = HtmlUtils.htmlUnescape(theOtherUid);
        final boolean isBlocked = Boolean.parseBoolean(HtmlUtils.htmlUnescape(isBlockedString));

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.THE_OTHER_UID + ": " + theOtherUid);
        System.out.println(Constants.IS_FOLLOWING + ": " + String.valueOf(isBlocked));

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        System.out.println("uid and accessToken verification result: " + status.toString());
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            try {
                datastoreService.setIsBlockedRelation(uid, theOtherUid, isBlocked);
            } catch (Exception e) {
                System.out.println(e.toString());
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("setIsBlocked response status: " + response.getStatus().toString());
        return response;
    }

    @RequestMapping(value = "/getBlocked", method = RequestMethod.POST)
    public @ResponseBody Object getBlocked(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.ACCESS_TOKEN) String accessToken,
            @RequestParam(Constants.SKIP) int skip,
            @RequestParam(Constants.LIMIT) int limit,
            ModelMap model) {
        final CarPoolCallResponse response = new CarPoolCallResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.SKIP + ": " + String.valueOf(skip));
        System.out.println(Constants.LIMIT + ": " + String.valueOf(limit));

        CarPoolCallResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        System.out.println("uid and accessToken verification result: " + status.toString());
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            try {
                final InnerCircleRelationList blockedList = datastoreService.getBlockedByUID(uid, skip, limit);
                response.setData(blockedList);
            } catch (Exception e) {
                System.out.println(e.toString());
                response.setStatus(CarPoolCallResponse.Status.FAILED);
            }
        }
        System.out.println("getFollowed response status: " + response.getStatus().toString());
        return response;
    }
}
