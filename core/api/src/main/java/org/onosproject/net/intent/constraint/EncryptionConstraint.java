/*
 * Copyright 2016-present Open Networking Laboratory
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

package org.onosproject.net.intent.constraint;

import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.ResourceContext;

import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;


/**
 * Encryption constrain.
 */
public class EncryptionConstraint implements Constraint {


    @Override
    public double cost(Link link, ResourceContext context) {
        return 1;
    }

    @Override
    public boolean validate(Path path, ResourceContext context) {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
