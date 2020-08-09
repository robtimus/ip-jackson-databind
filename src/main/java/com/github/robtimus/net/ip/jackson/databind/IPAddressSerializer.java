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
import java.util.Objects;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPAddressFormatter;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv6Address;

/**
 * Base class for JSON serializers for IP addresses.
 *
 * @author Rob Spoor
 * @param <IP> The supported type of IP address.
 */
public abstract class IPAddressSerializer<IP extends IPAddress<?>> extends JsonSerializer<IP> {

    private final IPAddressFormatter<IP> formatter;

    /**
     * Creates a new IP address serializer.
     *
     * @param formatter The formatter to use for parsing strings into IP addresses.
     */
    protected IPAddressSerializer(IPAddressFormatter<IP> formatter) {
        this.formatter = Objects.requireNonNull(formatter);
    }

    @Override
    public void serialize(IP value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(formatter.format(value));
    }

    @Override
    public abstract Class<IP> handledType();

    /**
     * Returns a JSON serializer for {@link IPv4Address}.
     *
     * @return A JSON serializer for {@link IPv4Address}.
     */
    public static IPAddressSerializer<IPv4Address> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends IPAddressSerializer<IPv4Address> {

        private static final IPv4 INSTANCE = new IPv4();

        private IPv4() {
            super(IPAddressFormatter.ipv4());
        }

        @Override
        public Class<IPv4Address> handledType() {
            return IPv4Address.class;
        }
    }

    /**
     * Returns a JSON serializer for {@link IPv6Address}.
     *
     * @return A JSON serializer for {@link IPv6Address}.
     */
    public static IPAddressSerializer<IPv6Address> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends IPAddressSerializer<IPv6Address> {

        private static final IPv6 INSTANCE = new IPv6();

        private IPv6() {
            super(IPAddressFormatter.ipv6WithDefaults());
        }

        @Override
        public Class<IPv6Address> handledType() {
            return IPv6Address.class;
        }
    }

    /**
     * Returns a JSON serializer for {@link IPAddress} of any type.
     *
     * @return A JSON serializer for {@link IPAddress} of any type.
     */
    public static IPAddressSerializer<IPAddress<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends IPAddressSerializer<IPAddress<?>> {

        private static final AnyVersion INSTANCE = new AnyVersion();

        private AnyVersion() {
            super(IPAddressFormatter.anyVersionWithDefaults());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<IPAddress<?>> handledType() {
            return (Class<IPAddress<?>>) (Class<?>) IPAddress.class;
        }
    }
}
