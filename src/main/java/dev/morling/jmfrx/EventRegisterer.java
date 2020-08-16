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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import dev.morling.jmfrx.descriptor.AttributeDescriptor;
import dev.morling.jmfrx.descriptor.EventDescriptor;
import dev.morling.jmfrx.event.JmxDumpEvent;
import dev.morling.jmfrx.profile.AttributeProfile;
import dev.morling.jmfrx.profile.EventProfile;
import jdk.jfr.AnnotationElement;
import jdk.jfr.Category;
import jdk.jfr.DataAmount;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.EventFactory;
import jdk.jfr.FlightRecorder;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;
import jdk.jfr.ValueDescriptor;

public class EventRegisterer {

    private static final String EVENT_TYPE_NAME_PREFIX = "dev.morling.jfr.";
    private static final String EVENT_TYPE_NAME_SUFFIX = "DumpEvent";

    private final ConcurrentMap<String, EventProfile> profiles;
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
        profiles = new ConcurrentHashMap<>();

        Map<String, AttributeProfile> attributeProfiles = new HashMap<>();

        attributeProfiles.put("OpenFileDescriptorCount", new AttributeProfile("OpenFileDescriptorCount", int.class, null, v -> v));
        attributeProfiles.put("TotalSwapSpaceSize", new AttributeProfile("TotalSwapSpaceSize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v));
        attributeProfiles.put("FreeSwapSpaceSize", new AttributeProfile("FreeSwapSpaceSize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v));
        attributeProfiles.put("FreeMemorySize", new AttributeProfile("FreeMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v));
        attributeProfiles.put("TotalMemorySize", new AttributeProfile("TotalMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v));
        EventProfile operatingSystemProfile = new EventProfile("java.lang:type=OperatingSystem", attributeProfiles);
        profiles.put("java.lang:type=OperatingSystem", operatingSystemProfile);

//        attributeProfiles = new HashMap<>();
//        attributeProfiles.put("StartTime", new AttributeProfile("StartTime", "StartTime_", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v));
//        EventProfile runtimeProfile = new EventProfile("java.lang:type=Runtime", attributeProfiles);
//        profiles.put("java.lang:type=Runtime", runtimeProfile);

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
                    EventDescriptor descriptor = factories.computeIfAbsent(mBeanName, this::getEventDescriptor);

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

    private EventDescriptor getEventDescriptor(String mBeanName) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName objectName = new ObjectName(mBeanName);
            MBeanInfo mBeanInfo = mbeanServer.getMBeanInfo(objectName);

            List<AnnotationElement> eventAnnotations = Arrays.asList(
                    new AnnotationElement(Category.class, getCategory(mBeanName)),
                    new AnnotationElement(StackTrace.class, false),
                    new AnnotationElement(Name.class, getName(mBeanName)),
                    new AnnotationElement(Label.class, getLabel(mBeanName)),
                    new AnnotationElement(Description.class,  mBeanInfo.getDescription())
            );

            List<AttributeDescriptor> fields = getFields(objectName, mBeanInfo);

            List<ValueDescriptor> valueDescriptors = fields.stream()
                    .map(AttributeDescriptor::getValueDescriptor)
                    .collect(Collectors.toList());

            return new EventDescriptor(EventFactory.create(eventAnnotations, valueDescriptors), fields);
        }
        catch (IntrospectionException | InstanceNotFoundException | MalformedObjectNameException | ReflectionException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getCategory(String mBeanName) {
        String[] parts = mBeanName.split(":");
        return new String [] { "JMX", parts[0] };
    }

    private String getName(String mBeanName) {
        return EVENT_TYPE_NAME_PREFIX + getLabel(mBeanName) + EVENT_TYPE_NAME_SUFFIX;
    }

    private String getLabel(String mBeanName) {
        String[] parts = mBeanName.split("=");
        return parts[1];
    }

    private List<AttributeDescriptor> getFields(ObjectName objectName, MBeanInfo mBeanInfo) {
        List<AttributeDescriptor> fields = new ArrayList<>();
        MBeanAttributeInfo[] attibuteInfos = mBeanInfo.getAttributes();
        EventProfile profile = profiles.get(objectName.toString());
        int i = 0;

        for (MBeanAttributeInfo attr : attibuteInfos) {
            List<AnnotationElement> fieldAnnotations = new ArrayList<>();
            fieldAnnotations.add(new AnnotationElement(Label.class, attr.getName()));
            fieldAnnotations.add(new AnnotationElement(Description.class, attr.getDescription()));

            Class<?> type = null;
            ValueConverter valueConverter = null;
            String name = attr.getName();

            if (profile != null) {
                AttributeProfile attributeProfile = profile.attributeProfiles.get(attr.getName());
                if (attributeProfile != null) {
                    if (attributeProfile.annotationElement != null) {
                        fieldAnnotations.add(attributeProfile.annotationElement);
                    }
                    type = attributeProfile.type;
                    valueConverter = attributeProfile.valueConverter;
                    name = attributeProfile.name;
                }
            }
            if (type == null) {
                if (attr.getType().equals(long.class.getName())) {
                    type = long.class;
                    valueConverter = v -> v;
                }
                else if (attr.getType().equals(boolean.class.getName())) {
                    type = boolean.class;
                    valueConverter = v -> v;
                }
                else if (attr.getType().equals(int.class.getName())) {
                    type = int.class;
                    valueConverter = v -> v;
                }
                else if (attr.getType().equals(double.class.getName())) {
                    type = double.class;
                    valueConverter = v -> v;
                }
                else {
                    type = String.class;
                    valueConverter = v -> String.valueOf(v);
                }
            }

            fields.add(new AttributeDescriptor(
                    i++,
                    new ValueDescriptor(type, name, fieldAnnotations),
                    valueConverter)
            );
        }

        return fields;
    }
}
