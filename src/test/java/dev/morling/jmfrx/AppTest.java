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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.morling.jmfrx.event.JmxDumpEvent;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;
public class AppTest {

    @BeforeClass
    public static void registerEvent() {
        EventRegisterer.getInstance().registerEvent();
    }

    @AfterClass
    public static void unregisterEvent() {
        EventRegisterer.getInstance().unregisterEvent();
    }

    @Test
    public void shouldEmitRuntimeDumpEvent() throws Exception {
        String osEventName = "dev.morling.jfr.OperatingSystemDumpEvent";
        String runtimeEventName = "dev.morling.jfr.RuntimeDumpEvent";

        RecordingStream recordingStream = new RecordingStream();
        recordingStream.enable(JmxDumpEvent.NAME)
            .withPeriod(Duration.ofMillis(500))
            .with("filter", ".*OperatingSystem.*|.*Runtime.*");
        recordingStream.enable(osEventName);
        recordingStream.enable(runtimeEventName);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RecordedEvent> runtimeEvent = new AtomicReference<>();

        recordingStream.onEvent(JmxDumpEvent.NAME, event -> {
//            System.out.println(event);
        });

        recordingStream.onEvent(osEventName, event -> {
//            System.out.println(event);
        });

        recordingStream.onEvent(runtimeEventName, event -> {
            runtimeEvent.set(event);
            latch.countDown();
//            System.out.println(event);
            System.out.println("onevent: " + System.currentTimeMillis());
        });

        recordingStream.startAsync();
        latch.await(5, TimeUnit.SECONDS);

        assertThat(runtimeEvent.get()).isNotNull();
        assertThat(runtimeEvent.get().getLong("Pid")).isEqualTo(ProcessHandle.current().pid());

        recordingStream.close();
    }
}
