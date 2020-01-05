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
 *
 *
 */

package com.github.jdiverttunnel.weynidrop.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dns 缓存，通过代理服务器进行dns解析时候使用
 */
public class DnsCache {
    private static Map<String, String> dnsMap = new ConcurrentHashMap<>();

    public static void put(String k, String v) {
        dnsMap.put(k, v);
    }

    public static String get(String k) {
        return dnsMap.get(k);
    }

    public static void remove(String k) {
        dnsMap.remove(k);
    }
}
