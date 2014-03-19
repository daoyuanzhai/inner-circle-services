package com.innercircle.services;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
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
import com.innercircle.services.model.InnerCircleResponse;
import com.innercircle.services.model.InnerCircleResponse.Status;
import com.innercircle.services.model.InnerCircleToken;
import com.mongodb.gridfs.GridFSDBFile;

@Controller
public class ServicesConsoleController {
    // private ModelMap mModel;
    @Autowired
    private DatastoreService datastoreService;
    private MessagingManager messagingManager;
    private InnerCircleResponse response;

    @Autowired
    private InnerCircleFileUpload fileUpload;

    @PostConstruct
    public void init() throws IOException {
        messagingManager = new MessagingManager();
        response = new InnerCircleResponse();
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public ModelAndView request () {
        return new ModelAndView ("index");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public @ResponseBody Object register(
            @RequestParam(Constants.EMAIL) String email,
            @RequestParam(Constants.PASSWORD) String password,
            @RequestParam(Constants.VIP_CODE) String VIPCode,
            ModelMap model) {
        resetResponse();
        email = HtmlUtils.htmlUnescape(email);
        password = HtmlUtils.htmlUnescape(password);
        VIPCode = HtmlUtils.htmlUnescape(VIPCode);

        System.out.println(Constants.EMAIL + ": " + email);
        System.out.println(Constants.PASSWORD + ": " + password);
        System.out.println(Constants.VIP_CODE + ": " + VIPCode);

        final String uid = datastoreService.addUser(email, password, VIPCode);
        if (null != uid) {
            final InnerCircleToken token = datastoreService.addOrUpdateToken(uid);
            response.setStatus(InnerCircleResponse.Status.SUCCESS);
            response.setData(token);
        } else {
            response.setStatus(InnerCircleResponse.Status.EMAIL_EXISTS_ERROR);
        }
        return response;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody Object login(
            @RequestParam(Constants.EMAIL) String email,
            @RequestParam(Constants.PASSWORD) String password,
            ModelMap model) {
        resetResponse();
        email = HtmlUtils.htmlUnescape(email);
        password = HtmlUtils.htmlUnescape(password);

        System.out.println(Constants.EMAIL + ": " + email);
        System.out.println(Constants.PASSWORD + ": " + password);

        final String uid = datastoreService.verifyEmailPassword(email, password);
        if (null != uid) {
            final InnerCircleToken token = datastoreService.addOrUpdateToken(uid);
            response.setStatus(InnerCircleResponse.Status.SUCCESS);
            response.setData(token);
        } else {
            response.setStatus(InnerCircleResponse.Status.EMAIL_PASSWORD_MISMATCH);;
        }
        return response;
    }

    @RequestMapping(value = "/refreshAccessToken", method = RequestMethod.POST)
    public @ResponseBody Object refreshAccessToken(
            @RequestParam(Constants.UID) String uid,
            @RequestParam(Constants.REFRESH_TOKEN) String refreshToken,
            ModelMap model) {
        resetResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        refreshToken = HtmlUtils.htmlUnescape(refreshToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.REFRESH_TOKEN + ": " + refreshToken);

        final InnerCircleToken token = datastoreService.updateAccessToken(uid, refreshToken);
        if (null != token) {
            response.setStatus(Status.SUCCESS);
            response.setData(token);
        } else {
            response.setStatus(Status.TOKEN_MISMATCH);
        }
        return response;
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public @ResponseBody Object sendMessage(
            @RequestParam(Constants.JSON_STRING) String jsonString,
            ModelMap model) {
        System.out.println(jsonString);
        try {
            resetResponse();
            final JSONObject registerJsonObject = new JSONObject(jsonString);

            final String uid = registerJsonObject.getString(Constants.UID);
            final String accessToken = registerJsonObject.getString(Constants.ACCESS_TOKEN);
            final String receiverUid = registerJsonObject.getString(Constants.RECEIVER_UID);
            final String message = registerJsonObject.getString(Constants.MESSAGE);
            System.out.println(Constants.UID + ": " + uid);
            System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
            System.out.println(Constants.RECEIVER_UID + ": " + receiverUid);
            System.out.println(Constants.MESSAGE + ": " + message);

            final InnerCircleResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
            response.setStatus(status);
            if (InnerCircleResponse.Status.SUCCESS == status) {
                messagingManager.sendMessage(receiverUid, message);
            }
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(InnerCircleResponse.Status.FAILED);;
            return response;
        }
    }

    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public @ResponseBody Object fileUpload(@ModelAttribute("uploadForm") InnerCircleFileUpload uploadForm, Model map) {
        resetResponse();
        System.out.println(Constants.UID + ": " + uploadForm.getUid());
        System.out.println(Constants.ACCESS_TOKEN + ": " + uploadForm.getAccessToken());
        System.out.println(Constants.IMAGE_USAGE + ": " + uploadForm.getImageUsage());

        MultipartFile file = uploadForm.getFile();
        try {
            datastoreService.saveFile(file);
            String fileName = file.getOriginalFilename();

            map.addAttribute("file", fileName);
            response.setStatus(Status.SUCCESS);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            map.addAttribute("file", e.toString());
            response.setStatus(Status.FAILED);
            return response;
        }
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
                response.setHeader("content-Disposition", "attachment; filename=" + file.getFilename());// "attachment;filename=test.xls"
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
        resetResponse();
        uid = HtmlUtils.htmlUnescape(uid);
        accessToken = HtmlUtils.htmlUnescape(accessToken);

        System.out.println(Constants.UID + ": " + uid);
        System.out.println(Constants.ACCESS_TOKEN + ": " + accessToken);
        System.out.println(Constants.GENDER + ": " + gender);

        InnerCircleResponse.Status status = datastoreService.verifyUidAccessToken(uid, accessToken);
        response.setStatus(status);
        if (Status.SUCCESS == status) {
            final InnerCircleToken token = datastoreService.updateGender(uid, gender);
            if (null != token) {
                response.setData(token);
            } else {
                response.setStatus(InnerCircleResponse.Status.FAILED);
            }
        }
        System.out.println("setGender response status: " + response.getStatus().toString());
        return response;
    }

    private void resetResponse() {
        response.setData(null);
        response.setStatus(null);
    }
}
