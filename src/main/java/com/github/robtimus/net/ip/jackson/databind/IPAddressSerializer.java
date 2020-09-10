/*
 * IPAddressSerializer.java
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
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv6Address;

/**
 * Base class for all serializers for {@link IPAddress} and sub classes.
 *
 * @author Rob Spoor
 * @param <I> The type of IP address to serialize.
 */
public abstract class IPAddressSerializer<I extends IPAddress<?>> extends JsonSerializer<I> {

    private final IPAddressFormatter<? super I> formatter;

    private IPAddressSerializer(IPAddressFormatter<? super I> formatter) {
        this.formatter = formatter;
    }

    @Override
    public void serialize(I value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(format(value));
    }

    private String format(I value) {
        return formatter != null ? formatter.format(value) : value.toString();
    }

    @Override
    public abstract Class<I> handledType();

    /**
     * A serializer for {@link IPv4Address}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends IPAddressSerializer<IPv4Address> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Address} serializer.
         */
        public IPv4() {
            super(null);
        }

        @Override
        public Class<IPv4Address> handledType() {
            return IPv4Address.class;
        }
    }

    /**
     * A serializer for {@link IPv4Address}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends IPAddressSerializer<IPv6Address> {

        static final IPv6 INSTANCE = new IPv6(null);

        /**
         * Creates a new {@link IPv6Address} serializer.
         *
         * @param formatter The formatter to use. If {@code null}, {@link IPv6Address#toString()} will be used instead.
         */
        public IPv6(IPAddressFormatter<? super IPv6Address> formatter) {
            super(formatter);
        }

        @Override
        public Class<IPv6Address> handledType() {
            return IPv6Address.class;
        }
    }

    /**
     * A serializer for {@link IPAddress}. It can handle both {@link IPv4Address} and {@link IPv6Address}.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends IPAddressSerializer<IPAddress<?>> {

        static final AnyVersion INSTANCE = new AnyVersion(null);

        /**
         * Creates a new {@link IPAddress} serializer.
         *
         * @param formatter The formatter to use. If {@code null}, {@link IPAddress#toString()} will be used instead.
         */
        public AnyVersion(IPAddressFormatter<? super IPAddress<?>> formatter) {
            super(formatter);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<IPAddress<?>> handledType() {
            return (Class<IPAddress<?>>) (Class<?>) IPAddress.class;
        }
    }
}
