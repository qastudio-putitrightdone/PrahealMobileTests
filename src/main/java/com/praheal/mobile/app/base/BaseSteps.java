package com.praheal.mobile.app.base;

import com.praheal.mobile.utils.GetConfigurations;
import com.praheal.mobile.utils.Screenshots;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.praheal.mobile.app.constants.LayoutConstants.BACKGROUND_DURATION_SECONDS;
import static com.praheal.mobile.app.constants.LayoutConstants.LAYOUT_TOLERANCE_PX;

public abstract class BaseSteps {

    protected final AndroidDriver androidDriver;
    private static final int ELEMENT_VISIBILITY_TIMEOUT = 30;

    protected BaseSteps(AndroidDriver androidDriver) {
        this.androidDriver = androidDriver;
    }

    protected WebElement waitUntilVisible(WebElement webElement, int timeoutSeconds) {
        return new WebDriverWait(androidDriver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOf(webElement));
    }

    protected WebElement waitUntilVisible(WebElement webElement) {
        return waitUntilVisible(webElement, ELEMENT_VISIBILITY_TIMEOUT);
    }

    protected boolean isElementDisplayed(WebElement webElement) {
        try {
            return webElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected <S extends BaseScreen> Map<String, Rectangle> captureRects(S screen, ScreenElement<S>[] elements) {
        Map<String, Rectangle> rects = new LinkedHashMap<>();
        for (ScreenElement<S> element : elements) {
            rects.put(element.label(), element.of(screen).getRect());
        }
        return rects;
    }

    protected List<String> layoutDifferences(Map<String, Rectangle> expected, Map<String, Rectangle> actual) {
        List<String> differences = new ArrayList<>();
        expected.forEach((name, before) -> {
            Rectangle after = actual.get(name);
            if (after == null || !isSameRect(before, after)) {
                differences.add(name + ": " + describe(before) + " -> " + describe(after));
            }
        });
        return differences;
    }

    private static boolean isSameRect(Rectangle before, Rectangle after) {
        return Math.abs(before.getX() - after.getX()) <= LAYOUT_TOLERANCE_PX
                && Math.abs(before.getY() - after.getY()) <= LAYOUT_TOLERANCE_PX
                && Math.abs(before.getWidth() - after.getWidth()) <= LAYOUT_TOLERANCE_PX
                && Math.abs(before.getHeight() - after.getHeight()) <= LAYOUT_TOLERANCE_PX;
    }

    private static String describe(Rectangle rect) {
        return rect == null ? "missing"
                : "(" + rect.getX() + "," + rect.getY() + " " + rect.getWidth() + "x" + rect.getHeight() + ")";
    }

    protected void attachScreenshot() {
        Screenshots.attach(androidDriver, String.valueOf(System.currentTimeMillis()));
    }

    protected void attachPageSource() {
        Allure.addAttachment("Page source", "text/xml", androidDriver.getPageSource(), "xml");
    }

    @Step("Press device Back button")
    public void pressBack() {
        androidDriver.pressKey(new KeyEvent(AndroidKey.BACK));
    }

    @Step("Minimise the app and open it again")
    public void minimiseAndRestoreApp() {
        androidDriver.runAppInBackground(Duration.ofSeconds(BACKGROUND_DURATION_SECONDS));
    }

    @Step("Terminate and reopen App")
    public void reopenApp() {
        androidDriver.terminateApp(GetConfigurations.getPackageName());
        androidDriver.activateApp(GetConfigurations.getPackageName());
    }
}
