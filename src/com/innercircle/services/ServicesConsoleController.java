package com.innercircle.services;

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
import com.innercircle.services.model.InnerCircleError;
import com.innercircle.services.model.InnerCircleToken;

@Controller
public class ServicesConsoleController {
    // private ModelMap mModel;
    @Autowired
    private DatastoreService datastoreService;

    @PostConstruct
    public void init() {
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
}
