package com.classpets.backend.auth.util;

public class DeviceUtil {

    public static final String DEVICE_PC = "PC";
    public static final String DEVICE_MOBILE = "MOBILE";
    public static final String DEVICE_TABLET = "TABLET";

    /**
     * Determine device type from User-Agent string.
     * 
     * @param userAgent User-Agent header value
     * @return DEVICE_PC, DEVICE_MOBILE, or DEVICE_TABLET
     */
    public static String getDeviceType(String userAgent) {
        if (userAgent == null) {
            return DEVICE_PC;
        }

        String agent = userAgent.toLowerCase();

        // Check for Tablet first (iPad or Android tablets often have 'android' but not
        // 'mobile', or specific tablet keywords)
        if (agent.contains("ipad") || (agent.contains("android") && !agent.contains("mobile"))
                || agent.contains("tablet")) {
            return DEVICE_TABLET;
        }

        // Check for Mobile
        if (agent.contains("mobile") || agent.contains("iphone") || agent.contains("android")) {
            return DEVICE_MOBILE;
        }

        // Default to PC
        return DEVICE_PC;
    }
}
