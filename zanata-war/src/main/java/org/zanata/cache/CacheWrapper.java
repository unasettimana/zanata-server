package org.zanata.cache;

import java.util.function.BiFunction;

public interface CacheWrapper<K, V> {
    void put(K key, V value);

    V get(K key);

    V getWithLoader(K key);

    boolean remove(K key);

    /**
     * @see java.util.concurrent.ConcurrentMap#computeIfPresent(Object, BiFunction)
     */
    V computeIfPresent(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction);
}
