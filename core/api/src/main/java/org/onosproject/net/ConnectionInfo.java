/*
 * Copyright 2016 Open Networking Laboratory
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

package org.onosproject.net;

import org.onlab.packet.IpAddress;

//TODO: It is not a behavior. to be moved. Where?
public interface ConnectionInfo {

    /**
     * Returns the IP address.
     *
     * @return the IP address
     */
    IpAddress ip();

    /**
     * Returns the port.
     *
     * @return the port
     */
    int port();

    /**
     * Returns the username for authentication.
     *
     * @return the username
     */
    String name();

    /**
     * Returns the password for authentication.
     *
     * @return the password
     */
    String password();

    /**
     * Returns a String representation of the path to access the configuration
     * interface.
     *
     * @return the configuration base path
     */
    String getConfigurationBasePath();

    /**
     * Creates a String representation combining the base path with the sub-path.
     *
     * @param subpath the sub-path to be attached
     * @return the configuration path + sub-path
     */
    String getConfigurationPath(String subpath);

    /**
     * Returns a String representation of the path to access the configuration
     * interface.
     *
     * @return the notification base path
     */
    String getNotificationBasePath();

}
