package com.innercircle.services.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleUser implements InnerCircleData {
    @Id
    private String uid;
    private String email;
    private String password;
    private String vipCode;
    private char gender;
    private String username;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setVipCode(String vipCode) {
        this.vipCode = vipCode;
    }

    public String getVipCode() {
        return this.vipCode;
    }

    public void setGender(final char gender) {
        this.gender = gender;
    }

    public char getGender() {
        return this.gender;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
