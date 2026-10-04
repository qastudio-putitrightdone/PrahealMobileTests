package com.praheal.mobile.capabilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InstalledChecks {

    public static boolean isAppInstalled(String deviceId, String packageName) {
        try {
            Process process = new ProcessBuilder(
                    "adb", "-s", deviceId, "shell", "pm", "list", "packages", packageName).start();

            boolean installed = false;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().equals("package:" + packageName)) {
                        installed = true;
                    }
                }
            }
            process.waitFor();
            return installed;
        } catch (IOException e) {
            throw new RuntimeException("Error checking if app is installed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
