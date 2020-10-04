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
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPAddressFormatter;
import com.github.robtimus.net.ip.IPRange;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Range;
import com.github.robtimus.net.ip.Subnet;

/**
 * Base class for all serializers for {@link IPRange} and sub types.
 *
 * @author Rob Spoor
 * @param <R> The type of IP range to serialize.
 */
public abstract class IPRangeSerializer<R extends IPRange<?>> extends JsonSerializer<R> {

    static final String FROM_FIELD_NAME = "from"; //$NON-NLS-1$
    static final String TO_FIELD_NAME = "to"; //$NON-NLS-1$

    private IPRangeSerializer() {
    }

    @Override
    public void serialize(R value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value instanceof Subnet<?>) {
            Subnet<?> subnet = (Subnet<?>) value;
            gen.writeString(formatFrom(value) + "/" + subnet.prefixLength()); //$NON-NLS-1$
        } else {
            gen.writeStartObject();
            gen.writeStringField(FROM_FIELD_NAME, formatFrom(value));
            gen.writeStringField(TO_FIELD_NAME, formatTo(value));
            gen.writeEndObject();
        }
    }

    abstract String formatFrom(R value);

    abstract String formatTo(R value);

    @Override
    public abstract Class<R> handledType();

    /**
     * A serializer for {@link IPv4Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv4 extends IPRangeSerializer<IPv4Range> {

        static final IPv4 INSTANCE = new IPv4();

        /**
         * Creates a new {@link IPv4Range} serializer.
         */
        public IPv4() {
            super();
        }

        @Override
        String formatFrom(IPv4Range value) {
            return format(value.from());
        }

        @Override
        String formatTo(IPv4Range value) {
            return format(value.to());
        }

        private String format(IPv4Address address) {
            return address.toString();
        }

        @Override
        public Class<IPv4Range> handledType() {
            return IPv4Range.class;
        }
    }

    /**
     * A serializer for {@link IPv6Range}.
     *
     * @author Rob Spoor
     */
    public static class IPv6 extends IPRangeSerializer<IPv6Range> {

        static final IPv6 INSTANCE = new IPv6(null);

        private final IPAddressFormatter<? super IPv6Address> formatter;

        /**
         * Creates a new {@link IPv6Range} serializer.
         *
         * @param formatter The formatter to use for the from and to addresses. If {@code null}, {@link IPv6Address#toString()} will be used instead.
         */
        public IPv6(IPAddressFormatter<? super IPv6Address> formatter) {
            this.formatter = formatter;
        }

        @Override
        String formatFrom(IPv6Range value) {
            return format(value.from());
        }

        @Override
        String formatTo(IPv6Range value) {
            return format(value.to());
        }

        private String format(IPv6Address address) {
            return formatter != null ? formatter.format(address) : address.toString();
        }

        @Override
        public Class<IPv6Range> handledType() {
            return IPv6Range.class;
        }
    }

    /**
     * A serializer for {@link IPRange}. It can handle both {@link IPv4Range} and {@link IPv6Range}.
     *
     * @author Rob Spoor
     */
    public static class AnyVersion extends IPRangeSerializer<IPRange<?>> {

        static final AnyVersion INSTANCE = new AnyVersion(null);

        private final IPAddressFormatter<? super IPAddress<?>> formatter;

        /**
         * Creates a new {@link IPRange} serializer.
         *
         * @param formatter The formatter to use for the from and to addresses. If {@code null}, {@link IPAddress#toString()} will be used instead.
         */
        public AnyVersion(IPAddressFormatter<? super IPAddress<?>> formatter) {
            this.formatter = formatter;
        }

        @Override
        String formatFrom(IPRange<?> value) {
            return format(value.from());
        }

        @Override
        String formatTo(IPRange<?> value) {
            return format(value.to());
        }

        private String format(IPAddress<?> address) {
            return formatter != null ? formatter.format(address) : address.toString();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Class<IPRange<?>> handledType() {
            return (Class<IPRange<?>>) (Class<?>) IPRange.class;
        }
    }
}
