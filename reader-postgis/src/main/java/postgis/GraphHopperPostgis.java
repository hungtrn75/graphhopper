/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package postgis;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.reader.dem.ElevationProvider;
import com.graphhopper.reader.dem.VietNamProvider;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.BaseGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.graphhopper.util.Helper.createFormatter;
import static com.graphhopper.util.Helper.getMemInfo;

/**
 * Modified version of GraphHopper to optimize working with Postgis
 *
 * @author Phil
 * @author Robin Boldt
 */
public class GraphHopperPostgis extends GraphHopper {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, String> postgisParams = new HashMap<>();

    public GraphHopperPostgis(GraphHopperConfig ghConfig) {
        super.setOSMFile(ghConfig.getString("datareader.file", ""));
        postgisParams.put("dbtype", "postgis");
        postgisParams.put("host", ghConfig.getString("db.host", ""));
        postgisParams.put("port", String.valueOf(ghConfig.getInt("db.port", 5432)));
        postgisParams.put("schema", ghConfig.getString("db.schema", ""));
        postgisParams.put("database", ghConfig.getString("db.database", ""));
        postgisParams.put("user", ghConfig.getString("db.user", ""));
        postgisParams.put("passwd", ghConfig.getString("db.passwd", ""));
        postgisParams.put("tags_to_copy", ghConfig.getString("db.tags_to_copy", ""));
        postgisParams.put("datareader.file", ghConfig.getString("datareader.file", ""));
        String cacheDirStr = ghConfig.getString("graph.elevation.cache_dir", "");
        ElevationProvider elevationProvider = new VietNamProvider(cacheDirStr);
        setElevationProvider(elevationProvider);
    }

    @Override
    protected void importOSM() {
        BaseGraph baseGraph = super.getBaseGraph();

        logger.info("start creating graph from " + super.getOSMFile());

        OSMReader reader = new OSMPostgisReader(baseGraph, super.getEncodingManager(), super.getOSMParsers(),super.getProperties(), postgisParams)
                .setFile(_getOSMFile()).
                setElevationProvider(super.getElevationProvider())
                .setCountryRuleFactory(super.getCountryRuleFactory());
        logger.info("using " + baseGraph.toString() + ", memory:" + getMemInfo());
        try {
            reader.readGraph();
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read file " + getOSMFile(), ex);
        }
        DateFormat f = createFormatter();
        super.getProperties().put("datareader.import.date", f.format(new Date()));
        if (reader.getDataDate() != null)
            super.getProperties().put("datareader.data.date", f.format(reader.getDataDate()));
    }
}
