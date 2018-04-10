package org.onosproject.orchestrator.dismi.primitives;

/**
 * Container for ACINO App Store Actions
 */
public abstract class ServiceAction extends Action {

    protected Subject source;
    protected Subject destination;

    public Subject getSource() {
        return source;
    }

    public Subject getDestination() {
        return destination;
    }
}
