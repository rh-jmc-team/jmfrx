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
package dev.morling.jmfrx;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import dev.morling.jmfrx.descriptor.AttributeDescriptor;
import dev.morling.jmfrx.descriptor.EventDescriptor;
import dev.morling.jmfrx.event.JmxDumpEvent;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;

public class EventRegisterer {

    private final ConcurrentMap<String, EventDescriptor> factories;
    private final ConcurrentMap<Pattern, List<String>> matchingBeanNames;

    private Runnable hook;

    private enum Holder {

        INSTANCE;

        private EventRegisterer registerer = new EventRegisterer();

        EventRegisterer getRegisterer() {
            return registerer;
        }
    }

    public EventRegisterer() {
        factories = new ConcurrentHashMap<>();
        matchingBeanNames = new ConcurrentHashMap<>();
    }

    public static EventRegisterer getInstance() {
        return Holder.INSTANCE.getRegisterer();
    }

    public void registerEvent() {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        hook = () -> {
            JmxDumpEvent dumpEvent = new JmxDumpEvent();

            if (!dumpEvent.isEnabled()) {
                return;
            }

            dumpEvent.begin();

            // sets the filter as a side-effect...
            dumpEvent.shouldCommit();

            try {
                List<String> mBeanNames = matchingBeanNames.computeIfAbsent(dumpEvent.filter.getPattern(), this::getMatchingObjectNames);

                for (String mBeanName : mBeanNames) {
                    EventDescriptor descriptor = factories.computeIfAbsent(mBeanName, EventDescriptor::getDescriptorFor);

                    Event event = descriptor.getFactory().newEvent();
                    event.begin();

                    List<Attribute> attributeValues = mbeanServer.getAttributes(new ObjectName(mBeanName), descriptor.getAttributeNames())
                            .asList();

                    for(Attribute attribute : attributeValues) {
                        AttributeDescriptor attributeDescriptor = descriptor.getAttribute(attribute.getName());
                        event.set(attributeDescriptor.getIndex(), attributeDescriptor.getValue(attribute.getValue()));
                    }

                    event.commit();
                }

                dumpEvent.capturedMbeans = String.join(", ", mBeanNames);
                dumpEvent.eventCount = mBeanNames.size();
            }
            catch (InstanceNotFoundException | ReflectionException | MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }

            dumpEvent.commit();
        };
        FlightRecorder.addPeriodicEvent(JmxDumpEvent.class, hook);
    }

    public void unregisterEvent() {
        FlightRecorder.removePeriodicEvent(hook);
    }

    private List<String> getMatchingObjectNames(Pattern pattern) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        return mbeanServer.queryMBeans(null, null)
            .stream()
            .filter(oi -> pattern.matcher(oi.getObjectName().toString()).matches())
            .map(ObjectInstance::getObjectName)
            .map(ObjectName::toString)
            .collect(Collectors.toList());
    }
}
