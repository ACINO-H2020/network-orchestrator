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

package org.onosproject.drivers.tapi;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.onlab.util.GuavaCollectors;
import org.onosproject.net.domain.DomainIntent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component(immediate = true)
@Service
public class TapiIntentToTunnelMapper implements TapiIntentToTunnel {

    private Map<DomainIntent, String> intentToIdentifier =
            new ConcurrentHashMap<>();
    private Set<DomainIntent> taggedForDeletion = new HashSet<>();

    @Override
    public void addTunnelIdentifier(DomainIntent intent, String identifier) {
        intentToIdentifier.put(intent, identifier);
    }

    @Override
    public boolean contains(DomainIntent intent) {
        return intentToIdentifier.containsKey(intent);
    }

    @Override
    public Set<DomainIntent> sameKey(DomainIntent intent) {
        return intentToIdentifier.keySet()
                .stream()
                .filter(key -> key.key().equals(intent.key()))
                .collect(GuavaCollectors.toImmutableSet());
    }

    @Override
    public boolean isTaggedForDeletion(DomainIntent intent) {
        return taggedForDeletion.contains(intent);
    }

    @Override
    public String getTunnelIdentifier(DomainIntent intent) {
        return intentToIdentifier.get(intent);
    }

    @Override
    public String removeIntent(DomainIntent intent) {
        String tunnelId = intentToIdentifier.remove(intent);
        Set<DomainIntent> sameKeys = intentToIdentifier.entrySet()
                .stream()
                .filter(entry -> tunnelId.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        taggedForDeletion.addAll(sameKeys);
        return tunnelId;
    }

}
