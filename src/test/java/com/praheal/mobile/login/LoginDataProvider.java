package com.praheal.mobile.login;

import com.praheal.mobile.app.users.PrahealPatient;
import org.testng.annotations.DataProvider;

import static com.praheal.mobile.app.users.UsersPool.MOBILE_NUMBER;
import static com.praheal.mobile.app.users.UsersPool.PASSWORD;
import static com.praheal.mobile.utils.RandomTestData.randomPassword;
import static com.praheal.mobile.utils.RandomTestData.randomUnregisteredMobileNumber;

public class LoginDataProvider {

    @DataProvider(name = "userData")
    public Object[][] getLoginData() {
        return new Object[][] {{new PrahealPatient(MOBILE_NUMBER, PASSWORD)}};
    }

    @DataProvider(name = "invalidUserData")
    public Object[][] getInvalidLoginData() {
        return new Object[][] {{new PrahealPatient(randomUnregisteredMobileNumber(), randomPassword())}};
    }
}
