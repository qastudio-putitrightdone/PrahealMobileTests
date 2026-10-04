package com.praheal.mobile.listeners;

import io.qameta.allure.LabelAnnotation;

import java.lang.annotation.*;

public class PrahealLabels {

    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @LabelAnnotation(name = "Test Case ID")
    public @interface TestID {
        String value();
    }

    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @LabelAnnotation(name = "Scenario ID")
    public @interface ScenarioID {
        String value();
    }
}
