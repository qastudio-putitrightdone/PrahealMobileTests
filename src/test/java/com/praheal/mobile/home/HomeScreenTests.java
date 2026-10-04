package com.praheal.mobile.home;

import com.praheal.mobile.app.screens.home.HomeScreenSteps;
import com.praheal.mobile.base.BaseMobileTest;
import com.praheal.mobile.listeners.PrahealLabels;
import io.qameta.allure.*;
import org.testng.annotations.Test;

public class HomeScreenTests extends BaseMobileTest {

    private HomeScreenSteps homeScreenSteps;

    @Test
    @Epic("Home Screen")
    @Story("Screen Elements Verification")
    @Description("Verify Home screen header displays patient name")
    @PrahealLabels.TestID("HAT-T17")
    @Severity(SeverityLevel.CRITICAL)
    public void verifyHeaderPatientNameIsDisplayed() {
        homeScreenSteps
                .waitUntilHomeScreenDisplayed()
                .checkHeaderPatientNameDisplayed();
    }

    @Test
    @Epic("Home Screen")
    @Story("Screen Elements Verification")
    @Description("Verify Home screen header patient name is not empty")
    @PrahealLabels.TestID("HAT-T18")
    public void verifyHeaderPatientNameIsNotEmpty() {
        homeScreenSteps
                .waitUntilHomeScreenDisplayed()
                .checkHeaderPatientNameIsNotEmpty();
    }
}
