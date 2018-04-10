package org.onosproject.netconf;

/**
 * Created by michele on 16/08/16.
 */

import org.onosproject.net.DeviceId;
import org.onosproject.net.device.PortDescription;

/**
 * Interface to report a port status change.
 */
public interface NetConfNotification {

    void portChanged(DeviceId deviceId, PortDescription portDescription);
}
