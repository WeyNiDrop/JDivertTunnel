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
import com.github.jdiverttunnel.weynidrop.common.entity.AddressMapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AddrMappingCache {
    /**
     * A map to cache addr mapping packet where from and send to
     */
    private static Map<String,AddressMapping> addrMap = new ConcurrentHashMap<>();

    public static void put(String k,AddressMapping v){
        addrMap.put(k,v);
    }

    public static AddressMapping get(String k){
        return addrMap.get(k);
    }

    public static void remove(String k){
        addrMap.remove(k);
    }
}
