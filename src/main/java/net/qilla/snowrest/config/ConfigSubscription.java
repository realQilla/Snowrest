package net.qilla.snowrest.config;

@FunctionalInterface
public interface ConfigSubscription {

    void onChange(SnowrestConfig config);
}
