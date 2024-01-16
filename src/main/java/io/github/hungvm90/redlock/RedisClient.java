package io.github.hungvm90.redlock;

public interface RedisClient {
    boolean set(String key, String value, long ttlInMilli);
    void deleteIfEqual(String key, String value);
}
