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
package org.onosproject.d.config;

import static com.google.common.base.Preconditions.checkArgument;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.Objects;

import org.onosproject.yang.model.DataNode;
import org.onosproject.yang.model.KeyLeaf;
import org.onosproject.yang.model.LeafListKey;
import org.onosproject.yang.model.ListKey;
import org.onosproject.yang.model.NodeKey;
import org.onosproject.yang.model.ResourceId;
import org.onosproject.yang.model.SchemaId;
import org.slf4j.Logger;

import com.google.common.annotations.Beta;

/**
 * Utility related to ResourceId.
 */
@Beta
public abstract class ResourceIds {

    private static final Logger log = getLogger(ResourceIds.class);

    /**
     * Absolute ResourceId pointing at root node.
     */
    public static final ResourceId ROOT_ID = ResourceId.builder()
            .addBranchPointSchema(DeviceResourceIds.ROOT_NAME,
                                  DeviceResourceIds.DCS_NAMESPACE)
            .build();

    /**
     * Builds the ResourceId of specified {@code node}.
     *
     * @param parent ResourceId of {@code node} parent
     * @param node to create ResourceId.
     * @return ResourceId of {@code node}
     */
    public static ResourceId resourceId(ResourceId parent, DataNode node) {
        final ResourceId.Builder builder;
        if (parent == null) {
            builder = ResourceId.builder();
        } else {
            try {
                builder = parent.copyBuilder();
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException(e);
            }
        }

        SchemaId sid = node.key().schemaId();
        switch (node.type()) {
        case MULTI_INSTANCE_LEAF_VALUE_NODE:
            builder.addLeafListBranchPoint(sid.name(), sid.namespace(),
                                           ((LeafListKey) node.key()).asString());
            break;

        case MULTI_INSTANCE_NODE:
            builder.addBranchPointSchema(sid.name(), sid.namespace());
            for (KeyLeaf keyLeaf : ((ListKey) node.key()).keyLeafs()) {
                builder.addKeyLeaf(keyLeaf.leafSchema().name(),
                                   keyLeaf.leafSchema().namespace(),
                                   keyLeaf.leafValAsString());
            }
            break;

        case SINGLE_INSTANCE_LEAF_VALUE_NODE:
        case SINGLE_INSTANCE_NODE:
            builder.addBranchPointSchema(sid.name(), sid.namespace());
            break;

        default:
            throw new IllegalArgumentException("Unknown type " + node);

        }

        return builder.build();
    }

    /**
     * Concats {@code path} after {@code prefix}.
     *
     * @param prefix path
     * @param path to append after {@code path}
     * @return concatenated ResouceId
     */
    public static ResourceId concat(ResourceId prefix, ResourceId path) {
        checkArgument(!path.nodeKeys().contains(DeviceResourceIds.ROOT_NODE),
                      "%s was already absolute path", path);
        try {
            return prefix.copyBuilder().append(path).build();
        } catch (CloneNotSupportedException e) {
            log.error("Could not copy {}", path, e);
            throw new IllegalArgumentException("Could not copy " + path, e);
        }
    }


    /**
     * Returns {@code child} as relative ResourceId against {@code base}.
     *
     * @param base ResourceId
     * @param child ResourceId to relativize
     * @return relative ResourceId
     */
    public static ResourceId relativize(ResourceId base, ResourceId child) {
        checkArgument(child.nodeKeys().size() >= base.nodeKeys().size(),
                      "%s path must be deeper than base prefix %s", child, base);
        @SuppressWarnings("rawtypes")
        Iterator<NodeKey> bIt = base.nodeKeys().iterator();
        @SuppressWarnings("rawtypes")
        Iterator<NodeKey> cIt = child.nodeKeys().iterator();
        while (bIt.hasNext()) {
            NodeKey<?> b = bIt.next();
            NodeKey<?> c = cIt.next();

            checkArgument(Objects.equals(b, c),
                          "%s is not a prefix of %s.\n" +
                          "b:%s != c:%s",
                          base, child,
                          b, c);
        }

        return ResourceId.builder().append(child.nodeKeys().subList(base.nodeKeys().size(),
                                                                    child.nodeKeys().size())).build();
    }

    /**
     * Tests if {@code child} starts with {@code prefix}.
     *
     * @param prefix expected
     * @param child to test
     * @return true if {@code child} starts with {@code prefix}
     */
    public static boolean isPrefix(ResourceId prefix, ResourceId child) {

        return child.nodeKeys().size() >= prefix.nodeKeys().size() &&
               prefix.nodeKeys().equals(child.nodeKeys().subList(0, prefix.nodeKeys().size()));
    }

    public static boolean startsWithRootNode(ResourceId path) {
        return !path.nodeKeys().isEmpty() &&
                DeviceResourceIds.ROOT_NAME.equals(path.nodeKeys().get(0).schemaId().name());
    }

}
