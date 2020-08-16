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
package dev.morling.jmfrx.spi;

import jdk.jfr.AnnotationElement;

/**
 * Implementations contribute event profiles for one or more MBean, which
 * describe how the MBean and its attributes should be mapped into a
 * corresponding JFR event.
 *
 * @author Gunnar Morling
 */
public interface EventProfileContributor {

    void contributeProfiles(EventProfileBuilder builder);

    public interface EventProfileBuilder {
        EventAttributeProfileBuilder addEventProfile(String mBeanName);
    }

    public interface EventAttributeProfileBuilder {
        EventAttributeProfileBuilder addAttributeProfile(String name, Class<?> type, AnnotationElement annotationElement, ValueConverter valueConverter);
        EventAttributeProfileBuilder addEventProfile(String mBeanName);
    }
}
