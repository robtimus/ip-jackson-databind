/*
 * IPModule.java
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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;

/**
 * A module that adds support for serializing and deserializing IP addresses and ranges values.
 *
 * @author Rob Spoor
 */
public final class IPModule extends Module {

    private static final IPModule INSTANCE = new IPModule();

    private IPModule() {
        super();
    }

    @Override
    public String getModuleName() {
        return getClass().getName();
    }

    @Override
    public Version version() {
        return ModuleVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        setupSerializers(context);
        setupDeserializers(context);
    }

    private void setupSerializers(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();

        serializers.addSerializer(IPAddressSerializer.ipv4());
        serializers.addSerializer(IPAddressSerializer.ipv6());
        serializers.addSerializer(IPAddressSerializer.anyVersion());

        serializers.addSerializer(SubnetSerializer.ipv4());
        serializers.addSerializer(SubnetSerializer.ipv6());
        serializers.addSerializer(SubnetSerializer.anyVersion());

        context.addSerializers(serializers);
    }

    private void setupDeserializers(SetupContext context) {
        SimpleDeserializers deserializers = new SimpleDeserializers();

        deserializers.addDeserializer(IPv4Address.class, IPAddressDeserializer.ipv4());
        deserializers.addDeserializer(IPv6Address.class, IPAddressDeserializer.ipv6());
        deserializers.addDeserializer(IPAddress.class, IPAddressDeserializer.anyVersion());

        deserializers.addDeserializer(IPv4Subnet.class, SubnetDeserializer.ipv4());
        deserializers.addDeserializer(IPv6Subnet.class, SubnetDeserializer.ipv6());
        deserializers.addDeserializer(Subnet.class, SubnetDeserializer.anyVersion());

        context.addDeserializers(deserializers);
    }

    /**
     * Returns a module for IP addresses and ranges.
     *
     * @return A module for IP addresses and ranges.
     */
    public static IPModule instance() {
        return INSTANCE;
    }
}
