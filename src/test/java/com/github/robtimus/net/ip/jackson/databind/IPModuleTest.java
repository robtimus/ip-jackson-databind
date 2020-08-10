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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

@SuppressWarnings("nls")
class IPModuleTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setupMapper() {
        Module module = IPModule.instance();

        mapper = new ObjectMapper()
                .registerModule(module);
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
            }
        }

        @Nested
        @DisplayName("deserialize")
        class Deserialize {

            @Test
            @DisplayName("nulls")
            void testSerializeNulls() throws IOException {
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
            void testSerializeNonNulls() throws IOException {
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

                    String invalidIPAddress = IPv6Address.LOCALHOST.toString();
                    String json = writer.toString()
                            .replace(original.ipv4Address.toString(), invalidIPAddress);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidIPAddress));
                }

                @Test
                @DisplayName("IPv4Address instead of IPv6Address")
                void testIPv4AddressInsteadOfIPv6Address() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidIPAddress = IPv4Address.LOCALHOST.toString();
                    String json = writer.toString()
                            .replace(original.ipv6Address.toString(), invalidIPAddress);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidIPAddress));
                }

                @Test
                @DisplayName("IPv6Address instead of IPAddress<IPv4Address>")
                void testIPv6AddressInsteadOfIPAddressOfIPv4() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidIPAddress = IPv6Address.LOCALHOST.toString();
                    String json = writer.toString()
                            .replace(original.genericIPv4Address.toString(), invalidIPAddress);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidIPAddress));
                }

                @Test
                @DisplayName("IPv4Address instead of IPAddress<IPv6Address>")
                void testIPv4AddressInsteadOfIPAddressOfIPv6() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidIPAddress = IPv4Address.LOCALHOST.toString();
                    String json = writer.toString()
                            .replace(original.genericIPv6Address.toString(), invalidIPAddress);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidIPAddress));
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
            }
        }

        @Nested
        @DisplayName("deserialize")
        class Deserialize {

            @Test
            @DisplayName("nulls")
            void testSerializeNulls() throws IOException {
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
            void testSerializeNonNulls() throws IOException {
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

                    String invalidSubnet = IPv6Address.MIN_VALUE.inSubnet(0).toString();
                    String json = writer.toString()
                            .replace(original.ipv4Subnet.toString(), invalidSubnet);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidSubnet));
                }

                @Test
                @DisplayName("IPv4Subnet instead of IPv6Subnet")
                void testIPv4SubnetInsteadOfIPv6Subnet() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidSubnet = IPv4Address.LOCALHOST.inSubnet(16).toString();
                    String json = writer.toString()
                            .replace(original.ipv6Subnet.toString(), invalidSubnet);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidSubnet));
                }

                @Test
                @DisplayName("IPv6Subnet instead of Subnet<IPv4Address>")
                void testIPv6SubnetInsteadOfSubnetOfIPv4() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidSubnet = IPv6Address.MIN_VALUE.inSubnet(16).toString();
                    String json = writer.toString()
                            .replace(original.genericIPv4Subnet.toString(), invalidSubnet);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidSubnet));
                }

                @Test
                @DisplayName("IPv4Subnet instead of Subnet<IPv6Address>")
                void testIPv4SubnetInsteadOfSubnetOfIPv6() throws IOException {
                    TestClass original = createPopulatedTestObject();

                    StringWriter writer = new StringWriter();
                    mapper.writeValue(writer, original);

                    String invalidSubnet = IPv4Address.LOCALHOST.inSubnet(16).toString();
                    String json = writer.toString()
                            .replace(original.genericIPv6Subnet.toString(), invalidSubnet);

                    JsonProcessingException exception = assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, TestClass.class));
                    assertThat(exception.getCause(), instanceOf(IllegalArgumentException.class));
                    assertThat(exception.getCause().getMessage(), containsString(invalidSubnet));
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
            return testObject;
        }
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
    }
}
