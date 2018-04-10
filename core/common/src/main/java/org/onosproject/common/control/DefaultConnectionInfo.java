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

package org.onosproject.common.control;

import com.google.common.base.Preconditions;
import org.onlab.packet.IpAddress;
import org.onosproject.net.ConnectionInfo;

import java.util.Objects;

/**
 * The default implementation for the {@link ConnectionInfo} interface.
 */
public class DefaultConnectionInfo implements ConnectionInfo {

    private final IpAddress ip;
    private final int port;
    private final String name;
    private final String password;
    private final String configurationBasePath;
    private final String notificationBasePath;

    /**
     * Constructor for a default connection info.
     *
     * @param ip                   the IP address of the connection endpoint
     * @param port                 the port of the connection endpoint
     * @param name                 the username for the login
     * @param password             the password for the login
     * @param configProtocol       the configuration protocol, e.g. http
     * @param configPath           the configuration path, e.g. data
     * @param notificationProtocol the notification protocol, e.g. ws
     * @param notificationPath     the notification path, e.g. streams
     */
    public DefaultConnectionInfo(IpAddress ip, int port, String name,
                                 String password, String configProtocol,
                                 String configPath, String notificationProtocol,
                                 String notificationPath) {
        this.ip = Preconditions.checkNotNull(ip, "An IP address has to be present");
        Preconditions.checkArgument(port > 0, "A port number cannot be zero or negative");
        this.port = port;
        // login data
        this.name = name;
        this.password = password;
        // the configuration protocol has to be available
        configProtocol = Preconditions.checkNotNull(configProtocol, "A protocol has to be present");
        Preconditions.checkArgument(!"".equals(configProtocol), "An empty protocol is not allowed.");
        // create the configuration base path
        this.configurationBasePath = getBasePath(configProtocol, configPath);
        // the notification base path is not mandatory and is only evaluated if
        // a protocol is available
        if (notificationProtocol != null && !"".equals(notificationProtocol)) {
            this.notificationBasePath = getBasePath(notificationProtocol, notificationPath);
        } else {
            this.notificationBasePath = null;
        }
    }

    /**
     * Constructor for a default connection info copying another info.
     *
     * @param info the info to be copied.
     */
    public DefaultConnectionInfo(ConnectionInfo info) {
        this.ip = info.ip();
        this.port = info.port();
        this.name = info.name();
        this.password = info.password();
        this.configurationBasePath = info.getConfigurationBasePath();
        this.notificationBasePath = info.getNotificationBasePath();
    }

    /**
     * Creates the base path based on the protocol and the path.
     *
     * @param protocol the protocol to be used for the base path
     * @param path     the path to be appended to the path
     * @return the base path string
     */
    private String getBasePath(String protocol, String path) {
        // create base path
        StringBuilder sb = new StringBuilder();
        sb.append(protocol);
        sb.append("://");
        sb.append(ip);
        sb.append(":");
        sb.append(port);
        // a missing path indicates that nothing needs to be appended
        if (path != null && !"".equals(path)) {
            // add a slash if needed
            if (!path.startsWith("/")) {
                sb.append("/");
            }
            // remove trailing slash
            if (path.endsWith("/")) {
                sb.append(path.substring(0, path.length() - 1));
            } else {
                sb.append(path);
            }
        }
        return sb.toString();
    }

    @Override
    public IpAddress ip() {
        return ip;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String getConfigurationBasePath() {
        return configurationBasePath;
    }

    @Override
    public String getConfigurationPath(String subpath) {
        if (subpath == null) {
            return getConfigurationBasePath();
        }
        // attach the sub-path
        if (subpath.startsWith("/")) {
            return configurationBasePath + subpath;
        } else {
            return configurationBasePath + "/" + subpath;
        }
    }

    @Override
    public String getNotificationBasePath() {
        return notificationBasePath;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port, name, password, configurationBasePath,
                            notificationBasePath);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultConnectionInfo)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        DefaultConnectionInfo that = (DefaultConnectionInfo) obj;
        return Objects.equals(this.ip, that.ip) && this.port == that.port
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.password, that.password)
                && Objects.equals(this.configurationBasePath, that.configurationBasePath)
                && Objects.equals(this.notificationBasePath, that.notificationBasePath);
    }

    @Override
    public String toString() {
        return "DefaultConnectionInfo [ip=" + ip + ", port=" + port + ", name="
                + name + ", password=" + password + ", configurationBasePath="
                + configurationBasePath + ", notificationBasePath="
                + notificationBasePath + "]";
    }

}
