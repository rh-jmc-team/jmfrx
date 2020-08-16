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
package dev.morling.jmfrx.event;

import jdk.jfr.Category;
import jdk.jfr.Description;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;
import jdk.jfr.SettingDefinition;
import jdk.jfr.StackTrace;

@Name(JmxDumpEvent.NAME)
@Label("JMX Dump")
@Category("JMX")
@Description("Periodically dumps specific JMX MBeans")
@StackTrace(false)
@Period("60 s")
public class JmxDumpEvent extends Event {

    public static final String NAME = "dev.morling.jfr.JmxDumpEvent";

    @Label("Captured MBean Names")
    public String capturedMbeans;

    @Label("Captured Evens")
    public int eventCount;

    public JmxMbeansFilterControl filter;

    @Label("JMX MBeans Name Filter")
    @SettingDefinition
    protected boolean filter(JmxMbeansFilterControl filter) {
        this.filter = filter;
        return true;
    }
}
