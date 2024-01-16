package io.github.hungvm90.redlock.adapter;

import io.github.hungvm90.redlock.RedisClient;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Connection;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;

public class JedisClient implements RedisClient {
    private final JedisPooled jedis;

    public JedisClient(String host, int port, int timeout) {
        this(new GenericObjectPoolConfig<>(), host, port, timeout);
    }

    public JedisClient(GenericObjectPoolConfig<Connection> poolConfig, String host, int port, int timeout) {
        jedis = new JedisPooled(poolConfig, host, port, timeout);
    }

    @Override
    public boolean set(String key, String value, long ttlInMilli) {
        SetParams setParams = new SetParams().nx().px(ttlInMilli);
        String res = jedis.set(key, value, setParams);
        return "OK".equals(res);
    }

    @Override
    public void deleteIfEqual(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedis.eval(script, Collections.singletonList(key), Collections.singletonList(value));
    }
}
