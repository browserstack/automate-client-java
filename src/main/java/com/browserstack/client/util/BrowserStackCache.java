package com.browserstack.client.util;

import java.util.HashMap;
import java.util.Map;


public class BrowserStackCache<K, T> {

    private static final long DEFAULT_EXPIRY_TIME = 86400000; // 1 day
    private static final int DEFAULT_MAX_INIT_ITEMS = 10;

    private final Map<K, BrowserStackCacheObject> cacheMap;
    private final long expiryTime;

    protected class BrowserStackCacheObject {
        public final T value;
        public final long created;

        protected BrowserStackCacheObject(T value) {
            this.value = value;
            this.created = System.currentTimeMillis();
        }
    }

    public BrowserStackCache() {
        this(DEFAULT_EXPIRY_TIME);
    }

    public BrowserStackCache(long expiryTime) {
        this(expiryTime, DEFAULT_MAX_INIT_ITEMS);
    }

    public BrowserStackCache(long expiryTime, int maxInitItems) {
        this.cacheMap = new HashMap<K, BrowserStackCacheObject>(maxInitItems);
        this.expiryTime = expiryTime;
    }

    public boolean containsKey(K key) {
        synchronized (cacheMap) {
            return cacheMap.containsKey(key);
        }
    }

    public void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new BrowserStackCacheObject(value));
        }
    }

    public T get(K key) {
        synchronized (cacheMap) {
            BrowserStackCacheObject c = cacheMap.get(key);
            if (c == null) {
                return null;
            }

            long timeDiff = System.currentTimeMillis() - c.created;
            if (timeDiff < 0 || timeDiff >= expiryTime) {
                cacheMap.remove(key);
                return null;
            }

            return c.value;
        }
    }

    public void remove(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
}