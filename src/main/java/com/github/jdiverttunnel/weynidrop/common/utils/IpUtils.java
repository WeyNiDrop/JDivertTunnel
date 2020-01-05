/*
 * Copyright (c) WeyNiDrop 2020.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.jdiverttunnel.weynidrop.common.utils;

import com.github.jdiverttunnel.weynidrop.common.constants.IpConstants;
import com.github.jdiverttunnel.weynidrop.common.ip.Ip2RegionSeacher;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbSearcher;
import org.lionsoul.ip2region.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.util.IPAddressUtil;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);
    private static final List<String> OUT_MAINLAND_CITIES = Arrays.asList("香港", "澳门", "台湾省");

    private static final String IPV4_REX = "^(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))$";

    private static final String IPV6_REX = "((([0-9a-f]{1,4}:){7}([0-9a-f]{1,4}|:))|(([0-9a-f]{1,4}:){6}(:[0-9a-f]{1,4}|((25[0-5]|2[0-4]\\d" +
            "|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-f]{1,4}:){5}(((:[0-9a-f]{1,4}){1,2})|:((25[0-5]" +
            "|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9a-f]{1,4}:){4}(((:[0-9a-f]{1,4}){1,3})|" +
            "((:[0-9a-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-f]{1,4}:)" +
            "{3}(((:[0-9a-f]{1,4}){1,4})|((:[0-9a-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?" +
            "\\d)){3}))|:))|(([0-9a-f]{1,4}:){2}(((:[0-9a-f]{1,4}){1,5})|((:[0-9a-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\." +
            "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9a-f]{1,4}:){1}(((:[0-9a-f]{1,4}){1,6})|((:[0-9a-f]{1,4}){0,4}:((25[0-5]" +
            "|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9a-f]{1,4}){1,7})|((:[0-9a-f]{1,4})" +
            "{0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?\\s*$";

    private IpUtils() {
    }

    /**
     * 根据单个ip，判断版本
     *
     * @param ip ip
     * @return int
     */
    public static String checkIPVersion(String ip) {
        if (IPAddressUtil.isIPv4LiteralAddress(ip)) {
            return IpConstants.IPV4;
        }
        if (IPAddressUtil.isIPv6LiteralAddress(ip)) {
            return IpConstants.IPV6;
        }
        return null;
    }


    /**
     * 判断字符串是否是ipv4
     * @param ip ip
     * @return boolean
     */
    public static boolean isIp4(String ip){
        Pattern pattern = Pattern.compile(IPV4_REX);
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    /**
     * 判断字符串是否是ipv6
     * @param ip ip
     * @return boolean
     */
    public static boolean isIp6(String ip) {
        Pattern pattern = Pattern.compile(IPV6_REX);
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    /**
     * 获取本机所有IP地址，ip4 及 ip6
     *
     * @return Map<String , List < String>>
     * @throws RuntimeException e
     */
    public static Map<String, List<String>> getLocalAllIpList() {
        try {
            Map<String, List<String>> ipMaps = new HashMap<>();
            List<String> ip4List = new ArrayList<>();
            List<String> ip6List = new ArrayList<>();
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            for (InetAddress inetAddress : InetAddress.getAllByName(hostname)) {
                String ipAddr = inetAddress.getHostAddress();
                if (inetAddress instanceof Inet4Address) {
                    ip4List.add(ipAddr);
                } else if (inetAddress instanceof Inet6Address) {
                    int index = ipAddr.indexOf('%');
                    if (index > 0) {
                        ipAddr = ipAddr.substring(0, index);
                    }
                    ip6List.add(ipAddr);
                }
            }
            ipMaps.put(IpConstants.IPV4, ip4List);
            ipMaps.put(IpConstants.IPV6, ip6List);
            return ipMaps;
        } catch (UnknownHostException e) {
            LOGGER.error("获取本地ip地址集合失败：", e);
            return null;
        }
    }

    /**
     * Get ip location info
     *
     * @param ip ip
     * @return String
     * @throws Exception e
     */
    public static String getCityInfo(String ip) throws Exception {
        //查询算法B-tree
        int algorithm = DbSearcher.BTREE_ALGORITHM;
        DbSearcher searcher = Ip2RegionSeacher.instance().getSearcher();
        //define the method
        Method method = null;
        switch (algorithm) {
            case DbSearcher.BTREE_ALGORITHM:
                method = searcher.getClass().getMethod("btreeSearch", String.class);
                break;
            case DbSearcher.BINARY_ALGORITHM:
                method = searcher.getClass().getMethod("binarySearch", String.class);
                break;
            case DbSearcher.MEMORY_ALGORITYM:
                method = searcher.getClass().getMethod("memorySearch", String.class);
                break;
            default:
                return null;
        }
        DataBlock dataBlock = null;
        if (!Util.isIpAddress(ip)) {
            throw new UnknownHostException("Error: Invalid ip address");
        }
        dataBlock = (DataBlock) method.invoke(searcher, ip);
        return dataBlock.getRegion();
    }

    /**
     * Whether this is chinese ip or not, if it's at local or LAN, return null
     *
     * @param ip ip
     * @return boolean
     */
    public static Boolean isChineseMainLandIp(String ip) {
        try {
            if (isSpecialIp(ip)) {
                return null;
            }
            String[] infos = getCityInfo(ip).split("\\|");
            //special ip
            if ("0".equals(infos[0])){
                return null;
            }
            return "中国".equals(infos[0])&&!OUT_MAINLAND_CITIES.contains(infos[2]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * whether this ip is special ip
     *
     * @param ip ip
     * @return boolean
     * @throws UnknownHostException e
     */
    private static boolean isSpecialIp(String ip) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(ip);
        if (addr.isAnyLocalAddress() || addr.isLinkLocalAddress() || addr.isLoopbackAddress() || addr.isSiteLocalAddress()
                || addr.isMulticastAddress() || addr.isMCGlobal() || addr.isMCSiteLocal() || addr.isMCNodeLocal()) {
            return true;
        }
        return false;
    }

    /**
     * 获取DNS服务器信息
     *
     * @param domain  要获取DNS信息的域名
     * @param provider      DNS服务器
     * @param types   信息类型 "A"(IP信息)，"MX"
     * @param timeout 请求超时
     * @param retryCount    重试次数
     *
     * @return 所有信息组成的数组
     *
     * @throws NamingException e
     *
     */
    private static ArrayList<String> getDNSRecs(String domain, String provider,
                                                String[] types, int timeout, int retryCount) throws NamingException {
        ArrayList<String> results = new ArrayList<>(15);

        Hashtable<String, String> env = new Hashtable<>();

        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        //设置域名服务器
        env.put(Context.PROVIDER_URL, "dns://" + provider);
        // 连接时间
        env.put("com.sun.jndi.dns.timeout.initial", String.valueOf(timeout));
        // 连接次数
        env.put("com.sun.jndi.dns.timeout.retries", String.valueOf(retryCount));

        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes(domain, types);

        for (Enumeration e = attrs.getAll(); e.hasMoreElements(); ) {
            Attribute a = (Attribute) e.nextElement();
            int size = a.size();
            for (int i = 0; i < size; i++) {
                results.add((String) a.get(i));
            }
        }
        return results;
    }

    /**
     * 获取域名所有IP
     *
     * @param domain     域名
     * @param dnsServers DNS服务器列表
     * @param timeout    请求超时
     * @param retryCount 重试次数
     * @return Set<String>
     */
    public static Set<String> getAllIP(String domain, String[] dnsServers,
                                    int timeout, int retryCount) {
        Set<String> ips = new HashSet<>();
        for (String dnsServer : dnsServers) {
            List<String> ipList;
            try {
                ipList = getDNSRecs(domain, dnsServer, new String[]{"A"},
                        timeout, retryCount);
            } catch (NamingException e) {
                continue;
            }
            ips.addAll(ipList);
        }
        return ips;
    }

    /**
     * 获取域名所有IP(使用本机dns)
     *
     * @param domain 域名
     * @return Set<String>
     */
    public static Set<String> getAllIP(String domain) {
        Set<String> ips = new HashSet<>();
        InetAddress[] addresses = null;
        try {
            addresses = InetAddress.getAllByName(domain);
            Arrays.stream(addresses).forEach(address->ips.add(address.getHostAddress()));
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }
        return ips;
    }

}
