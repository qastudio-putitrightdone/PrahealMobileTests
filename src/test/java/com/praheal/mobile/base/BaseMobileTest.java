package com.praheal.mobile.base;

import com.praheal.mobile.capabilities.DeviceCapabilities;
import com.praheal.mobile.listeners.PrahealListener;
import io.appium.java_client.android.AndroidDriver;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Objects;

import static com.praheal.mobile.capabilities.DeviceList.getFirstAvailableDevice;
import static com.praheal.mobile.capabilities.InstalledChecks.isAppInstalled;
import static com.praheal.mobile.utils.GetConfigurations.getAppPath;
import static com.praheal.mobile.utils.GetConfigurations.getPackageName;

@Listeners(PrahealListener.class)
public class BaseMobileTest {

    private StepInjector stepInjector;

    private static AndroidDriver androidDriver;
    private final DeviceCapabilities deviceCapabilities = new DeviceCapabilities();

    @BeforeSuite(alwaysRun = true)
    public void setUpApp() {
        String firstDeviceId = getFirstAvailableDevice();
        if (!isAppInstalled(firstDeviceId, getPackageName())) {
            androidDriver = deviceCapabilities.setAutomatorCapabilities(getAppPath(), firstDeviceId);
        } else {
            androidDriver = deviceCapabilities.setAutomatorCapabilities(firstDeviceId);
        }
        androidDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
    }

    @BeforeMethod(alwaysRun = true)
    public void setUpTest() {
        androidDriver.activateApp(getPackageName());
        stepInjector = new StepInjector(androidDriver);
    }

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "setUpTest")
    public void injectStepsFields() throws IllegalAccessException {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.getType().getSimpleName().endsWith("Steps")) {
                field.setAccessible(true);
                field.set(this, stepInjector.getStep(field.getType()));
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult iTestResult) {
        if (iTestResult.getStatus() == ITestResult.SUCCESS) {
            Allure.step("Test Passed: " + iTestResult.getName(), Status.PASSED);
            takeScreenshot();
        } else if (iTestResult.getStatus() == ITestResult.FAILURE) {
            Allure.step("Test Failed: " + iTestResult.getName(), Status.FAILED);
            takeScreenshot();
        } else if (iTestResult.getStatus() == ITestResult.SKIP) {
            Allure.step("Test Skipped: " + iTestResult.getName(), Status.SKIPPED);
        }

        androidDriver.terminateApp(getPackageName());
        if (Objects.nonNull(stepInjector)) {
            stepInjector = null;
        }
    }

    @AfterSuite(alwaysRun = true)
    public void tearDownApp() {
        if (Objects.nonNull(androidDriver)) {
            androidDriver.quit();
            androidDriver = null;
        }
    }

    private void takeScreenshot() {
        byte[] screenshotBytes = ((TakesScreenshot) androidDriver).getScreenshotAs(OutputType.BYTES);

        Allure.addAttachment(
                String.valueOf(System.currentTimeMillis()),
                "image/png",
                new ByteArrayInputStream(screenshotBytes),
                "png"
        );
    }
}
