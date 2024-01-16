package io.github.hungvm90.redlock;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class RedLock {

    private final List<RedisClient> redisClients;
    private final int retryCount;
    private final double clockDriftFactor = 0.01;
    private final int quorum;

    private final Random random;


    public RedLock(List<RedisClient> redisClients) {
        this(redisClients, 3);
    }

    public RedLock(List<RedisClient> redisClients, int retryCount) {
        this.redisClients = redisClients;
        this.retryCount = retryCount;
        quorum = redisClients.size() / 2 + 1;
        if (redisClients.size() < quorum) {
            throw new IllegalStateException("number of redis server is not enough");
        }
        random = new SecureRandom();
    }

    private void sleep() {
        try {
            Thread.sleep(Math.abs(random.nextLong()) % 200 + 50);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Lock> lock(String resource, long leaseTime) {
        int retry = 0;
        String value = UUID.randomUUID().toString();
        long drift = (long) ((leaseTime * clockDriftFactor) + 2);
        while (retry < retryCount) {
            int n = 0;
            long startTime = System.currentTimeMillis();
            for (RedisClient client : redisClients) {
                try {
                    if (client.set(resource, value, leaseTime)) {
                        n++;
                    }
                } catch (Exception e) {
                    log.error("lock error {}", e.getMessage(), e);
                }
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            long validity = leaseTime - elapsedTime - drift;
            if (validity > 0 && n >= quorum) {
                return Optional.of(Lock.of(resource, value, validity));
            } else {
                for (RedisClient client : redisClients) {
                    try {
                        client.deleteIfEqual(resource, value);
                    } catch (Exception e) {
                        //pass
                    }
                }
                retry++;
                sleep();
            }
        }
        return Optional.empty();
    }

    public void unlock(String resource, String value) {
        for (RedisClient client : redisClients) {
            try {
                client.deleteIfEqual(resource, value);
            } catch (Exception e) {
                log.error("unlock error {}", e.getMessage(), e);
            }
        }
    }
}
