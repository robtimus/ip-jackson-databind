/*
 * IPRangeDeserializer.java
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

import static com.github.robtimus.net.ip.jackson.databind.IPRangeSerializer.FROM_FIELD_NAME;
import static com.github.robtimus.net.ip.jackson.databind.IPRangeSerializer.TO_FIELD_NAME;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPRange;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Range;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

/**
 * Base class for all deserializers for {@link IPRange} and sub types.
 * It supports JSON in one of the following formats:
 * <ul>
 * <li>A CIDR subnet notation.</li>
 * <li>An object with properties {@code from} and {@code to}.</li>
 * </ul>
 *
 * @author Rob Spoor
 * @param <R> The type of IP range to deserialize.
 */
public abstract class IPRangeDeserializer<R extends IPRange<?>> extends JsonDeserializer<R> {

    private IPRangeDeserializer() {
    }

    @Override
    public R deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.hasToken(JsonToken.START_OBJECT)
                ? deserializeIPRange(p)
                : deserializeSubnet(p.getText());
    }

    private R deserializeIPRange(JsonParser p) throws IOException {
        TreeNode node = p.readValueAsTree();
        validateProperties(node, p);
        String from = getTextValue(node, FROM_FIELD_NAME);
        String to = getTextValue(node, TO_FIELD_NAME);
        return deserializeIPRange(from, to);
    }

    private String getTextValue(TreeNode node, String fieldName) {
        TreeNode childNode = node.get(fieldName);
        if (childNode instanceof TextNode) {
            TextNode textNode = (TextNode) childNode;
            return textNode.asText();
        }
        if (childNode == null) {
            throw new IllegalStateException(Messages.IPRange.missingProperty(fieldName));
        }
        throw new IllegalStateException(Messages.IPRange.invalidPropertyValue(fieldName, childNode));
    }

    private void validateProperties(TreeNode node, JsonParser p) throws UnrecognizedPropertyException {
        Set<String> fieldNames = new LinkedHashSet<>();
        for (Iterator<String> i = node.fieldNames(); i.hasNext(); ) {
            fieldNames.add(i.next());
        }
        fieldNames.remove(FROM_FIELD_NAME);
        fieldNames.remove(TO_FIELD_NAME);
        if (!fieldNames.isEmpty()) {
            String fieldName = fieldNames.iterator().next();
            throw UnrecognizedPropertyException.from(p, IPRange.class, fieldName, Arrays.asList(FROM_FIELD_NAME, TO_FIELD_NAME));
        }
    }

    abstract R deserializeSubnet(String value);

    abstract R deserializeIPRange(String from, String to);

    @Override
    public abstract Class<?> handledType();

    /**
     * A deserializer for {@link IPv4Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends IPRangeDeserializer<IPv4Range> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Range} deserializer.
         */
        public IPv4() {
            super();
        }

        @Override
        IPv4Range deserializeSubnet(String value) {
            return IPv4Subnet.valueOf(value);
        }

        @Override
        IPv4Range deserializeIPRange(String from, String to) {
            IPv4Address fromAddress = IPv4Address.valueOf(from);
            IPv4Address toAddress = IPv4Address.valueOf(to);
            return fromAddress.equals(toAddress)
                    ? fromAddress.asRange()
                    : fromAddress.to(toAddress);
        }

        @Override
        public Class<?> handledType() {
            return IPv4Range.class;
        }
    }

    /**
     * A deserializer for {@link IPv6Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends IPRangeDeserializer<IPv6Range> {

        static final IPv6 INSTANCE = new IPv6();

        /**
         * Creates a new {@link IPv6Range} deserializer.
         */
        public IPv6() {
            super();
        }

        @Override
        IPv6Range deserializeSubnet(String value) {
            return IPv6Subnet.valueOf(value);
        }

        @Override
        IPv6Range deserializeIPRange(String from, String to) {
            IPv6Address fromAddress = IPv6Address.valueOf(from);
            IPv6Address toAddress = IPv6Address.valueOf(to);
            return fromAddress.equals(toAddress)
                    ? fromAddress.asRange()
                    : fromAddress.to(toAddress);
        }

        @Override
        public Class<?> handledType() {
            return IPv6Range.class;
        }
    }

    /**
     * A deserializer for {@link IPRange}. It can handle both {@link IPv4Range} and {@link IPv6Range}. If a property is declared as either
     * {@code IPRange<IPv4Address>} or {@code IPRange<IPv6Address>}, it will limit the deserialization to only the specified type.
     * In other words, trying to deserialize an IPv6 range for a property of type {@code IPRange<IPv4Address>} or vice versa will fail.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends IPRangeDeserializer<IPRange<?>> implements ContextualDeserializer {

        static final AnyVersion INSTANCE = new AnyVersion();

        /**
         * Creates a new {@link IPRange} deserializer.
         */
        public AnyVersion() {
            super();
        }

        @Override
        IPRange<?> deserializeSubnet(String value) {
            return Subnet.valueOf(value);
        }

        @Override
        IPRange<?> deserializeIPRange(String from, String to) {
            IPAddress<?> fromAddress = IPAddress.valueOf(from);
            IPAddress<?> toAddress = IPAddress.valueOf(to);
            return fromAddress.equals(toAddress)
                    ? fromAddress.asRange()
                    : createRange(fromAddress, toAddress);
        }

        @SuppressWarnings("unchecked")
        private static <I extends IPAddress<I>> IPRange<?> createRange(IPAddress<?> from, IPAddress<?> to) {
            if (from.getClass() != to.getClass()) {
                throw new IllegalArgumentException(Messages.IPRange.incompatibleToAndFrom(from, to));
            }
            // from and to are of the same class, so the cast is safe
            return ((I) from).to((I) to);
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
            return IPRange.class;
        }
    }
}
