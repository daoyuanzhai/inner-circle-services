package com.innercircle.services.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Rider {
    // required fields
    @Id
    private String uid;
    private boolean activated;
    private String email;
    private String password;
    private String username;
    private boolean canDrive;

    // optional fields
    private char gender;
    private String major;
    private int schoolClass;

    public void setUid(final String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return this.uid;
    }

    public void setActivated(final boolean activated) {
        this.activated = activated;
    }

    public boolean getActivated() {
        return this.activated;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setCanDrive(final boolean canDrive) {
        this.canDrive = canDrive;
    }

    public boolean getCanDrive() {
        return this.canDrive;
    }

    public void setGender(final char gender) {
        this.gender = gender;
    }

    public char getGender() {
        return this.gender;
    }

    public void setMajor(final String major) {
        this.major = major;
    }

    public String getMajor() {
        return this.major;
    }

    public void setSchoolClass(final int schoolClass) {
        this.schoolClass = schoolClass;
    }

    public int getSchoolClass() {
        return this.schoolClass;
    }
}
