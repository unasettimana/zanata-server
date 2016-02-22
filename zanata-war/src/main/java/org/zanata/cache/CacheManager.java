/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheLoader;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.CacheContainer;
import org.zanata.util.Zanata;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Base class which implements common infrastructure around caches.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public abstract class CacheManager<K, V> {

    @Inject
    @Zanata
    private CacheContainer cacheContainer;

    private ExecutorService updatesExecutorService;

    protected CacheWrapper<K, V> wrapper;

    protected abstract CacheLoader<K, V> buildCacheLoader();

    protected abstract String getCacheName();

    // CDI constructor
    public CacheManager() {
    }

    @VisibleForTesting
    public CacheManager(CacheContainer cacheContainer) {
        this.cacheContainer = cacheContainer;
    }

    @PostConstruct
    public void initialize() {
        CacheLoader<K, V> loader = buildCacheLoader();
        if (loader == null) {
            wrapper = InfinispanCacheWrapper
                    .create(getCacheName(), cacheContainer);
        } else {
            wrapper = InfinispanCacheWrapper
                    .create(getCacheName(), cacheContainer, loader);
        }
    }

    protected void startListening() {
        if(updatesExecutorService == null) {
            updatesExecutorService = Executors.newSingleThreadExecutor();
        }
    }

    public V get(K key) {
        return wrapper.get(key);
    }

    public void put(K key, V value) {
        wrapper.put(key, value);
    }

    public V getWithLoader(K key) {
        return wrapper.getWithLoader(key);
    }

    public boolean remove(K key) {
        return wrapper.remove(key);
    }

    public Future<?> submitUpdate(K key, CacheOperation<V> op) {
        startListening(); // verify the service is up and running

        return updatesExecutorService.submit(() -> {
            wrapper.computeIfPresent(key, (k, v) -> {
                return op.apply(v);
            });
        });
    }

    @PreDestroy
    public void stopListening() {
        if (updatesExecutorService != null &&
                !updatesExecutorService.isShutdown()) {
            updatesExecutorService.shutdown();
        }
    }

    @FunctionalInterface
    public interface CacheOperation<V> extends Function<V, V> {
        @Override
        V apply(V currentEntry);
    }
}
