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

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
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

abstract class IPRangeDeserializer<R extends IPRange<?>> extends JsonDeserializer<R> {

    @Override
    public R deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return p.hasToken(JsonToken.START_OBJECT)
                ? deserializeIPRange(p)
                : deserializeSubnet(p.getText());
    }

    private R deserializeIPRange(JsonParser p) throws IOException {
        TreeNode node = p.readValueAsTree();
        String from = getTextValue(node, "from"); //$NON-NLS-1$
        String to = getTextValue(node, "to"); //$NON-NLS-1$
        return deserializeIPRange(from, to);
    }

    private String getTextValue(TreeNode node, String fieldName) {
        TextNode textNode = (TextNode) node.get(fieldName);
        return textNode.asText();
    }

    abstract R deserializeSubnet(String value);

    abstract R deserializeIPRange(String from, String to);

    @Override
    public abstract Class<?> handledType();

    static IPRangeDeserializer<IPv4Range> ipv4() {
        return IPv4.INSTANCE;
    }

    private static final class IPv4 extends IPRangeDeserializer<IPv4Range> {

        private static final IPv4 INSTANCE = new IPv4();

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

    static IPRangeDeserializer<IPv6Range> ipv6() {
        return IPv6.INSTANCE;
    }

    private static final class IPv6 extends IPRangeDeserializer<IPv6Range> {

        private static final IPv6 INSTANCE = new IPv6();

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

    static IPRangeDeserializer<IPRange<?>> anyVersion() {
        return AnyVersion.INSTANCE;
    }

    private static final class AnyVersion extends IPRangeDeserializer<IPRange<?>> implements ContextualDeserializer {

        private static final AnyVersion INSTANCE = new AnyVersion();

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
                throw new IllegalArgumentException(Messages.IPRange.incompatibleToAndFrom.get(from, to));
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
            return IPRange.class;
        }
    }
}
