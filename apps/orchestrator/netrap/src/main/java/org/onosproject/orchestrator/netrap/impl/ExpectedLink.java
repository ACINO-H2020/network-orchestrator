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

package org.onosproject.orchestrator.netrap.impl;

import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Link;
import org.onosproject.net.driver.Behaviour;
import org.onosproject.net.provider.ProviderId;

import static com.google.common.base.MoreObjects.toStringHelper;

public class ExpectedLink implements Link {
    ConnectPoint src;
    ConnectPoint dst;
    DefaultAnnotations annotations;

    public ExpectedLink(ConnectPoint src, ConnectPoint dst, DefaultAnnotations annotations) {
        this.src = src;
        this.dst = dst;
        this.annotations = annotations;
    }

    @Override
    public DefaultAnnotations annotations() {
        return annotations;
    }

    @Override
    public ProviderId providerId() {
        return ProviderId.NONE;
    }

    @Override
    public <B extends Behaviour> B as(Class<B> projectionClass) {
        return null;
    }

    @Override
    public <B extends Behaviour> boolean is(Class<B> projectionClass) {
        return false;
    }

    @Override
    public ConnectPoint src() {
        return src;
    }

    @Override
    public ConnectPoint dst() {
        return dst;
    }

    @Override
    public Type type() {
        return Type.INDIRECT;
    }

    @Override
    public State state() {
        return State.INACTIVE;
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public boolean isExpected() {
        return true;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("src", src)
                .add("dst", dst)
                .add("type", Type.VIRTUAL)
                .add("state", State.INACTIVE)
                .add("expected", true)
                .toString();
    }
}
