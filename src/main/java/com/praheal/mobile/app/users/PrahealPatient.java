package com.praheal.mobile.app.users;

public class PrahealPatient {

    private final String mobileNumber;
    private final String password;

    public PrahealPatient(String mobileNumber, String password) {
        this.mobileNumber = mobileNumber;
        this.password = password;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public String getPassword() {
        return this.password;
    }

    @Override
    public String toString() {
        return "PrahealPatient{mobileNumber=" + mobileNumber + "}";
    }
}
