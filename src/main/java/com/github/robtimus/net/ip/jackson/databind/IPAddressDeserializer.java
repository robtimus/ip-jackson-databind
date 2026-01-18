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

import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv6Address;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Base class for all deserializers for {@link IPAddress} and sub classes.
 *
 * @author Rob Spoor
 * @param <I> The type of IP address to deserialize.
 */
public abstract class IPAddressDeserializer<I extends IPAddress<?>> extends ValueDeserializer<I> {

    private IPAddressDeserializer() {
    }

    @Override
    public I deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return deserialize(p.getString());
    }

    abstract I deserialize(String value);

    @Override
    public abstract Class<?> handledType();

    /**
     * A deserializer for {@link IPv4Address}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends IPAddressDeserializer<IPv4Address> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Address} deserializer.
         */
        public IPv4() {
            super();
        }

        @Override
        IPv4Address deserialize(String value) {
            return IPv4Address.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv4Address.class;
        }
    }

    /**
     * A deserializer for {@link IPv6Address}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends IPAddressDeserializer<IPv6Address> {

        static final IPv6 INSTANCE = new IPv6();

        /**
         * Creates a new {@link IPv6Address} deserializer.
         */
        public IPv6() {
            super();
        }

        @Override
        IPv6Address deserialize(String value) {
            return IPv6Address.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv6Address.class;
        }
    }

    /**
     * A deserializer for {@link IPAddress}. It can handle both {@link IPv4Address} and {@link IPv6Address}. If a property is declared as either
     * {@code IPAddress<IPv4Address>} or {@code IPAddress<IPv6Address>}, it will limit the deserialization to only the specified type.
     * In other words, trying to deserialize an IPv6 address for a property of type {@code IPAddress<IPv4Address>} or vice versa will fail.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends IPAddressDeserializer<IPAddress<?>> {

        static final AnyVersion INSTANCE = new AnyVersion();

        /**
         * Creates a new {@link IPAddress} deserializer.
         */
        public AnyVersion() {
            super();
        }

        @Override
        IPAddress<?> deserialize(String value) {
            return IPAddress.valueOf(value);
        }

        @Override
        public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            Class<?> genericType = property != null
                    ? getGenericType(property.getType())
                    : null;
            if (genericType == IPv4Address.class) {
                return IPv4.INSTANCE;
            }
            if (genericType == IPv6Address.class) {
                return IPv6.INSTANCE;
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
