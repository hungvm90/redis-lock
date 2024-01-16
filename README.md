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