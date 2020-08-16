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
package dev.morling.jmfrx.descriptor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jdk.jfr.EventFactory;

public class EventDescriptor {

    private final EventFactory factory;
    private final Map<String, AttributeDescriptor> attributes;
    private String[] attributeNames;

    public EventDescriptor(EventFactory factory, List<AttributeDescriptor> attributes) {
        this.factory = factory;
        this.attributes = attributes.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ad -> ad.getValueDescriptor().getName(),
                        ad -> ad)
                );

                Collections.unmodifiableList(attributes);
        this.attributeNames = attributes.stream()
                .map(ad -> ad.getValueDescriptor().getName())
                .toArray(String[]::new);
    }

    public EventFactory getFactory() {
        return factory;
    }

    public AttributeDescriptor getAttribute(String name) {
        return attributes.get(name);
    }

    public String[] getAttributeNames() {
        return attributeNames;
    }
}
