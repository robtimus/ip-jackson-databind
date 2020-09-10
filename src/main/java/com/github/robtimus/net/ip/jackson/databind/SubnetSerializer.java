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
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPAddressFormatter;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Range;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

/**
 * Base class for all serializers for {@link Subnet} and sub types.
 *
 * @author Rob Spoor
 * @param <S> The type of subnet to serialize.
 */
public abstract class SubnetSerializer<S extends Subnet<?>> extends JsonSerializer<S> {

    private SubnetSerializer() {
    }

    @Override
    public void serialize(S value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(format(value));
    }

    abstract String format(S value);

    @Override
    public abstract Class<S> handledType();

    /**
     * A serializer for {@link IPv4Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends SubnetSerializer<IPv4Subnet> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Subnet} serializer.
         */
        public IPv4() {
            super();
        }

        @Override
        String format(IPv4Subnet value) {
            return value.toString();
        }

        @Override
        public Class<IPv4Subnet> handledType() {
            return IPv4Subnet.class;
        }
    }

    /**
     * A serializer for {@link IPv6Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends SubnetSerializer<IPv6Subnet> {

        static final IPv6 INSTANCE = new IPv6(null);

        private final IPAddressFormatter<? super IPv6Address> formatter;

        /**
         * Creates a new {@link IPv6Subnet} serializer.
         *
         * @param formatter The formatter to use for the from and to addresses. If {@code null}, {@link IPv6Subnet#toString()} will be used instead.
         */
        public IPv6(IPAddressFormatter<? super IPv6Address> formatter) {
            this.formatter = formatter;
        }

        @Override
        String format(IPv6Subnet value) {
            return formatter != null ? formatter.format(value.routingPrefix()) + "/" + value.prefixLength() : value.toString(); //$NON-NLS-1$
        }

        @Override
        public Class<IPv6Subnet> handledType() {
            return IPv6Subnet.class;
        }
    }

    /**
     * A serializer for {@link Subnet}. It can handle both {@link IPv4Subnet} and {@link IPv6Subnet}.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends SubnetSerializer<Subnet<?>> {

        static final AnyVersion INSTANCE = new AnyVersion(null);

        private final IPAddressFormatter<? super IPAddress<?>> formatter;

        /**
         * Creates a new {@link Subnet} serializer.
         *
         * @param formatter The formatter to use for the from and to addresses. If {@code null}, {@link Subnet#toString()} will be used instead.
         */
        public AnyVersion(IPAddressFormatter<? super IPAddress<?>> formatter) {
            this.formatter = formatter;
        }

        @Override
        String format(Subnet<?> value) {
            return formatter != null ? formatter.format(value.routingPrefix()) + "/" + value.prefixLength() : value.toString(); //$NON-NLS-1$
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<Subnet<?>> handledType() {
            return (Class<Subnet<?>>) (Class<?>) Subnet.class;
        }
    }
}
