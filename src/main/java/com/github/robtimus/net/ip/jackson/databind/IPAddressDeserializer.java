/*
 * IPAddressDeserializer.java
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv6Address;

/**
 * Base class for JSON deserializers for IP addresses.
 *
 * @author Rob Spoor
 * @param <IP> The supported type of IP address.
 */
public abstract class IPAddressDeserializer<IP extends IPAddress<?>> extends JsonDeserializer<IP> {

    /**
     * Creates a new IP address deserializer.
     */
    protected IPAddressDeserializer() {
        super();
    }

    @Override
    public IP deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p.getText(), ctxt.getContextualType());
    }

    abstract IP deserialize(String value, JavaType type);

    @Override
    public abstract Class<?> handledType();

    /**
     * Returns a JSON deserializer for {@link IPv4Address}.
     *
     * @return A JSON deserializer for {@link IPv4Address}.
     */
    public static IPAddressDeserializer<IPv4Address> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends IPAddressDeserializer<IPv4Address> {

        private static final IPv4 INSTANCE = new IPv4();

        @Override
        IPv4Address deserialize(String value, JavaType type) {
            return IPv4Address.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv4Address.class;
        }
    }

    /**
     * Returns a JSON deserializer for {@link IPv6Address}.
     *
     * @return A JSON deserializer for {@link IPv6Address}.
     */
    public static IPAddressDeserializer<IPv6Address> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends IPAddressDeserializer<IPv6Address> {

        private static final IPv6 INSTANCE = new IPv6();

        @Override
        IPv6Address deserialize(String value, JavaType type) {
            return IPv6Address.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv6Address.class;
        }
    }

    /**
     * Returns a JSON deserializer for {@link IPAddress} of any type.
     *
     * @return A JSON deserializer for {@link IPAddress} of any type.
     */
    public static IPAddressDeserializer<IPAddress<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends IPAddressDeserializer<IPAddress<?>> implements ContextualDeserializer {

        private static final AnyVersion INSTANCE = new AnyVersion();

        @Override
        IPAddress<?> deserialize(String value, JavaType type) {
            return IPAddress.valueOf(value);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            Class<?> genericType = property != null
                    ? getGenericType(property.getType())
                    : null;
            if (genericType == IPv4Address.class) {
                return ipv4();
            }
            if (genericType == IPv6Address.class) {
                return ipv6();
            }
            return this;
        }

        private Class<?> getGenericType(JavaType type) {
            return type != null
                    ? type.getBindings().getBoundType(0).getRawClass()
                    : null;
        }

        @Override
        public Class<?> handledType() {
            return IPAddress.class;
        }
    }
}
