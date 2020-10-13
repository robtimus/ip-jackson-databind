# ip-jackson-databind

Provides support for serializing and deserializing IP addresses and ranges using Jackson. All you need to do is register a module. This can be done in two ways:

1. Using `ObjectMapper.registerModule`:

        mapper.registerModule(IPModule.instance());

2. Using `ObjectMapper.findAndRegisterModules()`. This will register not just an instance of `IPModule`, but any other module that's made available through the Service Provider Interface (SPI) mechanism:

        mapper.findAndRegisterModules();

No matter which way you choose, it will automatically allow all instances of [IPAddress](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPAddress.html), [IPRange](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPRange.html) and [Subnet](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/Subnet.html) to be serialized and deserialized, without the need for any custom serializer or deserializer.
