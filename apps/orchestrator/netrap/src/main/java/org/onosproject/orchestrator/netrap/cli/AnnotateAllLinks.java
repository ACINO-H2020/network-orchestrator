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
package org.onosproject.orchestrator.netrap.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Link;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkProvider;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;


/**
 * Annotates network link model.
 */
@Command(scope = "acino", name = "annotate-links",
        description = "Annotates all links")
public class AnnotateAllLinks extends AbstractShellCommand {

    static final ProviderId PID = new ProviderId("cli", "org.onosproject.cli", true);

    @Argument(index = 0, name = "key", description = "Annotation key",
            required = true, multiValued = false)
    String key = null;

    @Argument(index = 1, name = "value",
            description = "Annotation value (null to remove)",
            required = false, multiValued = false)
    String value = null;

    @Override
    protected void execute() {
        LinkService service = get(LinkService.class);

        LinkProviderRegistry registry = get(LinkProviderRegistry.class);
        LinkProvider provider = new AnnotationProvider();
        try {
            LinkProviderService providerService = registry.register(provider);
            for (Link l : service.getLinks()) {
                providerService.linkDetected(description(l, key, value));
            }
        } finally {
            registry.unregister(provider);
        }
    }

    private LinkDescription description(Link link, String key, String value) {
        DefaultAnnotations.Builder builder = DefaultAnnotations.builder();
        if (value != null) {
            builder.set(key, value);
        } else {
            builder.remove(key);
        }
        return new DefaultLinkDescription(link.src(), link.dst(), link.type(), link.isExpected(), builder.build());
    }


    // Token provider entity
    private static final class AnnotationProvider
            extends AbstractProvider implements LinkProvider {
        private AnnotationProvider() {
            super(PID);
        }

    }
}
