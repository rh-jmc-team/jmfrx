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

import dev.morling.jmfrx.spi.EventProfileContributor;
import jdk.jfr.AnnotationElement;
import jdk.jfr.DataAmount;
import jdk.jfr.Percentage;
import jdk.jfr.Timespan;
import jdk.jfr.Timestamp;

/**
 * Contributes the event profiles for the java.lang.* MBeans.
 *
 * @author Gunnar Morling
 */
public class JavaLangEventProfileContributor implements EventProfileContributor {

    @Override
    public void contributeProfiles(EventProfileBuilder builder) {
        builder.addEventProfile("java.lang:type=OperatingSystem")
                .addAttributeProfile("OpenFileDescriptorCount", int.class, null, v -> v)
                .addAttributeProfile("TotalSwapSpaceSize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("FreeSwapSpaceSize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("FreeMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("TotalMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("CommittedVirtualMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("FreePhysicalMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("TotalPhysicalMemorySize", long.class, new AnnotationElement(DataAmount.class, DataAmount.BYTES), v -> v)
                .addAttributeProfile("CpuLoad", double.class, new AnnotationElement(Percentage.class), v -> v)
                .addAttributeProfile("ProcessCpuLoad", double.class, new AnnotationElement(Percentage.class), v -> v)
                .addAttributeProfile("SystemCpuLoad", double.class, new AnnotationElement(Percentage.class), v -> v)
                .addAttributeProfile("ProcessCpuTime", long.class, new AnnotationElement(Timespan.class, Timespan.NANOSECONDS), v -> v )
            .addEventProfile("java.lang:type=Runtime")
                .addAttributeProfile("StartTime", long.class, new AnnotationElement(Timestamp.class, Timestamp.MILLISECONDS_SINCE_EPOCH), v -> v )
                .addAttributeProfile("Uptime", long.class, new AnnotationElement(Timespan.class, Timespan.SECONDS),
                        v -> (long) v * 60);
    }
}
