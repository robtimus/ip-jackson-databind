module com.github.robtimus.ip.jackson.databind {
    requires transitive com.github.robtimus.ip.utils;
    requires transitive com.fasterxml.jackson.databind;

    exports com.github.robtimus.net.ip.jackson.databind;

    opens com.github.robtimus.net.ip.jackson.databind to com.fasterxml.jackson.databind;

    provides com.fasterxml.jackson.databind.Module with com.github.robtimus.net.ip.jackson.databind.IPModule;
}
