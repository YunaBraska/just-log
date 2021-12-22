/*
 * Copyright 2016 Martin Winandy
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package berlin.yuna.logtest.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public final class SystemStreamCollector implements BeforeEachCallback, AfterEachCallback {

    private final ByteArrayOutputStream standardStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

    private final PrintStream originalStandardStream = System.out;
    private final PrintStream originalErrorStream = System.err;

    @Override
    public void beforeEach(final ExtensionContext extensionContext) {
        try {
            start();
        } finally {
            stop();
        }
    }

    public void start() {
        try {
            System.setOut(new PrintStream(standardStream, true, StandardCharsets.UTF_8.name()));
            System.setErr(new PrintStream(errorStream, true, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException ex) {
            // UTF-8 should be supported on all platforms
            throw new RuntimeException(ex);
        }
    }

    public void stop() {

    }

    @Override
    public void afterEach(final ExtensionContext extensionContext) {
        System.setOut(originalStandardStream);
        System.setErr(originalErrorStream);
        clear();
    }


    public String consumeStandardOutput() {
        final byte[] data;
        synchronized (standardStream) {
            data = standardStream.toByteArray();
            standardStream.reset();
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public String consumeErrorOutput() {
        final byte[] data;
        synchronized (errorStream) {
            data = errorStream.toByteArray();
            errorStream.reset();
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public void clear() {
        synchronized (standardStream) {
            standardStream.reset();
        }
        synchronized (errorStream) {
            errorStream.reset();
        }
    }
}
