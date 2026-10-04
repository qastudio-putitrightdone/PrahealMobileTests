package com.praheal.mobile.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class FetchConfigData {

    public static Properties getConfigData() {
        String configFilePath = System.getProperty("user.dir") + "/src/main/resources/config.properties";
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(configFilePath)) {
            properties.load(fileInputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config file: " + configFilePath, e);
        }
    }
}
