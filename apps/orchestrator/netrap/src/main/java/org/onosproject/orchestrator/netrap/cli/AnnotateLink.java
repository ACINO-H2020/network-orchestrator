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
import org.apache.karaf.shell.commands.Option;
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
@Command(scope = "acino", name = "annotate-link",
        description = "Annotates a link, bi-directionally")
public class AnnotateLink extends AbstractShellCommand {

    static final ProviderId PID = new ProviderId("cli", "org.onosproject.cli", true);

    @Argument(index = 0, name = "src", description = "Source device/port",
            required = true, multiValued = false)
    String src = null;

    @Argument(index = 1, name = "dst", description = "Destination device/port",
            required = true, multiValued = false)
    String dst = null;

    @Argument(index = 2, name = "key", description = "Annotation key",
            required = true, multiValued = false)
    String key = null;

    @Argument(index = 3, name = "value",
            description = "Annotation value (null to remove)",
            required = false, multiValued = false)
    String value = null;

    @Option(name = "-u", aliases = "--unidirectionally",
            description = "annotate only unidirectionally from source to dest",
            required = false, multiValued = false)
    Boolean unidir = false;


    @Override
    protected void execute() {
        LinkService service = get(LinkService.class);
        boolean modified = false;
        LinkProviderRegistry registry = get(LinkProviderRegistry.class);
        LinkProvider provider = new AnnotationProvider();

        // TODO: don't iterate through links, use ConnectPoint lookup
        try {
            LinkProviderService providerService = registry.register(provider);
            for (Link l : service.getLinks()) {
                if (l.dst().toString().equals(dst) && l.src().toString().equals(src)) {
                    providerService.linkDetected(description(l, key, value));
                    modified = true;
                }
                if (!unidir && l.dst().toString().equals(src) && l.src().toString().equals(dst)) {
                    providerService.linkDetected(description(l, key, value));
                    modified = true;
                }
            }
        } finally {
            registry.unregister(provider);
        }
        if (!modified) {
            print("Could not find link!");
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
    public static final class AnnotationProvider
            extends AbstractProvider implements LinkProvider {
        public AnnotationProvider() {
            super(PID);
        }

    }
}

