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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.DismiConfiguration;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;

import java.io.IOException;

public class JsonMapper {
    public JsonMapper() {

    }

    /**
     * @param json : Accepts a Service instance encoded in JSON format.
     * @return service : returns an instance of Service.
     * @description : toJson(...) method accepts a service serialized as a JSON-encoded string,
     * and uses Jackson mapper to convert it into a Service instance.
     */
    public static Service jsonToService(String json) throws Exception {

        Object o = jsonToObject(json, Service.class);
        Service service = null;
        if (!(o instanceof Service)) {
            throw new Exception("JsonMapper::jsonToService: Input json object is not of class Service!");
        }

        service = (Service) o;
        return service;
    }

    /**
     * @param json : Accepts an Intent instance encoded in JSON format.
     * @return Intent: returns an instance of intent.
     * @description : toJson(...) method accepts an intent serialized as a JSON-encoded string.
     * and uses Jackson mapper to convert it into an Intent instance.
     */
    public static Intent jsonToIntent(String json) throws Exception {

        Object o = jsonToObject(json, Intent.class);
        Intent intent = null;
        if (!(o instanceof Intent)) {
            throw new Exception("JsonMapper::jsonToIntent: Input json object is not of class Intent!");
        }

        intent = (Intent) o;
        return intent;
    }

    /**
     * @param json : Accepts a ConnectionPoint instance encoded in JSON format.
     * @return point : returns an instance of ConnectionPoint.
     * @description : jsonToConnectionPoint(...) accepts a ConnectionPoint serialized as a JSON encoded string.
     * and uses Jackson mapper to convert it into a ConnectionPoint instance.
     */
    public static ConnectionPoint jsonToConnectionPoint(String json) throws Exception {

        Object o = jsonToObject(json, ConnectionPoint.class);
        ConnectionPoint point = null;
        if (!(o instanceof ConnectionPoint)) {
            throw new Exception("JsonMapper::jsonToConnectionPoint: Input json object is not of class Service!");
        }

        point = (ConnectionPoint) o;
        return point;
    }

    /**
     * @param json : Accepts a ConnectionPoint instance encoded in JSON format.
     * @return point : returns an instance of ConnectionPoint.
     * @description : jsonToConnectionPointExtended(...) accepts a ConnectionPointExtended serialized.
     * as a JSON encoded string, and uses Jackson mapper to convert it into a ConnectionPoint instance.
     */
    public static ConnectionPointExtended jsonToConnectionPointExtended(String json) throws Exception {

        Object o = jsonToObject(json, ConnectionPointExtended.class);
        ConnectionPointExtended point = null;
        if (!(o instanceof ConnectionPointExtended)) {
            throw new Exception("JsonMapper::jsonToConnectionPointExtended: " +
                                        "Input json object is not of class ConnectionPointExtended!");
        }

        point = (ConnectionPointExtended) o;
        return point;
    }

    /**
     * @param json      : Accepts service encoded in json format.
     * @param classname : Accepts the Java class name of the expected object instance.
     * @return Object : returns an instance of the requested object.
     * @description : jsonToObject(...) method accepts a Java object encoded as a JSON string.
     * and the class name of the object.
     * The Jackson mapper is used to convert it into a Java object.
     */
    public static Object jsonToObject(String json, Class classname) throws Exception {

        Object object = null;
        if (null == json) {
            throw new Exception("JsonMapper::jsonToObject: Input json string is null !");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            object = mapper.readValue(json, classname);
        } catch (JsonParseException e) {
            throw new Exception("JsonMapper::jsonToObject:: " + e.getMessage());
        } catch (JsonMappingException e) {
            throw new Exception("JsonMapper::jsonToObject:: " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("JsonMapper::jsonToObject:: " + e.getMessage());
        }
        return object;
    }

    /**
     * @param object : Accepts an instance of the object for converting in json format.
     * @return String : Return objcet instance in JSON format.
     * @description : toJson(...) method accepts an instance of service and then uses Jackson mapper to convert.
     * into.
     * json encoded format.
     */
    public static String toJson(Object object) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        if (null == object) {
            throw new Exception("JsonMapper::objectToJson: Input object is null !");
        }
        String jsonObject = null;
        try {
            jsonObject = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new Exception("JsonMapper::objectToJson: " + e.getMessage());
        }
        return jsonObject;
    }

    /**
     * @param json : Accepts a DismiConfiguration instance encoded in JSON format.
     * @return service : returns an instance of DismiConfiguration.
     * @description : toJson(...) method accepts a DismiConfiguration serialized as a JSON-encoded string,
     * and uses Jackson mapper to convert it into a DismiConfiguration instance.
     */
    public static DismiConfiguration jsonToDismiConfiguration(String json) throws Exception {

        Object o = jsonToObject(json, DismiConfiguration.class);
        DismiConfiguration dismiConfiguration = null;
        if (!(o instanceof DismiConfiguration)) {
            throw new Exception("JsonMapper::jsonToDismiConfiguration: Input json object is not of class " +
                                        "DismiConfiguration !");
        }
        dismiConfiguration = (DismiConfiguration) o;
        return dismiConfiguration;
    }
}
