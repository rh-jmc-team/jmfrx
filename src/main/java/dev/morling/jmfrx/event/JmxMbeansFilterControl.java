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

import java.util.Set;
import java.util.regex.Pattern;

import jdk.jfr.SettingControl;

public class JmxMbeansFilterControl extends SettingControl {

    private Pattern pattern = Pattern.compile("");

    @Override
    public void setValue(String value) {
        System.out.println("SetValue #### " + value);
        this.pattern = value != null ? Pattern.compile(value) : null;
    }

    @Override
    public String combine(Set<String> values) {
        System.out.println("Combine ##### " + values);
        return String.join("|", values);
    }

    @Override
    public String getValue() {
        System.out.println("GetValue ##### " + pattern);
        return pattern == null ? null : pattern.toString();
    }

    public boolean matches(String s) {
        System.out.println("Matches #### " + pattern + " " + s);
        return pattern != null && pattern.matcher(s).matches();
    }

    public Pattern getPattern() {
        return pattern;
    }
}
