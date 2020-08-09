# ip-jackson-databind

Provides support for serializing and deserializing IP addresses and ranges using Jackson. All you need to do is register a module:

    Module module = IPModule.instance();
    mapper.registerModule(module);

This will automatically allow all instances of [IPAddress](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPAddress.html), [IPRange](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPRange.html) and [Subnet](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/Subnet.html) to be serialized and deserialized, without the need for any custom serializer or deserializer.
