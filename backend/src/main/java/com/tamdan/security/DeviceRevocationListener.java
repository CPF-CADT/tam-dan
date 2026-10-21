package com.tamdan.security;

public interface DeviceRevocationListener {
    void onDeviceRevoked(DeviceRevocationEvent event);
}
