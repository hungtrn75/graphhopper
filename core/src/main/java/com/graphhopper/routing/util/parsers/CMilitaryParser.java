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

package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.storage.IntsRef;

/**
 * https://wiki.openstreetmap.org/wiki/Key:lanes
 */
public class CMilitaryParser implements TagParser {
    private final IntEncodedValue militaryEnc;

    public CMilitaryParser(IntEncodedValue militaryEnc) {
        this.militaryEnc = militaryEnc;
    }

    @Override
    public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay way, IntsRef relationFlags) {
        int militaryId = 0;
        if (way.hasTag("military_id")) {
            String value = way.getTag("military_id");
            militaryId = Integer.parseInt(value);

        }
        militaryEnc.setInt(false, edgeFlags, militaryId);
        return edgeFlags;
    }
}
