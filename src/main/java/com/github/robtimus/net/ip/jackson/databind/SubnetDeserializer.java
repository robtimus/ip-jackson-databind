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
 * Base class for JSON deserializers for subnets.
 *
 * @author Rob Spoor
 * @param <S> The type of subnet.
 */
public abstract class SubnetDeserializer<S extends Subnet<?>> extends JsonDeserializer<S> {

    /**
     * Creates a new subnet deserializer.
     */
    protected SubnetDeserializer() {
        super();
    }

    @Override
    public S deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return deserialize(p.getText(), ctxt.getContextualType());
    }

    abstract S deserialize(String value, JavaType type);

    @Override
    public abstract Class<?> handledType();

    /**
     * Returns a JSON deserializer for {@link IPv4Subnet}.
     *
     * @return A JSON deserializer for {@link IPv4Subnet}.
     */
    public static SubnetDeserializer<IPv4Subnet> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends SubnetDeserializer<IPv4Subnet> {

        private static final IPv4 INSTANCE = new IPv4();

        @Override
        IPv4Subnet deserialize(String value, JavaType type) {
            return IPv4Subnet.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv4Subnet.class;
        }
    }

    /**
     * Returns a JSON deserializer for {@link IPv6Subnet}.
     *
     * @return A JSON deserializer for {@link IPv6Subnet}.
     */
    public static SubnetDeserializer<IPv6Subnet> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends SubnetDeserializer<IPv6Subnet> {

        private static final IPv6 INSTANCE = new IPv6();

        @Override
        IPv6Subnet deserialize(String value, JavaType type) {
            return IPv6Subnet.valueOf(value);
        }

        @Override
        public Class<?> handledType() {
            return IPv6Subnet.class;
        }
    }

    /**
     * Returns a JSON deserializer for {@link Subnet} of any type.
     *
     * @return A JSON deserializer for {@link Subnet} of any type.
     */
    public static SubnetDeserializer<Subnet<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends SubnetDeserializer<Subnet<?>> implements ContextualDeserializer {

        private static final AnyVersion INSTANCE = new AnyVersion();

        @Override
        Subnet<?> deserialize(String value, JavaType type) {
            return Subnet.valueOf(value);
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
            return Subnet.class;
        }
    }
}
