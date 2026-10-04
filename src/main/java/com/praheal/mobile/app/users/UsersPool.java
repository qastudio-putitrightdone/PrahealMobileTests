package com.praheal.mobile.app.users;

import static com.praheal.mobile.utils.Secrets.require;

public final class UsersPool {

    public static final String MOBILE_NUMBER = require("patient.mobileNumber");
    public static final String PASSWORD = require("patient.password");

    private UsersPool() {
    }
}
