package io.github.hungvm90.redlock;

import lombok.Data;

@Data
public class Lock {
    private String resource;
    private String value;
    private long validity;

    public static Lock of(String resource, String value, long validity) {
        Lock lock = new Lock();
        lock.setResource(resource);
        lock.setValue(value);
        lock.setValidity(validity);
        return lock;
    }
}
