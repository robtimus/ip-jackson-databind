/*
 * IPModuleTest.java
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPAddressFormatter;
import com.github.robtimus.net.ip.IPRange;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Range;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

@SuppressWarnings("nls")
class IPModuleTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setupMapper() {
        mapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @Nested
    @DisplayName("IP addresses")
    class IPAddresses {

        @Nested
        @DisplayName("serialize")
        class Serialize {

            @Test
            @DisplayName("nulls")
            void testSerializeNulls() throws IOException {
                TestClass original = new TestClass();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();
                assertThat(json, containsString("\"ipv4Address\":null"));
                assertThat(json, containsString("\"ipv6Address\":null"));
                assertThat(json, containsString("\"ipAddress\":null"));
                assertThat(json, containsString("\"genericIPv4Address\":null"));
                assertThat(json, containsString("\"genericIPv6Address\":null"));
                assertThat(json, containsString("\"ipv6AddressWithCustomFormat\":null"));
                assertThat(json, containsString("\"ipAddressWithCustomFormat\":null"));
            }

            @Test
            @DisplayName("non-nulls")
            void testSerializeNonNulls() throws IOException {
                TestClass original = createPopulatedTestObject();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();
                assertThat(json, containsString("\"ipv4Address\":\"127.0.0.1\""));
                assertThat(json, containsString("\"ipv6Address\":\"::1\""));
                assertThat(json, containsString("\"ipAddress\":\"::2\""));
                assertThat(json, containsString("\"genericIPv4Address\":\"127.0.0.2\""));
                assertThat(json, containsString("\"genericIPv6Address\":\"::3\""));
                assertThat(json, containsString("\"ipv6AddressWithCustomFormat\":\"0:0:0:0:0:0:0:1\""));
                assertThat(json, containsString("\"ipAddressWithCustomFormat\":\"0:0:0:0:0:0:0:2\""));
            }
        }

        @Nested
        @DisplayName("deserialize")
        class Deserialize {

            @Test
            @DisplayName("nulls")
            void testDeserializeNulls() throws IOException {
                TestClass original = new TestClass();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();

                TestClass deserialized = mapper.readValue(json, TestClass.class);

                assertNull(deserialized.ipv4Address);
                assertNull(deserialized.ipv6Address);
                assertNull(deserialized.ipAddress);
                assertNull(deserialized.genericIPv4Address);
                assertNull(deserialized.genericIPv4Address);
            }

            @Test
            @DisplayName("non-nulls")
            void testDeserializeNonNulls() throws IOException {
                TestClass original = createPopulatedTestObject();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();

                TestClass deserialized = mapper.readValue(json, TestClass.class);

                assertEquals(original.ipv4Address, deserialized.ipv4Address);
                assertEquals(original.ipv6Address, deserialized.ipv6Address);
                assertEquals(original.ipAddress, deserialized.ipAddress);
                assertEquals(original.genericIPv4Address, deserialized.genericIPv4Address);
                assertEquals(original.genericIPv6Address, deserialized.genericIPv6Address);
            }

            @Nested
            @DisplayName("incompatible values")
            class IncompatibleVersions {

                @Test
                @DisplayName("IPv6Address instead of IPv4Address")
                void testIPv6AddressInsteadOfIPv4Address() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv6Address invalidIPAddress = IPv6Address.LOCALHOST;
                    String json = writer.toString()
                            .replace(original.ipv4Address.toString(), invalidIPAddress.toString());

                    assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv4Address::valueOf);
                }

                @Test
                @DisplayName("IPv4Address instead of IPv6Address")
                void testIPv4AddressInsteadOfIPv6Address() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv4Address invalidIPAddress = IPv4Address.LOCALHOST;
                    String json = writer.toString()
                            .replace(original.ipv6Address.toString(), invalidIPAddress.toString());

                    assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv6Address::valueOf);
                }

                @Test
                @DisplayName("IPv6Address instead of IPAddress<IPv4Address>")
                void testIPv6AddressInsteadOfIPAddressOfIPv4() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv6Address invalidIPAddress = IPv6Address.LOCALHOST;
                    String json = writer.toString()
                            .replace(original.genericIPv4Address.toString(), invalidIPAddress.toString());

                    assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv4Address::valueOf);
                }

                @Test
                @DisplayName("IPv4Address instead of IPAddress<IPv6Address>")
                void testIPv4AddressInsteadOfIPAddressOfIPv6() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv4Address invalidIPAddress = IPv4Address.LOCALHOST;
                    String json = writer.toString()
                            .replace(original.genericIPv6Address.toString(), invalidIPAddress.toString());

                    assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv6Address::valueOf);
                }

                private void assertInvalidIPAddressError(String json, String invalidIPAddress, Function<String, IPAddress<?>> parser) {
                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));

                    String expected = assertThrows(IllegalArgumentException.class, () -> parser.apply(invalidIPAddress)).getMessage();
                    assertEquals(expected, exception.getCause().getMessage());
                }
            }
        }

        private TestClass createPopulatedTestObject() {
            TestClass testObject = new TestClass();
            testObject.ipv4Address = IPv4Address.LOCALHOST;
            testObject.ipv6Address = IPv6Address.LOCALHOST;
            testObject.ipAddress = IPv6Address.LOCALHOST.next();
            testObject.genericIPv4Address = IPv4Address.LOCALHOST.next();
            testObject.genericIPv6Address = IPv6Address.LOCALHOST.next().next();
            testObject.ipv6AddressWithCustomFormat = testObject.ipv6Address;
            testObject.ipAddressWithCustomFormat = testObject.ipAddress;
            return testObject;
        }
    }

    @Nested
    @DisplayName("Subnets")
    class Subnets {

        @Nested
        @DisplayName("serialize")
        class Serialize {

            @Test
            @DisplayName("nulls")
            void testSerializeNulls() throws IOException {
                TestClass original = new TestClass();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();
                assertThat(json, containsString("\"ipv4Subnet\":null"));
                assertThat(json, containsString("\"ipv6Subnet\":null"));
                assertThat(json, containsString("\"subnet\":null"));
                assertThat(json, containsString("\"genericIPv4Subnet\":null"));
                assertThat(json, containsString("\"genericIPv6Subnet\":null"));
                assertThat(json, containsString("\"ipv6SubnetWithCustomFormat\":null"));
                assertThat(json, containsString("\"subnetWithCustomFormat\":null"));
            }

            @Test
            @DisplayName("non-nulls")
            void testSerializeNonNulls() throws IOException {
                TestClass original = createPopulatedTestObject();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();
                assertThat(json, containsString("\"ipv4Subnet\":\"127.0.0.0/24\""));
                assertThat(json, containsString("\"ipv6Subnet\":\"::/96\""));
                assertThat(json, containsString("\"subnet\":\"::/64\""));
                assertThat(json, containsString("\"genericIPv4Subnet\":\"127.0.0.0/16\""));
                assertThat(json, containsString("\"genericIPv6Subnet\":\"::/80\""));
                assertThat(json, containsString("\"ipv6SubnetWithCustomFormat\":\"0:0:0:0:0:0:0:0/96\""));
                assertThat(json, containsString("\"subnetWithCustomFormat\":\"0:0:0:0:0:0:0:0/64\""));
            }
        }

        @Nested
        @DisplayName("deserialize")
        class Deserialize {

            @Test
            @DisplayName("nulls")
            void testDeserializeNulls() throws IOException {
                TestClass original = new TestClass();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();

                TestClass deserialized = mapper.readValue(json, TestClass.class);

                assertNull(deserialized.ipv4Subnet);
                assertNull(deserialized.ipv6Subnet);
                assertNull(deserialized.subnet);
                assertNull(deserialized.genericIPv4Subnet);
                assertNull(deserialized.genericIPv4Subnet);
            }

            @Test
            @DisplayName("non-nulls")
            void testDeserializeNonNulls() throws IOException {
                TestClass original = createPopulatedTestObject();

                StringWriter writer = new StringWriter();
                mapper.writeValue(writer, original);

                String json = writer.toString();

                TestClass deserialized = mapper.readValue(json, TestClass.class);

                assertEquals(original.ipv4Subnet, deserialized.ipv4Subnet);
                assertEquals(original.ipv6Subnet, deserialized.ipv6Subnet);
                assertEquals(original.subnet, deserialized.subnet);
                assertEquals(original.genericIPv4Subnet, deserialized.genericIPv4Subnet);
                assertEquals(original.genericIPv6Subnet, deserialized.genericIPv6Subnet);
            }

            @Nested
            @DisplayName("incompatible values")
            class IncompatibleVersions {

                @Test
                @DisplayName("IPv6Subnet instead of IPv4Subnet")
                void testIPv6SubnetInsteadOfIPv4Subnet() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv6Subnet invalidSubnet = IPv6Address.MIN_VALUE.inSubnet(0);
                    String json = writer.toString()
                            .replace(original.ipv4Subnet.toString(), invalidSubnet.toString());

                    assertInvalidSubnetError(json, invalidSubnet.toString(), IPv4Subnet::valueOf);
                }

                @Test
                @DisplayName("IPv4Subnet instead of IPv6Subnet")
                void testIPv4SubnetInsteadOfIPv6Subnet() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv4Subnet invalidSubnet = IPv4Address.LOCALHOST.inSubnet(16);
                    String json = writer.toString()
                            .replace(original.ipv6Subnet.toString(), invalidSubnet.toString());

                    assertInvalidSubnetError(json, invalidSubnet.toString(), IPv6Subnet::valueOf);
                }

                @Test
                @DisplayName("IPv6Subnet instead of Subnet<IPv4Address>")
                void testIPv6SubnetInsteadOfSubnetOfIPv4() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv6Subnet invalidSubnet = IPv6Address.MIN_VALUE.inSubnet(16);
                    String json = writer.toString()
                            .replace(original.genericIPv4Subnet.toString(), invalidSubnet.toString());

                    assertInvalidSubnetError(json, invalidSubnet.toString(), IPv4Subnet::valueOf);
                }

                @Test
                @DisplayName("IPv4Subnet instead of Subnet<IPv6Address>")
                void testIPv4SubnetInsteadOfSubnetOfIPv6() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    IPv4Subnet invalidSubnet = IPv4Address.LOCALHOST.inSubnet(16);
                    String json = writer.toString()
                            .replace(original.genericIPv6Subnet.toString(), invalidSubnet.toString());

                    assertInvalidSubnetError(json, invalidSubnet.toString(), IPv6Subnet::valueOf);
                }

                private void assertInvalidSubnetError(String json, String invalidSubnet, Function<String, Subnet<?>> parser) {
                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));

                    String expected = assertThrows(IllegalArgumentException.class, () -> parser.apply(invalidSubnet)).getMessage();
                    assertEquals(expected, exception.getCause().getMessage());
                }
            }
        }

        private TestClass createPopulatedTestObject() {
            TestClass testObject = new TestClass();
            testObject.ipv4Subnet = IPv4Address.LOCALHOST.inSubnet(24);
            testObject.ipv6Subnet = IPv6Address.MIN_VALUE.inSubnet(96);
            testObject.subnet = IPv6Address.MIN_VALUE.inSubnet(64);
            testObject.genericIPv4Subnet = IPv4Address.LOCALHOST.inSubnet(16);
            testObject.genericIPv6Subnet = IPv6Address.MIN_VALUE.inSubnet(80);
            testObject.ipv6SubnetWithCustomFormat = testObject.ipv6Subnet;
            testObject.subnetWithCustomFormat = testObject.subnet;
            return testObject;
        }
    }

    @Nested
    @DisplayName("IP ranges")
    class IPRanges {

        @Nested
        @DisplayName("Subnets")
        class Subnets {

            @Nested
            @DisplayName("serialize")
            class Serialize {

                @Test
                @DisplayName("nulls")
                void testSerializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":null"));
                    assertThat(json, containsString("\"ipv6Range\":null"));
                    assertThat(json, containsString("\"ipRange\":null"));
                    assertThat(json, containsString("\"genericIPv4Range\":null"));
                    assertThat(json, containsString("\"genericIPv6Range\":null"));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":null"));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":null"));
                }

                @Test
                @DisplayName("non-nulls")
                void testSerializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":\"127.0.0.0/24\""));
                    assertThat(json, containsString("\"ipv6Range\":\"::/96\""));
                    assertThat(json, containsString("\"ipRange\":\"::/64\""));
                    assertThat(json, containsString("\"genericIPv4Range\":\"127.0.0.0/16\""));
                    assertThat(json, containsString("\"genericIPv6Range\":\"::/80\""));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":\"0:0:0:0:0:0:0:0/96\""));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":\"0:0:0:0:0:0:0:0/64\""));
                }
            }

            @Nested
            @DisplayName("deserialize")
            class Deserialize {

                @Test
                @DisplayName("nulls")
                void testDeserializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertNull(deserialized.ipv4Range);
                    assertNull(deserialized.ipv6Range);
                    assertNull(deserialized.ipRange);
                    assertNull(deserialized.genericIPv4Range);
                    assertNull(deserialized.genericIPv4Range);
                }

                @Test
                @DisplayName("non-nulls")
                void testDeserializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertEquals(original.ipv4Range, deserialized.ipv4Range);
                    assertEquals(original.ipv6Range, deserialized.ipv6Range);
                    assertEquals(original.ipRange, deserialized.ipRange);
                    assertEquals(original.genericIPv4Range, deserialized.genericIPv4Range);
                    assertEquals(original.genericIPv6Range, deserialized.genericIPv6Range);
                }

                @Nested
                @DisplayName("incompatible values")
                class IncompatibleVersions {

                    @Test
                    @DisplayName("IPv6Range instead of IPv4Range")
                    void testIPv6RangeInsteadOfIPv4Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Range invalidRange = IPv6Address.MIN_VALUE.inSubnet(0);
                        String json = writer.toString()
                                .replace(original.ipv4Range.toString(), invalidRange.toString());

                        assertInvalidSubnetError(json, invalidRange.toString(), IPv4Subnet::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPv6Range")
                    void testIPv4RangeInsteadOfIPv6Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Range invalidRange = IPv4Address.LOCALHOST.inSubnet(16);
                        String json = writer.toString()
                                .replace(original.ipv6Range.toString(), invalidRange.toString());

                        assertInvalidSubnetError(json, invalidRange.toString(), IPv6Subnet::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6Range instead of IPRange<IPv4Address>")
                    void testIPv6RangeInsteadOfIPRangeOfIPv4() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Range invalidRange = IPv6Address.MIN_VALUE.inSubnet(16);
                        String json = writer.toString()
                                .replace(original.genericIPv4Range.toString(), invalidRange.toString());

                        assertInvalidSubnetError(json, invalidRange.toString(), IPv4Subnet::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPRange<IPv6Address>")
                    void testIPv4RangeInsteadOfRangeOfIPv6() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Range invalidRange = IPv4Address.LOCALHOST.inSubnet(16);
                        String json = writer.toString()
                                .replace(original.genericIPv6Range.toString(), invalidRange.toString());

                        assertInvalidSubnetError(json, invalidRange.toString(), IPv6Subnet::valueOf);
                    }

                    private void assertInvalidSubnetError(String json, String invalidSubnet, Function<String, IPRange<?>> parser) {
                        JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                                () -> mapper.readValue(json, TestClass.class));
                        assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));

                        String expected = assertThrows(IllegalArgumentException.class, () -> parser.apply(invalidSubnet)).getMessage();
                        assertEquals(expected, exception.getCause().getMessage());
                    }
                }
            }

            private TestClass createPopulatedTestObject() {
                TestClass testObject = new TestClass();
                testObject.ipv4Range = IPv4Address.LOCALHOST.inSubnet(24);
                testObject.ipv6Range = IPv6Address.MIN_VALUE.inSubnet(96);
                testObject.ipRange = IPv6Address.MIN_VALUE.inSubnet(64);
                testObject.genericIPv4Range = IPv4Address.LOCALHOST.inSubnet(16);
                testObject.genericIPv6Range = IPv6Address.MIN_VALUE.inSubnet(80);
                testObject.ipv6RangeWithCustomFormat = testObject.ipv6Range;
                testObject.ipRangeWithCustomFormat = testObject.ipRange;
                return testObject;
            }
        }

        @Nested
        @DisplayName("Singleton IP ranges")
        class Singletons {

            @Nested
            @DisplayName("serialize")
            class Serialize {

                @Test
                @DisplayName("nulls")
                void testSerializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":null"));
                    assertThat(json, containsString("\"ipv6Range\":null"));
                    assertThat(json, containsString("\"ipRange\":null"));
                    assertThat(json, containsString("\"genericIPv4Range\":null"));
                    assertThat(json, containsString("\"genericIPv6Range\":null"));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":null"));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":null"));
                }

                @Test
                @DisplayName("non-nulls")
                void testSerializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":{\"from\":\"127.0.0.1\",\"to\":\"127.0.0.1\"}"));
                    assertThat(json, containsString("\"ipv6Range\":{\"from\":\"::1\",\"to\":\"::1\"}"));
                    assertThat(json, containsString("\"ipRange\":{\"from\":\"::2\",\"to\":\"::2\"}"));
                    assertThat(json, containsString("\"genericIPv4Range\":{\"from\":\"127.0.0.2\",\"to\":\"127.0.0.2\"}"));
                    assertThat(json, containsString("\"genericIPv6Range\":{\"from\":\"::3\",\"to\":\"::3\"}"));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":{\"from\":\"0:0:0:0:0:0:0:1\",\"to\":\"0:0:0:0:0:0:0:1\"}"));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":{\"from\":\"0:0:0:0:0:0:0:2\",\"to\":\"0:0:0:0:0:0:0:2\"}"));
                }
            }

            @Nested
            @DisplayName("deserialize")
            class Deserialize {

                @Test
                @DisplayName("nulls")
                void testDeserializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertNull(deserialized.ipv4Range);
                    assertNull(deserialized.ipv6Range);
                    assertNull(deserialized.ipRange);
                    assertNull(deserialized.genericIPv4Range);
                    assertNull(deserialized.genericIPv4Range);
                }

                @Test
                @DisplayName("non-nulls")
                void testDeserializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertEquals(original.ipv4Range, deserialized.ipv4Range);
                    assertEquals(original.ipv6Range, deserialized.ipv6Range);
                    assertEquals(original.ipRange, deserialized.ipRange);
                    assertEquals(original.genericIPv4Range, deserialized.genericIPv4Range);
                    assertEquals(original.genericIPv6Range, deserialized.genericIPv6Range);
                }

                @Nested
                @DisplayName("incompatible values")
                class IncompatibleVersions {

                    @Test
                    @DisplayName("IPv6Range instead of IPv4Range")
                    void testIPv6RangeInsteadOfIPv4Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidIPAddress = IPv6Address.LOCALHOST;
                        String json = writer.toString()
                                .replace(original.ipv4Range.from().toString(), invalidIPAddress.toString());

                        assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPv6Range")
                    void testIPv4RangeInsteadOfIPv6Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidIPAddress = IPv4Address.LOCALHOST;
                        String json = writer.toString()
                                .replace(original.ipv6Range.from().toString(), invalidIPAddress.toString());

                        assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6Range instead of IPRange<IPv4Address>")
                    void testIPv6RangeInsteadOfIPRangeOfIPv4() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidIPAddress = IPv6Address.LOCALHOST;
                        String json = writer.toString()
                                .replace(original.genericIPv4Range.from().toString(), invalidIPAddress.toString());

                        assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPRange<IPv6Address>")
                    void testIPv4RangeInsteadOfRangeOfIPv6() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidIPAddress = IPv4Address.LOCALHOST;
                        String json = writer.toString()
                                .replace(original.genericIPv6Range.from().toString(), invalidIPAddress.toString());

                        assertInvalidIPAddressError(json, invalidIPAddress.toString(), IPv6Address::valueOf);
                    }

                    private void assertInvalidIPAddressError(String json, String invalidIPAddress, Function<String, IPAddress<?>> parser) {
                        JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                                () -> mapper.readValue(json, TestClass.class));
                        assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));

                        String expected = assertThrows(IllegalArgumentException.class, () -> parser.apply(invalidIPAddress)).getMessage();
                        assertEquals(expected, exception.getCause().getMessage());
                    }
                }
            }

            private TestClass createPopulatedTestObject() {
                TestClass testObject = new TestClass();
                testObject.ipv4Range = IPv4Address.LOCALHOST.asRange();
                testObject.ipv6Range = IPv6Address.LOCALHOST.asRange();
                testObject.ipRange = IPv6Address.LOCALHOST.next().asRange();
                testObject.genericIPv4Range = IPv4Address.LOCALHOST.next().asRange();
                testObject.genericIPv6Range = IPv6Address.LOCALHOST.next().next().asRange();
                testObject.ipv6RangeWithCustomFormat = testObject.ipv6Range;
                testObject.ipRangeWithCustomFormat = testObject.ipRange;
                return testObject;
            }
        }

        @Nested
        @DisplayName("Regular IP ranges")
        class Regular {

            @Nested
            @DisplayName("serialize")
            class Serialize {

                @Test
                @DisplayName("nulls")
                void testSerializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":null"));
                    assertThat(json, containsString("\"ipv6Range\":null"));
                    assertThat(json, containsString("\"ipRange\":null"));
                    assertThat(json, containsString("\"genericIPv4Range\":null"));
                    assertThat(json, containsString("\"genericIPv6Range\":null"));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":null"));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":null"));
                }

                @Test
                @DisplayName("non-nulls")
                void testSerializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();
                    assertThat(json, containsString("\"ipv4Range\":{\"from\":\"127.0.0.1\",\"to\":\"127.0.0.6\"}"));
                    assertThat(json, containsString("\"ipv6Range\":{\"from\":\"::1\",\"to\":\"::6\"}"));
                    assertThat(json, containsString("\"ipRange\":{\"from\":\"::2\",\"to\":\"::7\"}"));
                    assertThat(json, containsString("\"genericIPv4Range\":{\"from\":\"127.0.0.2\",\"to\":\"127.0.0.7\"}"));
                    assertThat(json, containsString("\"genericIPv6Range\":{\"from\":\"::3\",\"to\":\"::8\"}"));
                    assertThat(json, containsString("\"ipv6RangeWithCustomFormat\":{\"from\":\"0:0:0:0:0:0:0:1\",\"to\":\"0:0:0:0:0:0:0:6\"}"));
                    assertThat(json, containsString("\"ipRangeWithCustomFormat\":{\"from\":\"0:0:0:0:0:0:0:2\",\"to\":\"0:0:0:0:0:0:0:7\"}"));
                }
            }

            @Nested
            @DisplayName("deserialize")
            class Deserialize {

                @Test
                @DisplayName("nulls")
                void testDeserializeNulls() throws IOException {
                    TestClass original = new TestClass();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertNull(deserialized.ipv4Range);
                    assertNull(deserialized.ipv6Range);
                    assertNull(deserialized.ipRange);
                    assertNull(deserialized.genericIPv4Range);
                    assertNull(deserialized.genericIPv4Range);
                }

                @Test
                @DisplayName("non-nulls")
                void testDeserializeNonNulls() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String json = writer.toString();

                    TestClass deserialized = mapper.readValue(json, TestClass.class);

                    assertEquals(original.ipv4Range, deserialized.ipv4Range);
                    assertEquals(original.ipv6Range, deserialized.ipv6Range);
                    assertEquals(original.ipRange, deserialized.ipRange);
                    assertEquals(original.genericIPv4Range, deserialized.genericIPv4Range);
                    assertEquals(original.genericIPv6Range, deserialized.genericIPv6Range);
                }

                @Nested
                @DisplayName("incompatible values")
                class IncompatibleVersions {

                    @Test
                    @DisplayName("IPv6 from for IPv4Range")
                    void testIPv6RangeInsteadOfIPv4Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidFrom = IPv6Address.LOCALHOST;
                        IPv6Address invalidTo = to(invalidFrom);
                        String json = writer.toString()
                                .replace(original.ipv4Range.from().toString(), invalidFrom.toString())
                                .replace(original.ipv4Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPv6Range")
                    void testIPv4RangeInsteadOfIPv6Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidFrom = IPv4Address.LOCALHOST;
                        IPv4Address invalidTo = to(invalidFrom);
                        String json = writer.toString()
                                .replace(original.ipv6Range.from().toString(), invalidFrom.toString())
                                .replace(original.ipv6Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6Range instead of IPRange<IPv4Address>")
                    void testIPv6RangeInsteadOfIPRangeOfIPv4() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidFrom = IPv6Address.LOCALHOST;
                        IPv6Address invalidTo = to(invalidFrom);
                        String json = writer.toString()
                                .replace(original.genericIPv4Range.from().toString(), invalidFrom.toString())
                                .replace(original.genericIPv4Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4Range instead of IPRange<IPv6Address>")
                    void testIPv4RangeInsteadOfRangeOfIPv6() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidFrom = IPv4Address.LOCALHOST;
                        IPv4Address invalidTo = to(invalidFrom);
                        String json = writer.toString()
                                .replace(original.genericIPv6Range.from().toString(), invalidFrom.toString())
                                .replace(original.genericIPv6Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6 from for IPv4Range")
                    void testIPv6FromForIPv4Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidFrom = IPv6Address.MIN_VALUE;
                        String json = writer.toString()
                                .replace(original.ipv4Range.from().toString(), invalidFrom.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6 to for IPv4Range")
                    void testIPv6ToForIPv4Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidTo = IPv6Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.ipv4Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidTo.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4 from for IPv6Range")
                    void testIPv4FromForIPv6Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidFrom = IPv4Address.MIN_VALUE;
                        String json = writer.toString()
                                .replace(original.ipv6Range.from().toString(), invalidFrom.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4 to for IPv6Range")
                    void testIPv4ToForIPv6Range() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidTo = IPv4Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.ipv6Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidTo.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6 from for IPRange<IPv4Address>")
                    void testIPv6FromForIPRangeOfIPv4() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidFrom = IPv6Address.MIN_VALUE;
                        String json = writer.toString()
                                .replace(original.genericIPv4Range.from().toString(), invalidFrom.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv6 to for IPRange<IPv4Address>")
                    void testIPv6ToForIPRangeOfIPv4() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address invalidTo = IPv6Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.genericIPv4Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidTo.toString(), IPv4Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4 from for IPRange<IPv6Address>")
                    void testIPv4FromForIPRangeOfIPv6() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidFrom = IPv4Address.MIN_VALUE;
                        String json = writer.toString()
                                .replace(original.genericIPv6Range.from().toString(), invalidFrom.toString());

                        assertInvalidIPAddressError(json, invalidFrom.toString(), IPv6Address::valueOf);
                    }

                    @Test
                    @DisplayName("IPv4 to for IPRange<IPv6Address>")
                    void testIPv4ToForIPRangeOfIPv6() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address invalidTo = IPv4Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.genericIPv6Range.to().toString(), invalidTo.toString());

                        assertInvalidIPAddressError(json, invalidTo.toString(), IPv6Address::valueOf);
                    }

                    private void assertInvalidIPAddressError(String json, String invalidIPAddress, Function<String, IPAddress<?>> parser) {
                        JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                                () -> mapper.readValue(json, TestClass.class));
                        assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));

                        String expected = assertThrows(IllegalArgumentException.class, () -> parser.apply(invalidIPAddress)).getMessage();
                        assertEquals(expected, exception.getCause().getMessage());
                    }

                    @Test
                    @DisplayName("IPv4 from and IPv6 to for IPRange<?>")
                    void testIPv4FromAndIPv6ToForIPRange() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv4Address to = IPv4Address.MIN_VALUE;
                        IPv6Address from = IPv6Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.ipRange.from().toString(), from.toString())
                                .replace(original.ipRange.to().toString(), to.toString());

                        JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                                () -> mapper.readValue(json, TestClass.class));
                        assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                        assertEquals(Messages.IPRange.incompatibleToAndFrom.get(from, to), exception.getCause().getMessage());
                    }

                    @Test
                    @DisplayName("IPv6 from and IPv4 to for IPRange<?>")
                    void testIPv6FromAndIPv4ToForIPRange() throws IOException {
                        TestClass original = createPopulatedTestObject();

                        StringWriter writer = new StringWriter();
                        mapper.writeValue(writer, original);

                        IPv6Address to = IPv6Address.MIN_VALUE;
                        IPv4Address from = IPv4Address.MAX_VALUE;
                        String json = writer.toString()
                                .replace(original.ipRange.from().toString(), from.toString())
                                .replace(original.ipRange.to().toString(), to.toString());

                        JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                                () -> mapper.readValue(json, TestClass.class));
                        assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                        assertEquals(Messages.IPRange.incompatibleToAndFrom.get(from, to), exception.getCause().getMessage());
                    }
                }
            }

            @Nested
            @DisplayName("invalid contents")
            class InvalidContents {

                @Test
                @DisplayName("missing from")
                void testMissingFrom() {
                    String json = "{\"ipRange\":{\"to\":\"127.0.0.1\"}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.missingProperty.get("from"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("missing to")
                void testMissingTo() {
                    String json = "{\"ipRange\":{\"from\":\"127.0.0.1\"}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.missingProperty.get("to"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("null from")
                void testNullFrom() {
                    String json = "{\"ipRange\":{\"from\":null,\"to\":\"127.0.0.1\"}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.invalidPropertyValue.get("from", "null"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("null to")
                void testNullTo() {
                    String json = "{\"ipRange\":{\"from\":\"127.0.0.1\",\"to\":null}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.invalidPropertyValue.get("to", "null"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("incorrect from")
                void testIncorrectFrom() {
                    String json = "{\"ipRange\":{\"from\":{},\"to\":\"127.0.0.1\"}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.invalidPropertyValue.get("from", "{}"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("incorrect to")
                void testIncorrectTo() {
                    String json = "{\"ipRange\":{\"from\":\"127.0.0.1\",\"to\":{}}";

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalStateException.class));
                    assertEquals(Messages.IPRange.invalidPropertyValue.get("to", "{}"), exception.getCause().getMessage());
                }

                @Test
                @DisplayName("unknown property")
                void testUnknownProperty() {
                    String json = "{\"ipRange\":{\"from\":\"127.0.0.1\",\"to\":\"127.0.0.6\",\"unknown\":null}";

                    UnrecognizedPropertyException exception = assertThrows(UnrecognizedPropertyException.class,
                            () -> mapper.readValue(json, TestClass.class));
                    assertEquals(IPRange.class, exception.getReferringClass());
                    assertEquals("unknown", exception.getPropertyName());
                    assertThat(exception.getKnownPropertyIds(), contains("from", "to"));
                }
            }

            private TestClass createPopulatedTestObject() {
                TestClass testObject = new TestClass();
                testObject.ipv4Range = (IPv4Range) createRange(IPv4Address.LOCALHOST);
                testObject.ipv6Range = (IPv6Range) createRange(IPv6Address.LOCALHOST);
                testObject.ipRange = createRange(IPv6Address.LOCALHOST.next());
                testObject.genericIPv4Range = createRange(IPv4Address.LOCALHOST.next());
                testObject.genericIPv6Range = createRange(IPv6Address.LOCALHOST.next().next());
                testObject.ipv6RangeWithCustomFormat = testObject.ipv6Range;
                testObject.ipRangeWithCustomFormat = testObject.ipRange;
                return testObject;
            }

            private <I extends IPAddress<I>> IPRange<I> createRange(I start) {
                I end = to(start);
                return start.to(end);
            }

            private <I extends IPAddress<I>> I to(I start) {
                I end = start;
                for (int i = 0; i < 5; i++) {
                    end = end.next();
                }
                return end;
            }
        }
    }

    @Test
    @DisplayName("IPModule.instance()")
    void testInstance() {

        assertEquals(IPModule.class, IPModule.instance().getClass());
        assertSame(IPModule.instance(), IPModule.instance());
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static final class TestClass {

        private IPv4Address ipv4Address;
        private IPv6Address ipv6Address;
        private IPAddress<?> ipAddress;
        private IPAddress<IPv4Address> genericIPv4Address;
        private IPAddress<IPv6Address> genericIPv6Address;

        private IPv4Subnet ipv4Subnet;
        private IPv6Subnet ipv6Subnet;
        private Subnet<?> subnet;
        private Subnet<IPv4Address> genericIPv4Subnet;
        private Subnet<IPv6Address> genericIPv6Subnet;

        private IPv4Range ipv4Range;
        private IPv6Range ipv6Range;
        private IPRange<?> ipRange;
        private IPRange<IPv4Address> genericIPv4Range;
        private IPRange<IPv6Address> genericIPv6Range;

        @JsonSerialize(using = CustomIPv6AddressSerializer.class)
        private IPv6Address ipv6AddressWithCustomFormat;
        @JsonSerialize(using = CustomIPAddressSerializer.class)
        private IPAddress<?> ipAddressWithCustomFormat;

        @JsonSerialize(using = CustomIPv6RangeSerializer.class)
        private IPv6Range ipv6RangeWithCustomFormat;
        @JsonSerialize(using = CustomIPRangeSerializer.class)
        private IPRange<?> ipRangeWithCustomFormat;

        @JsonSerialize(using = CustomIPv6SubnetSerializer.class)
        private IPv6Subnet ipv6SubnetWithCustomFormat;
        @JsonSerialize(using = CustomSubnetSerializer.class)
        private Subnet<?> subnetWithCustomFormat;
    }

    private static final class CustomIPv6AddressSerializer extends IPAddressSerializer.IPv6 {

        CustomIPv6AddressSerializer() {
            super(IPAddressFormatter.ipv6()
                    .withMediumStyle()
                    .build());
        }
    }

    private static final class CustomIPAddressSerializer extends IPAddressSerializer.AnyVersion {

        CustomIPAddressSerializer() {
            super(IPAddressFormatter.anyVersion()
                    .withMediumStyle()
                    .build());
        }
    }

    private static final class CustomIPv6RangeSerializer extends IPRangeSerializer.IPv6 {

        CustomIPv6RangeSerializer() {
            super(IPAddressFormatter.ipv6()
                    .withMediumStyle()
                    .build());
        }
    }

    private static final class CustomIPRangeSerializer extends IPRangeSerializer.AnyVersion {

        CustomIPRangeSerializer() {
            super(IPAddressFormatter.anyVersion()
                    .withMediumStyle()
                    .build());
        }
    }

    private static final class CustomIPv6SubnetSerializer extends SubnetSerializer.IPv6 {

        CustomIPv6SubnetSerializer() {
            super(IPAddressFormatter.ipv6()
                    .withMediumStyle()
                    .build());
        }
    }

    private static final class CustomSubnetSerializer extends SubnetSerializer.AnyVersion {

        CustomSubnetSerializer() {
            super(IPAddressFormatter.anyVersion()
                    .withMediumStyle()
                    .build());
        }
    }
}
