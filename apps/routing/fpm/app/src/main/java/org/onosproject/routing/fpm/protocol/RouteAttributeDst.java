/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onosproject.routing.fpm.protocol;

import com.google.common.base.MoreObjects;
import org.onlab.packet.DeserializationException;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip6Address;
import org.onlab.packet.IpAddress;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Destination address route attribute.
 */
public final class RouteAttributeDst extends RouteAttribute {

    private final IpAddress dstAddress;

    /**
     * Class constructor.
     *
     * @param length length
     * @param type type
     * @param dstAddress destination address
     */
    public RouteAttributeDst(int length, int type, IpAddress dstAddress) {
        super(length, type);

        this.dstAddress = dstAddress;
    }

    /**
     * Returns the destination IP address.
     *
     * @return destination IP address
     */
    public IpAddress dstAddress() {
        return dstAddress;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("type", type())
                .add("length", length())
                .add("dstAddress", dstAddress)
                .toString();
    }

    /**
     * Returns a decoder for a destination address route attribute.
     *
     * @return destination address route attribute decoder
     */
    public static RouteAttributeDecoder<RouteAttributeDst> decoder() {
        return (int length, int type, byte[] value) -> {

            IpAddress dstAddress;
            if (value.length == Ip4Address.BYTE_LENGTH) {
                dstAddress = IpAddress.valueOf(IpAddress.Version.INET, value);
            } else if (value.length == Ip6Address.BYTE_LENGTH) {
                dstAddress = IpAddress.valueOf(IpAddress.Version.INET6, value);
            } else {
                throw new DeserializationException("Invalid address length");
            }

            return new RouteAttributeDst(length, type, dstAddress);
        };
    }

    /**
     * Encode the RouteAttributeDst contents into the ChannelBuffer.
     *
     * @param cb channelbuffer to be filled in
     */
    @Override
    public void encode(ChannelBuffer cb) {

        cb.writeShort(Short.reverseBytes((short) length()));
        cb.writeShort(Short.reverseBytes((short) type()));

        ChannelBuffer buffer =  ChannelBuffers.copiedBuffer(dstAddress.toOctets());
        if (length() == Ip6Address.BYTE_LENGTH +
                RouteAttribute.ROUTE_ATTRIBUTE_HEADER_LENGTH) {
            cb.writeBytes(buffer, Ip6Address.BYTE_LENGTH);
        } else if (length() == Ip4Address.BYTE_LENGTH +
                RouteAttribute.ROUTE_ATTRIBUTE_HEADER_LENGTH) {
            cb.writeBytes(buffer, Ip4Address.BYTE_LENGTH);
        } else {
            throw new RuntimeException("Dst address length incorrect!");
        }
    }
}
