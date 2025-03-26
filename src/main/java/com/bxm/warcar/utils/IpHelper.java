package com.bxm.warcar.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Random;


public class IpHelper {

    private static final String LOCALHOST_IP6 = "0:0:0:0:0:0:0:1";
    private static final String LOCALHOST_IP4 = "127.0.0.1";


    /**
     * 获取客户端IP
     *
     * @param request
     * @return java.lang.String
     */
    public static String getIpFromHeader(HttpServletRequest request) {
        if (null == request) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (org.apache.commons.lang3.StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.indexOf(',') == -1 ? ip : ip.substring(0, ip.indexOf(','));
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (org.apache.commons.lang3.StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return LOCALHOST_IP6.equals(ip)?LOCALHOST_IP4:ip;
    }

    /**
     *  取前两位的数字
     * @param ip
     * @return
     */
    public static String getIpNoDotTwo(String ip){
        if(StringUtils.isBlank(ip)){
            return "0";
        }
        String[] ips = ip.split("\\.");
        if(ips.length != 4){
            return"0";
        }
        return ips[0]+ips[1];
    }


    /**
     *  取去掉 "." 的所有的数字
     * @param ip
     * @return
     */
    public static String getIpNoDot(String ip){
        if(StringUtils.isBlank(ip)){
            return "0";
        }
        String[] ips = ip.split("\\.");
        if(ips.length != 4){
            return"0";
        }
        return ip.replace(".","");
    }

    /**
     * 将标准格式的ip中转化成int类型的整数
     * 通过左移位操作（<<）给每一段的数字加权，第一段的权为2的24次方，第二段的权为2的16次方，第三段的权为2的8次方，最后一段的权为1
     * ip格式不对时返回0
     * @param ip 0.0.0.0格式的ip
     */
    public static int getIntIp(String ip){
        if(StringUtils.isBlank(ip)){
            return 0;
        }
        String[] ips = ip.split("\\.");
        if(ips.length != 4){
            return 0;
        }
        return (Integer.parseInt(ips[0]) << 24) + (Integer.parseInt(ips[1]) << 16) + (Integer.parseInt(ips[2]) << 8) + Integer.parseInt(ips[3]);
    }

    /**
     * 将整型ip转换未string标准格式的ip
     * 1、将整数值进行右移位操作（>>>），右移24位，右移时高位补0，得到的数字即为第一段IP。
     * 2、通过与操作符（&）将整数值的高8位设为0，再右移16位，得到的数字即为第二段IP。
     * 3、通过与操作符吧整数值的高16位设为0，再右移8位，得到的数字即为第三段IP。
     * 4、通过与操作符吧整数值的高24位设为0，得到的数字即为第四段IP。
     * @param intIp
     * @return
     */
    public static String getStringIp(int intIp) {
        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((intIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((intIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((intIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((intIp & 0x000000FF)));
        return sb.toString();
    }

    /**
     * 生成一个随机IP
     * @return IP
     */
    public static String generate() {
        // ip范围
        int[][] range = { { 607649792, 608174079 }, // 36.56.0.0-36.63.255.255
                { 1038614528, 1039007743 }, // 61.232.0.0-61.237.255.255
                { 1783627776, 1784676351 }, // 106.80.0.0-106.95.255.255
                { 2035023872, 2035154943 }, // 121.76.0.0-121.77.255.255
                { 2078801920, 2079064063 }, // 123.232.0.0-123.235.255.255
                { -1950089216, -1948778497 }, // 139.196.0.0-139.215.255.255
                { -1425539072, -1425014785 }, // 171.8.0.0-171.15.255.255
                { -1236271104, -1235419137 }, // 182.80.0.0-182.92.255.255
                { -770113536, -768606209 }, // 210.25.0.0-210.47.255.255
                { -569376768, -564133889 }, // 222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        return num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
    }

    /**
     * 将十进制转换成IP地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = (ip >> 24) & 0xff;
        b[1] = (ip >> 16) & 0xff;
        b[2] = (ip >> 8) & 0xff;
        b[3] = ip & 0xff;
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);
        return x;
    }
}
