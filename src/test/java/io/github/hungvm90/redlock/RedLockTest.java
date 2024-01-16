package io.github.hungvm90.redlock;

import io.github.hungvm90.redlock.adapter.JedisClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class RedLockTest {

    @Test
    void testSingleHost() {
        List<RedisClient> clients = new LinkedList<>();
        clients.add(new JedisClient("redis", 6379, 100));
        RedLock redLock = new RedLock(clients);

        var lock = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNotNull(lock);
        assertEquals("xxx", lock.getResource());
        assertTrue(lock.getValue() != null && !lock.getValue().isEmpty());
        assertTrue(lock.getValidity() > 0);

        var lock2 = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNull(lock2);

        redLock.unlock(lock.getResource(), lock.getValue());

        lock2 = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNotNull(lock2);
        assertEquals("xxx", lock2.getResource());
        assertTrue(lock2.getValue() != null && !lock2.getValue().isEmpty());
        assertTrue(lock2.getValidity() > 0);

        redLock.unlock(lock2.getResource(), lock2.getValue());
    }

    @Test
    void testMultipleHost() {
        List<RedisClient> clients = new LinkedList<>();
        clients.add(new JedisClient("redis", 6379, 100));
        clients.add(new JedisClient("redis", 6370, 100));
        clients.add(new JedisClient("redis", 6371, 100));
        RedLock redLock = new RedLock(clients);

        var lock = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNotNull(lock);
        assertEquals("xxx", lock.getResource());
        assertTrue(lock.getValue() != null && !lock.getValue().isEmpty());
        assertTrue(lock.getValidity() > 0);

        var lock2 = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNull(lock2);

        redLock.unlock(lock.getResource(), lock.getValue());

        lock2 = redLock.lock("xxx", 5 * 1000).orElse(null);
        assertNotNull(lock2);
        assertEquals("xxx", lock2.getResource());
        assertTrue(lock2.getValue() != null && !lock2.getValue().isEmpty());
        assertTrue(lock2.getValidity() > 0);

        redLock.unlock(lock2.getResource(), lock2.getValue());
    }

    @Test
    void testConcurrentLock() throws Exception {
        List<RedisClient> clients = new LinkedList<>();
        clients.add(new JedisClient("redis", 6379, 100));
        clients.add(new JedisClient("redis", 6370, 100));
        clients.add(new JedisClient("redis", 6371, 100));
        RedLock redLock = new RedLock(clients, 100);
        AtomicInteger success = new AtomicInteger(0);
        ExecutorService executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            executor.execute(() -> {
                try {
                    log.info("lock");
                    var lock = redLock.lock("xxxyyy", 1000 * 10);
                    if (lock.isPresent()) {
                        log.info("get lock " + lock.get());
                        success.incrementAndGet();
                        redLock.unlock(lock.get().getResource(), lock.get().getValue());
                    } else {
                        log.info("fail to get lock");
                    }
                } catch (Exception e) {
                    log.error("ex to get lock {}", e.getMessage(), e);
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        assertEquals(100, success.get());
    }
}