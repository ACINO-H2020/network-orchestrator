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

package org.onosproject.orchestrator.dismi.validation;

import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-10-06.
 */
public class SelectorVocabulary extends HashMap<String, HashMap> {
    private final Logger log = getLogger(getClass());
    public static final String IPDSCP = "IP-Differentiated_service_Code_Point";


    public SelectorVocabulary() {
        loadVocabulary();
    }

    public HashMap<String, Integer> put(String word, HashMap<String, Integer> values) {
        return super.put(word, values);
    }

    public HashMap<String, Integer> get(int index) {
        return super.get(index);
    }

    public HashMap<String, Integer> get(String word) {
        return super.get(word);
    }

    private void loadVocabulary() {
        log.info("Loading selector Vocabulary !");
        //http://www.netcontractor.pl/blog/wp-content/uploads/2010/06/QoS-Values-Calculator-v2.jpg

        //http://www.bogpeople.com/networking/dscp.shtml
        //-------------------------------------------------------
        HashMap<String, Integer> ipDscp = new HashMap<String, Integer>();
        // DSCP name and Differenciated Services (DS) Field Value in decimal
        ipDscp.put("CS0", new Integer(0));
        ipDscp.put("CS1", new Integer(8));
        ipDscp.put("AF11", new Integer(10));
        ipDscp.put("AF12", new Integer(12));
        ipDscp.put("AF13", new Integer(14));
        ipDscp.put("CS2", new Integer(16));
        ipDscp.put("AF21", new Integer(18));
        ipDscp.put("AF22", new Integer(20));
        ipDscp.put("AF23", new Integer(22));
        ipDscp.put("CS3", new Integer(24));
        ipDscp.put("AF31", new Integer(26));
        ipDscp.put("AF32", new Integer(28));
        ipDscp.put("AF33", new Integer(30));
        ipDscp.put("CS4", new Integer(32));
        ipDscp.put("AF41", new Integer(34));
        ipDscp.put("AF42", new Integer(36));
        ipDscp.put("AF43", new Integer(38));
        ipDscp.put("CS5", new Integer(40));
        ipDscp.put("EF", new Integer(46));
        ipDscp.put("CS6", new Integer(48));
        ipDscp.put("CS7", new Integer(56));
        this.put(IPDSCP, ipDscp);
    }
}
