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
package dev.morling.jmfrx.internal.profile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.morling.jmfrx.spi.EventProfileContributor.EventAttributeProfileBuilder;
import dev.morling.jmfrx.spi.EventProfileContributor.EventProfileBuilder;
import dev.morling.jmfrx.spi.ValueConverter;
import jdk.jfr.AnnotationElement;

public class EventProfileBuilderImpl implements EventProfileBuilder, EventAttributeProfileBuilder {

    private String currentEventProfileName;
    private Map<String, List<AttributeProfile>> eventProfiles;

    public EventProfileBuilderImpl() {
        eventProfiles = new HashMap<>();
    }

    @Override
    public EventAttributeProfileBuilder addAttributeProfile(String name, Class<?> type,
            AnnotationElement annotationElement, ValueConverter valueConverter) {

        eventProfiles.get(currentEventProfileName).add(new AttributeProfile(name, type, annotationElement, valueConverter));
        return this;
    }

    @Override
    public EventAttributeProfileBuilder addEventProfile(String mBeanName) {
        currentEventProfileName = mBeanName;
        eventProfiles.put(mBeanName, new ArrayList<>());
        return this;
    }

    public Map<String, EventProfile> getEventProfiles() {
        return eventProfiles.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> new EventProfile(e.getKey(), e.getValue())));
    }
}
