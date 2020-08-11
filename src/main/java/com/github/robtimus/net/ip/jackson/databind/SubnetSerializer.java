/*
 * SubnetSerializer.java
 * Copyright 2020 Rob Spoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.robtimus.net.ip.jackson.databind;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

abstract class SubnetSerializer<S extends Subnet<?>> extends JsonSerializer<S> {

    @Override
    public void serialize(S value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString());
    }

    @Override
    public abstract Class<S> handledType();

    static SubnetSerializer<IPv4Subnet> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends SubnetSerializer<IPv4Subnet> {

        private static final IPv4 INSTANCE = new IPv4();

        @Override
        public Class<IPv4Subnet> handledType() {
            return IPv4Subnet.class;
        }
    }

    static SubnetSerializer<IPv6Subnet> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends SubnetSerializer<IPv6Subnet> {

        private static final IPv6 INSTANCE = new IPv6();

        @Override
        public Class<IPv6Subnet> handledType() {
            return IPv6Subnet.class;
        }
    }

    static SubnetSerializer<Subnet<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends SubnetSerializer<Subnet<?>> {

        private static final AnyVersion INSTANCE = new AnyVersion();

        @Override
        @SuppressWarnings("unchecked")
        public Class<Subnet<?>> handledType() {
            return (Class<Subnet<?>>) (Class<?>) Subnet.class;
        }
    }
}
