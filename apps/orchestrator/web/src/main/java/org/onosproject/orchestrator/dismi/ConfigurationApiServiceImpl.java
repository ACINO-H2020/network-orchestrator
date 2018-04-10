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

package org.onosproject.orchestrator.dismi;

import org.onosproject.orchestrator.dismi.aciIntents.ModelType;
import org.onosproject.orchestrator.dismi.api.ApiResponseMessage;
import org.onosproject.orchestrator.dismi.compiler.OnosFeatures;
import org.onosproject.orchestrator.dismi.primitives.DismiConfiguration;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-07-26.
 */
public class ConfigurationApiServiceImpl implements ConfigurationApiService {
    private final Logger log = getLogger(getClass());

    public Response configurationGet() {

        // Following both classes are used to set configuration
        DismiConfiguration dismiConfiguration = new DismiConfiguration();
        dismiConfiguration.setBidirectional(OnosFeatures.isBidirectional());
        dismiConfiguration.setUnidirectional(OnosFeatures.isUnidirectional());
        dismiConfiguration.setNewModel(ModelType.isNewModel());
        try {
            JsonMapper jsonMapper = new JsonMapper();
            String jsonResponse = jsonMapper.toJson(dismiConfiguration);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problems when processing. " + e.getMessage());
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        e.getMessage()).toJson()).build();
        }
    }

    public Response configurationPut(DismiConfiguration dismiConfiguration) {

        OnosFeatures.setUnidirectional(dismiConfiguration.isUnidirectional());
        OnosFeatures.setBidirectional(dismiConfiguration.isBidirectional());
        ModelType.setNewModel(dismiConfiguration.isNewModel());

        if (ModelType.isNewModel()) {
            log.info("Current DISMI is configured to operate with new ACI Intents [ACIPPIntent] !");
        } else {
            log.info("Current DISMI is configured to operate with old, flat model using [AciIntent]!");
        }
        return Response.ok().build();

    }

    public Response configurationDefault() {

        DismiConfiguration dismiConfiguration = new DismiConfiguration();
        // Set default configuration parameters
        OnosFeatures.setUnidirectional(false);
        OnosFeatures.setBidirectional(true);
        ModelType.setNewModel(false);

        dismiConfiguration.setBidirectional(OnosFeatures.isBidirectional());
        dismiConfiguration.setUnidirectional(OnosFeatures.isUnidirectional());
        dismiConfiguration.setNewModel(ModelType.isNewModel());
        if (ModelType.isNewModel()) {
            log.info("Current DISMI is configured to operate with new ACI Intents [ACIPPIntent] !");
        } else {
            log.info("Current DISMI is configured to operate with old, flat model using [AciIntent]!");
        }
        try {
            JsonMapper jsonMapper = new JsonMapper();
            String jsonResponse = jsonMapper.toJson(dismiConfiguration);
            return Response.ok(jsonResponse).build();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Problems when processing. " + e.getMessage());
            return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ErrorCode.PROCESSING_ERROR,
                                                                        e.getMessage()).toJson()).build();
        }
    }
}
