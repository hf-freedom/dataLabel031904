package com.datalabel.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LocalCache {
    
    private static final LocalCache INSTANCE = new LocalCache();
    
    private final Map<Long, Object> dataMap = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> typeMap = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    private LocalCache() {}
    
    public static LocalCache getInstance() {
        return INSTANCE;
    }
    
    public synchronized Long generateId() {
        return idGenerator.getAndIncrement();
    }
    
    public void put(Long id, Object obj) {
        dataMap.put(id, obj);
        typeMap.put(obj.getClass().getName(), obj.getClass());
    }
    
    public Object get(Long id) {
        return dataMap.get(id);
    }
    
    public void remove(Long id) {
        dataMap.remove(id);
    }
    
    public <T> List<T> getAll(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Object obj : dataMap.values()) {
            if (clazz.isInstance(obj)) {
                result.add(clazz.cast(obj));
            }
        }
        return result;
    }
    
    public void clear() {
        dataMap.clear();
        typeMap.clear();
    }
    
    public boolean containsKey(Long id) {
        return dataMap.containsKey(id);
    }
}
