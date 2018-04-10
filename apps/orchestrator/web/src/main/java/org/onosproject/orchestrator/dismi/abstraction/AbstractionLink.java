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

package org.onosproject.orchestrator.dismi.abstraction;

import com.google.common.base.MoreObjects;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;

/**
 * Created by aghafoor on 2017-08-14.
 */
public class AbstractionLink {

    private EndPoint src;
    private EndPoint dst;

    public EndPoint getSrc() {
        return src;
    }

    public void setSrc(EndPoint src) {
        this.src = src;
    }

    public EndPoint getDst() {
        return dst;
    }

    public void setDst(EndPoint dst) {
        this.dst = dst;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractionLink that = (AbstractionLink) o;
        return java.util.Objects.equals(src, that.src) &&
                java.util.Objects.equals(dst, that.dst);
    }

    @Override
    public int hashCode() {

        return java.util.Objects.hash(src, dst);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("src", src)
                .add("dst", dst)
                .toString();
    }
}
