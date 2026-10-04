package com.praheal.mobile.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class Secrets {

    private static final Path SECRETS_FILE = Path.of(System.getProperty("user.dir"), "secrets.properties");
    private static final Properties FILE_VALUES = load();

    private Secrets() {
    }

    public static String require(String key) {
        String value = System.getenv(toEnvName(key));
        if (value == null || value.isBlank()) {
            value = FILE_VALUES.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing secret '" + key + "': set env var " + toEnvName(key)
                    + " or add it to secrets.properties (see secrets.properties.example)");
        }
        return value;
    }

    private static String toEnvName(String key) {
        return "PRAHEAL_" + key.replace('.', '_').replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    private static Properties load() {
        Properties properties = new Properties();
        if (Files.exists(SECRETS_FILE)) {
            try (FileInputStream inputStream = new FileInputStream(SECRETS_FILE.toFile())) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to read " + SECRETS_FILE, e);
            }
        }
        return properties;
    }
}
