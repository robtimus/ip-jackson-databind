# ip-jackson-databind
[![Maven Central](https://img.shields.io/maven-central/v/com.github.robtimus/ip-jackson-databind)](https://search.maven.org/artifact/com.github.robtimus/ip-jackson-databind)
[![Build Status](https://github.com/robtimus/ip-jackson-databind/actions/workflows/build.yml/badge.svg)](https://github.com/robtimus/ip-jackson-databind/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Aip-jackson-databind&metric=alert_status)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Aip-jackson-databind)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.github.robtimus%3Aip-jackson-databind&metric=coverage)](https://sonarcloud.io/summary/overall?id=com.github.robtimus%3Aip-jackson-databind)
[![Known Vulnerabilities](https://snyk.io/test/github/robtimus/ip-jackson-databind/badge.svg)](https://snyk.io/test/github/robtimus/ip-jackson-databind)

Provides support for serializing and deserializing IP addresses and ranges using Jackson. All you need to do is register a module. This can be done in two ways:

1. Using `ObjectMapper.registerModule`:

    ```java
    mapper.registerModule(IPModule.instance());
    ```

2. Using `ObjectMapper.findAndRegisterModules()`. This will register not just an instance of `IPModule`, but any other module that's made available through the Service Provider Interface (SPI) mechanism:

    ```java
    mapper.findAndRegisterModules();
    ```

No matter which way you choose, it will automatically allow all instances of [IPAddress](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPAddress.html), [IPRange](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/IPRange.html) and [Subnet](https://robtimus.github.io/ip-utils/apidocs/com/github/robtimus/net/ip/Subnet.html) to be serialized and deserialized, without the need for any custom serializer or deserializer.
