package com.praheal.mobile.app.screens.home;

import com.praheal.mobile.app.base.BaseSteps;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Step;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class HomeScreenSteps extends BaseSteps {

    private final HomeScreen homeScreen;

    public HomeScreenSteps(AndroidDriver androidDriver) {
        super(androidDriver);
        this.homeScreen = new HomeScreen(androidDriver);
    }

    @Step("Wait until Home screen is displayed")
    public HomeScreenSteps waitUntilHomeScreenDisplayed() {
        waitUntilVisible(homeScreen.homeTitle);

        return this;
    }

    @Step("Check that patient name is displayed in header")
    public void checkHeaderPatientNameDisplayed() {
        assertTrue(isElementDisplayed(homeScreen.headerPatientName), "Patient name is not displayed in header");
    }

    @Step("Check that patient name in header is not empty")
    public void checkHeaderPatientNameIsNotEmpty() {
        assertFalse(homeScreen.headerPatientName.getText().isBlank(), "Patient name in header is empty");
    }
}
