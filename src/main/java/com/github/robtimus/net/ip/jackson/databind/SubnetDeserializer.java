/*
 * SubnetDeserializer.java
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
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

/**
 * Base class for all deserializers for {@link Subnet} and sub classes.
 *
 * @author Rob Spoor
 * @param <S> The type of subnet to deserialize.
 */
public abstract class SubnetDeserializer<S extends Subnet<?>> extends JsonDeserializer<S> {

    private SubnetDeserializer() {
    }

    @Override
    public S deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p.getText());
    }

    abstract S deserialize(String value);

    @Override
    public abstract Class<?> handledType();

    /**
     * A deserializer for {@link IPv4Subnet}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends SubnetDeserializer<IPv4Subnet> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Subnet} deserializer.
         */
        public IPv4() {
            super();
        }

        @Override
        IPv4Subnet deserialize(String value) {
            return IPv4Subnet.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv4Subnet.class;
        }
    }

    /**
     * A deserializer for {@link IPv6Subnet}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends SubnetDeserializer<IPv6Subnet> {

        static final IPv6 INSTANCE = new IPv6();

        /**
         * Creates a new {@link IPv6Subnet} deserializer.
         */
        public IPv6() {
            super();
        }

        @Override
        IPv6Subnet deserialize(String value) {
            return IPv6Subnet.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv6Subnet.class;
        }
    }

    /**
     * A deserializer for {@link Subnet}. It can handle both {@link IPv4Subnet} and {@link IPv6Subnet}. If a property is declared as either
     * {@code Subnet<IPv4Address>} or {@code Subnet<IPv6Address>}, it will limit the deserialization to only the specified type.
     * In other words, trying to deserialize an IPv6 subnet for a property of type {@code Subnet<IPv4Address>} or vice versa will fail.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends SubnetDeserializer<Subnet<?>> implements ContextualDeserializer {

        static final AnyVersion INSTANCE = new AnyVersion();

        /**
         * Creates a new {@link Subnet} deserializer.
         */
        public AnyVersion() {
            super();
        }

        @Override
        Subnet<?> deserialize(String value) {
            return Subnet.valueOf(value);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
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
            return Subnet.class;
        }
    }
}
