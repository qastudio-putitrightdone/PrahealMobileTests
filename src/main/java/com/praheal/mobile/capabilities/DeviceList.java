package com.praheal.mobile.capabilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DeviceList {

    public static String getFirstAvailableDevice() {
        List<String> devices = getConnectedDevices();
        if (devices.isEmpty()) {
            throw new RuntimeException("No ADB devices found. Please connect a device or start an emulator.");
        }
        return devices.get(0);
    }

    public static List<String> getConnectedDevices() {
        List<String> devices = new ArrayList<>();

        try {
            Process process = new ProcessBuilder("adb", "devices").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()
                            && !line.startsWith("List of devices")
                            && line.endsWith("device")) {
                        devices.add(line.split("\\s+")[0]);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return devices;
    }
}
