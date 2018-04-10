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

package org.onosproject.net.intent.constraint;

import org.onosproject.net.Link;
import org.onosproject.net.RestorationType;
import org.onosproject.net.intent.ResourceContext;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A constraint to describe the type of restoration to be applied
 * in case of failure.
 */
public class RestorationConstraint extends BooleanConstraint {

    private RestorationType restorationType;

    public RestorationConstraint(RestorationType restorationType) {
        checkNotNull(restorationType, "The type of restoration cannot be null");
        this.restorationType = restorationType;
    }

    /**
     * Returns the restoration type required by this constraint.
     *
     * @return restorationType
     */
    public RestorationType restorationType() {
        return restorationType;
    }


    @Override
    public boolean isValid(Link link, ResourceContext context) {
        return true;
    }

    @Override
    public int hashCode() {
        return restorationType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RestorationConstraint other = (RestorationConstraint) obj;
        return this.restorationType() == other.restorationType();
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("Restoration", restorationType).toString();
    }


}
