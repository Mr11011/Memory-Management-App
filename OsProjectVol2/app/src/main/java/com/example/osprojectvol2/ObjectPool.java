package com.example.osprojectvol2;

import java.util.ArrayList;
import java.util.List;

public class ObjectPool<T> {
    private final List<T> pool;
    private final int maxSize;
    private final ObjectFactory<T> factory;

    public ObjectPool(int maxSize, ObjectFactory<T> factory) {
        this.maxSize = maxSize;
        this.factory = factory;
        this.pool = new ArrayList<>();
    }

    public synchronized T acquire() {
        if (pool.isEmpty()) {
            return factory.create();
        } else {
            return pool.remove(pool.size() - 1);
        }
    }

    public synchronized void release(T object) {
        if (pool.size() < maxSize) {
            pool.add(object);
        }
    }

    public interface ObjectFactory<T> {
        T create();
    }
}