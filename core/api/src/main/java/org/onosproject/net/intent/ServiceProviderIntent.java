package org.onosproject.net.intent;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import java.util.Collection;
import java.util.List;

public abstract class ServiceProviderIntent extends ConnectivityIntent {

    protected ServiceProviderIntent(ApplicationId appId,
                                    Key key,
                                    Collection<NetworkResource> resources,
                                    TrafficSelector selector,
                                    TrafficTreatment treatment,
                                    List<Constraint> constraints,
                                    int priority) {
        super(appId, key, resources, selector, treatment,
              constraints, priority);
    }

    protected ServiceProviderIntent() {
        super();
    }
}
