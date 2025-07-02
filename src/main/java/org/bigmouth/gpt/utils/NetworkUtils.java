package org.bigmouth.gpt.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Allen Hu
 * @date 2025/4/18
 */
@Slf4j
public class NetworkUtils {

    public static String getPlainMacAddress() {
        String macAddress = getMacAddress();
        if (macAddress != null) {
            return macAddress.replaceAll(":", "");
        }
        return null;
    }

    /**
     * 获取本机的MAC地址
     * @return MAC地址，可能为空
     */
    public static String getMacAddress() {
        try {
            StringBuilder macAddress = new StringBuilder();
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = interfaces.nextElement();
                try {
                    if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) {
                        continue;
                    }
                    boolean hasValidAddress = false;
                    java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                            hasValidAddress = true;
                            break;
                        }
                    }
                    if (!hasValidAddress) {
                        continue;
                    }
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null) {
                        for (int i = 0; i < mac.length; i++) {
                            macAddress.append(String.format("%02X", mac[i]));
                            if (i < mac.length - 1) {
                                macAddress.append(":");
                            }
                        }
                        break;
                    }
                } catch (java.net.SocketException e) {
                    log.error("Get network interface information failed", e);
                }
            }

            if (macAddress.length() > 0) {
                return macAddress.toString();
            }
            log.warn("No network interface found, using random UUID as client ID");
        } catch (Exception e) {
            log.error("生成客户端ID失败", e);
        }
        return null;
    }
}
