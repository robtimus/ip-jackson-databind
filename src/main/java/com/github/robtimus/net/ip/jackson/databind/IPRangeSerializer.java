/*
 * IPRangeSerializer.java
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
import com.github.robtimus.net.ip.IPRange;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv6Range;

abstract class IPRangeSerializer<R extends IPRange<?>> extends JsonSerializer<R> {

    static final String FROM_FIELD_NAME = "from"; //$NON-NLS-1$
    static final String TO_FIELD_NAME = "to"; //$NON-NLS-1$

    @Override
    public void serialize(R value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(FROM_FIELD_NAME, value.from().toString());
        gen.writeStringField(TO_FIELD_NAME, value.to().toString());
        gen.writeEndObject();
    }

    @Override
    public abstract Class<R> handledType();

    static IPRangeSerializer<IPv4Range> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends IPRangeSerializer<IPv4Range> {

        private static final IPv4 INSTANCE = new IPv4();

        @Override
        public Class<IPv4Range> handledType() {
            return IPv4Range.class;
        }
    }

    static IPRangeSerializer<IPv6Range> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends IPRangeSerializer<IPv6Range> {

        private static final IPv6 INSTANCE = new IPv6();

        @Override
        public Class<IPv6Range> handledType() {
            return IPv6Range.class;
        }
    }

    static IPRangeSerializer<IPRange<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends IPRangeSerializer<IPRange<?>> {

        private static final AnyVersion INSTANCE = new AnyVersion();

        @Override
        @SuppressWarnings("unchecked")
        public Class<IPRange<?>> handledType() {
            return (Class<IPRange<?>>) (Class<?>) IPRange.class;
        }
    }
}
