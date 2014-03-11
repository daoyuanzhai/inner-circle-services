package com.innercircle.services;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.innercircle.services.data.DatastoreService;
import com.innercircle.services.messaging.MessagingManager;
import com.innercircle.services.model.InnerCircleError;
import com.innercircle.services.model.InnerCircleToken;

@Controller
public class ServicesConsoleController {
    // private ModelMap mModel;
    @Autowired
    private DatastoreService datastoreService;
    private MessagingManager messagingManager;

    @PostConstruct
    public void init() throws IOException {
    	messagingManager = MessagingManager.getInstance();
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView request () {
        return new ModelAndView ("index");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody Object register(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
        try {
            final JSONObject registerJsonObject = new JSONObject(jsonString);

            final String email = registerJsonObject.getString(Constants.EMAIL);
            final String password = registerJsonObject.getString(Constants.PASSWORD);
            final String VIPCode = registerJsonObject.getString(Constants.VIP_CODE);
            System.out.println(Constants.EMAIL + ": " + email);
            System.out.println(Constants.PASSWORD + ": " + password);
            System.out.println(Constants.VIP_CODE + ": " + VIPCode);

            final String uid = datastoreService.addUser(email, password, VIPCode);
            if (null != uid) {
                final InnerCircleToken token = datastoreService.addOrUpdateToken(uid);
                return token;
            } else {
                final InnerCircleError error = new InnerCircleError();
                error.setError("email already exists");
                return error;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody Object login(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
        try {
            final JSONObject registerJsonObject = new JSONObject(jsonString);

            final String email = registerJsonObject.getString(Constants.EMAIL);
            final String password = registerJsonObject.getString(Constants.PASSWORD);
            System.out.println(Constants.EMAIL + ": " + email);
            System.out.println(Constants.PASSWORD + ": " + password);

            final String uid = datastoreService.verifyEmailPassword(email, password);
            if (null != uid) {
                final InnerCircleToken token = datastoreService.addOrUpdateToken(uid);
                return token;
            } else {
                final InnerCircleError error = new InnerCircleError();
                error.setError("email and password don't match");
                return error;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/refreshAccessToken", method = RequestMethod.POST)
    public @ResponseBody Object refreshAccessToken(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
        try {
            final JSONObject registerJsonObject = new JSONObject(jsonString);

            final String uid = registerJsonObject.getString(Constants.UID);
            final String refreshToken = registerJsonObject.getString(Constants.REFRESH_TOKEN);
            System.out.println(Constants.UID + ": " + uid);
            System.out.println(Constants.REFRESH_TOKEN + ": " + refreshToken);

            final InnerCircleToken token = datastoreService.updateAccessToken(uid, refreshToken);
            if (null != token) {
                return token;
            } else {
                final InnerCircleError error = new InnerCircleError();
                error.setError("uid and refreshToken don't match");
                return error;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public @ResponseBody Object sendMessage(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
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

            if (datastoreService.verifyUidAccessToken(uid, accessToken)) {
                System.out.println("verified");
                messagingManager.sendLog(message);
                return null;
            } else {
                final InnerCircleError error = new InnerCircleError();
                error.setError("accessToken has expired");
                return error;
            }
        } catch (IOException e) {
        	final InnerCircleError error = new InnerCircleError();
            error.setError("error sending message");
            return error;
        } catch (JSONException e) {
            e.printStackTrace();
            final InnerCircleError error = new InnerCircleError();
            error.setError("error parsing json parameters");
            return error;
        }
    }
}
