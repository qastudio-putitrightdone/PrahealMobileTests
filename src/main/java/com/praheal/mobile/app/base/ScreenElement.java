package com.praheal.mobile.app.base;

import org.openqa.selenium.WebElement;

import java.util.Locale;

public interface ScreenElement<S extends BaseScreen> {

    WebElement of(S screen);

    String name();

    default String label() {
        String words = name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
