Redis Redlock for java

```java
List<RedisClient> clients = new LinkedList<>();
clients.add(new JedisClient("redis", 6379, 100));
RedLock redLock = new RedLock(clients);

var lock = redLock.lock("xxx", 5 * 1000).orElse(null);
assertNotNull(lock);
assertEquals("xxx", lock.getResource());
assertTrue(lock.getValue() != null && !lock.getValue().isEmpty());
assertTrue(lock.getValidity() > 0);
```
Or you can implement RedisClient interface with your redis driver

```java
public static class SpringRedisClient implements RedisClient {
    private final RedisTemplate<String, String> redisTemplate;

    public SpringRedisClient(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean set(String s, String s1, long l) {
        return redisTemplate.opsForValue().setIfAbsent(s, s1, l, TimeUnit.MILLISECONDS);
    }

    @Override
    public void deleteIfEqual(String s, String s1) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        redisTemplate.execute(RedisScript.of(script, Long.class), List.of(s), s1);
    }
}
```