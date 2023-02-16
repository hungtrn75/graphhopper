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
import com.graphhopper.routing.ev.CDbName;
import com.graphhopper.routing.ev.CWayType;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.storage.IntsRef;

public class CWayTypeParser implements TagParser {

  private final IntEncodedValue dbNameEnc;

  public CWayTypeParser(IntEncodedValue dbNameEnc) {
    this.dbNameEnc = dbNameEnc;
  }

  @Override
  public IntsRef handleWayTags(IntsRef edgeFlags, ReaderWay readerWay, IntsRef relationFlags) {
    int osmId = 1;
    if (readerWay.hasTag(CWayType.KEY)) {
      String value = readerWay.getTag(CWayType.KEY);
      osmId = Integer.parseInt(value);
    }
    dbNameEnc.setInt(false, edgeFlags, osmId);
    return edgeFlags;
  }
}
