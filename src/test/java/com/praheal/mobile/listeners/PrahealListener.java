package com.praheal.mobile.listeners;

import com.praheal.mobile.base.BaseMobileTest;
import com.praheal.mobile.base.StepTracker;
import io.qameta.allure.Allure;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.reflect.Method;

import static com.praheal.mobile.base.StepInterceptor.CHECK_PREFIX;

public class PrahealListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod()) {
            return;
        }
        StepTracker.reset();

        Method javaMethod = method.getTestMethod().getConstructorOrMethod().getMethod();

        PrahealLabels.TestID testID = javaMethod.getAnnotation(PrahealLabels.TestID.class);
        if (testID != null) {
            addTag("Test Case ID: ", testID.value());
        }

        PrahealLabels.ScenarioID scenarioID = javaMethod.getAnnotation(PrahealLabels.ScenarioID.class);
        if (scenarioID != null) {
            addTag("Scenario ID: ", scenarioID.value());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod()
                || !(testResult.getInstance() instanceof BaseMobileTest)
                || testResult.getStatus() != ITestResult.SUCCESS) {
            return;
        }
        String lastStep = StepTracker.getLastTopLevelStep();
        if (lastStep == null || !lastStep.startsWith(CHECK_PREFIX)) {
            testResult.setStatus(ITestResult.FAILURE);
            testResult.setThrowable(new AssertionError(
                    "Test must end with a " + CHECK_PREFIX + "... step, but the last step was: " + lastStep));
        }
    }

    private static void addTag(String prefix, String value) {
        Allure.label("tag", prefix + value);
    }
}
