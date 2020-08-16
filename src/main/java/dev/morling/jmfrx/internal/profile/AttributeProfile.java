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

import dev.morling.jmfrx.spi.ValueConverter;
import jdk.jfr.AnnotationElement;

public class AttributeProfile {

    public final String name;
    public final Class<?> type;
    public final AnnotationElement annotationElement;
    public final ValueConverter valueConverter;

    public AttributeProfile(String name, Class<?> type, AnnotationElement annotationElement) {
        this(name, type, annotationElement, null);
    }

    public AttributeProfile(String name, Class<?> type, AnnotationElement annotationElement, ValueConverter valueConverter) {
        this.name = name;
        this.type = type;
        this.annotationElement = annotationElement;
        this.valueConverter = valueConverter;
    }
}
