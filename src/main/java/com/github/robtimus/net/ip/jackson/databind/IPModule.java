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

import com.github.robtimus.net.ip.IPAddress;
import com.github.robtimus.net.ip.IPRange;
import com.github.robtimus.net.ip.IPv4Address;
import com.github.robtimus.net.ip.IPv4Range;
import com.github.robtimus.net.ip.IPv4Subnet;
import com.github.robtimus.net.ip.IPv6Address;
import com.github.robtimus.net.ip.IPv6Range;
import com.github.robtimus.net.ip.IPv6Subnet;
import com.github.robtimus.net.ip.Subnet;
import tools.jackson.core.Version;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.module.SimpleDeserializers;
import tools.jackson.databind.module.SimpleSerializers;

/**
 * A module that adds support for serializing and deserializing IP addresses and ranges values.
 *
 * @author Rob Spoor
 */
public final class IPModule extends JacksonModule {

    private static final IPModule INSTANCE = new IPModule();

    /**
     * Creates a new module.
     * <p>
     * This constructor should not be used directly; it exists for allowing the module to be found using {@link MapperBuilder#findAndAddModules()}
     * only.
     */
    public IPModule() {
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

        serializers.addSerializer(IPAddressSerializer.IPv4.INSTANCE);
        serializers.addSerializer(IPAddressSerializer.IPv6.INSTANCE);
        serializers.addSerializer(IPAddressSerializer.AnyVersion.INSTANCE);

        serializers.addSerializer(SubnetSerializer.IPv4.INSTANCE);
        serializers.addSerializer(SubnetSerializer.IPv6.INSTANCE);
        serializers.addSerializer(SubnetSerializer.AnyVersion.INSTANCE);

        serializers.addSerializer(IPRangeSerializer.IPv4.INSTANCE);
        serializers.addSerializer(IPRangeSerializer.IPv6.INSTANCE);
        serializers.addSerializer(IPRangeSerializer.AnyVersion.INSTANCE);

        context.addSerializers(serializers);
    }

    private void setupDeserializers(SetupContext context) {
        SimpleDeserializers deserializers = new SimpleDeserializers();

        deserializers.addDeserializer(IPv4Address.class, IPAddressDeserializer.IPv4.INSTANCE);
        deserializers.addDeserializer(IPv6Address.class, IPAddressDeserializer.IPv6.INSTANCE);
        deserializers.addDeserializer(IPAddress.class, IPAddressDeserializer.AnyVersion.INSTANCE);

        deserializers.addDeserializer(IPv4Subnet.class, SubnetDeserializer.IPv4.INSTANCE);
        deserializers.addDeserializer(IPv6Subnet.class, SubnetDeserializer.IPv6.INSTANCE);
        deserializers.addDeserializer(Subnet.class, SubnetDeserializer.AnyVersion.INSTANCE);

        deserializers.addDeserializer(IPv4Range.class, IPRangeDeserializer.IPv4.INSTANCE);
        deserializers.addDeserializer(IPv6Range.class, IPRangeDeserializer.IPv6.INSTANCE);
        deserializers.addDeserializer(IPRange.class, IPRangeDeserializer.AnyVersion.INSTANCE);

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
