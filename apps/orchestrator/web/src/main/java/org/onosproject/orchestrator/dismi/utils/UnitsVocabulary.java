/*
 * Copyright (c) 2018 ACINO Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.orchestrator.dismi.utils;

import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-10-06.
 */
public class UnitsVocabulary extends HashMap<String, HashMap> {

    private final Logger log = getLogger(getClass());
    public static final String BANDWIDTHUNITS = "bandwidth-units";
    public static final String TIMEUNITS = "time-units";


    public UnitsVocabulary() {
        loadUnitsVocabulary();
    }

    public HashMap<String, Double> put(String word, HashMap<String, Double> values) {
        return super.put(word, values);
    }

    public HashMap<String, Double> get(int index) {
        return super.get(index);
    }

    public HashMap<String, Double> get(String word) {
        return super.get(word);
    }


    private void loadUnitsVocabulary() {
        //log.info("Loading vocabulary for validating and resolving units !");
        //------------------------------------------------
        HashMap<String, Double> bandwidthUnits = new HashMap<String, Double>();
        bandwidthUnits.put("tbits/s", new Double(1000000000000.00));
        bandwidthUnits.put("gbits/s", new Double(1000000000.00));
        bandwidthUnits.put("mbits/s", new Double(1000000.00));
        bandwidthUnits.put("kbits/s", new Double(1000.00));
        bandwidthUnits.put("bits/s", new Double(1.00));
        bandwidthUnits.put("bps", new Double(1.00));
        bandwidthUnits.put("kbps", new Double(1000.00));
        bandwidthUnits.put("mbps", new Double(1000000.00));
        bandwidthUnits.put("gbps", new Double(1000000000.00));
        bandwidthUnits.put("tbps", new Double(1000000000000.00));
        this.put(BANDWIDTHUNITS, bandwidthUnits);

        //-------------------------------------------------------
        HashMap<String, Double> timeUnits = new HashMap<String, Double>();
        timeUnits.put("us", new Double(1 / 10000000.00));
        timeUnits.put("ms", new Double(1 / 1000.00));
        timeUnits.put("s", new Double(1.00));
        timeUnits.put("m", new Double(60.00));
        timeUnits.put("h", new Double(60 * 60.00));
        timeUnits.put("d", new Double(60 * 60 * 24.00));
        timeUnits.put("w", new Double(60 * 60 * 24 * 7.00));
        this.put(TIMEUNITS, timeUnits);
    }
}
