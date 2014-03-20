package com.innercircle.services.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class InnerCircleUser implements InnerCircleData {
    @Id
    private String id;
    private String email;
    private String password;
    private String VIPCode;
    private char gender;
    private String username;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
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

    public void setVIPCode(String VIPCode) {
        this.VIPCode = VIPCode;
    }

    public String getVIPCode() {
        return this.VIPCode;
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
