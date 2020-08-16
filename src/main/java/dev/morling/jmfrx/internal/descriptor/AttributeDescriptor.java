/**
 *  Copyright 2020 The JMFRX authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package dev.morling.jmfrx.internal.descriptor;

import dev.morling.jmfrx.spi.ValueConverter;
import jdk.jfr.ValueDescriptor;

public class AttributeDescriptor {

    private final int index;
    private final ValueDescriptor valueDescriptor;
    private final ValueConverter valueConverter;

    public AttributeDescriptor(int index, ValueDescriptor valueDescriptor, ValueConverter valueConverter) {
        this.index = index;
        this.valueDescriptor = valueDescriptor;
        this.valueConverter = valueConverter;
    }

    public int getIndex() {
        return index;
    }

    public ValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    public Object getValue(Object sourceValue) {
        if (sourceValue == null) {
            return null;
        }

        return valueConverter.getValue(sourceValue);
    }
}
