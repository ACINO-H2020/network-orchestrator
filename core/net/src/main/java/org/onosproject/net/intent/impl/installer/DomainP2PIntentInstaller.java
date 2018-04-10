/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.net.intent.impl.installer;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.net.domain.DomainPointToPointIntent;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentInstaller;
import org.onosproject.net.intent.IntentOperationContext;

@Component(immediate = true)
public class DomainP2PIntentInstaller extends DomainIntentInstallerBase
        implements IntentInstaller<DomainPointToPointIntent> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentExtensionService;

    @Activate
    public void activated() {
        intentExtensionService.registerInstaller(DomainPointToPointIntent.class,
                                                 this);
    }

    @Deactivate
    public void deactivated() {
        intentExtensionService.unregisterInstaller(DomainPointToPointIntent.class);
    }

    @Override
    public void apply(
            IntentOperationContext<DomainPointToPointIntent> context) {
        super.applyInternal(context);
    }

}
