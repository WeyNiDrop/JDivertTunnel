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

package com.github.jdiverttunnel.weynidrop.common.ip;

import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;

public class Ip2RegionSeacher {
    private static final Logger LOGGER = LoggerFactory.getLogger(Ip2RegionSeacher.class);
    private static final Ip2RegionSeacher instance = new Ip2RegionSeacher();
    private DbSearcher searcher;

    private Ip2RegionSeacher() {
        //db
        String dbPath = this.getClass().getResource("/ip2region.db").getPath();
        File file = new File(dbPath);
        if (file.exists() == false) {
            LOGGER.error("Error: Invalid ip2region.db file");
        }
        DbConfig config = null;
        try {
            config = new DbConfig();
            searcher = new DbSearcher(config, dbPath);
        } catch (DbMakerConfigException | FileNotFoundException e) {
            LOGGER.error("Error: init ip2region db searcher fail,e: ", e);
        }
    }

    /**
     * @return the searcher
     */
    public DbSearcher getSearcher() {
        return searcher;
    }

    public static Ip2RegionSeacher instance(){
        return instance;
    }

}
