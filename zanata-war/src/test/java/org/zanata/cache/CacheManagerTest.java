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

import com.google.common.cache.CacheLoader;
import org.infinispan.manager.CacheContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CacheManagerTest {
    @Mock
    CacheContainer cacheContainer;

    CacheManager<Long, String> manager;

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
        manager = new MapBasedCacheManager<>();
        manager.initialize();
    }

    @After
    public void afterTest() {
        manager.stopListening();
    }

    @Test
    public void putCacheEntry() {
        manager.put(1L, "One");
        assertThat(manager.get(1L)).isEqualTo("One");
    }

    @Test
    public void getCacheEntryWithoutLoader() {
        assertThat(manager.get(1L)).isNull();
    }

    @Test
    public void submitUpdateToCache() throws Exception {
        manager.put(1L, "One");
        Future future =
                manager.submitUpdate(1L,
                        currentEntry -> "Modified: " + currentEntry);
        // Wait until the update has finished
        // For test purposes, this should be very quick
        future.get();
        assertThat(manager.get(1L)).isEqualTo("Modified: One");
    }

    @Test
    public void submitMultipleUpdatesToCache() throws Exception {
        manager.put(1L, "One");
        Future future =
                manager.submitUpdate(1L,
                        currentEntry -> "Modified: " + currentEntry);
        future =
                manager.submitUpdate(1L,
                        currentEntry -> "Second: " + currentEntry);
        future =
                manager.submitUpdate(1L,
                        currentEntry -> "Third: " + currentEntry);
        // Wait until the update has finished
        // For test purposes, this should be very quick
        future.get();
        assertThat(manager.get(1L)).isEqualTo("Third: Second: Modified: One");
    }

    private static class MapBasedCacheManager<K, V> extends CacheManager<K, V> {
        @Override
        protected String getCacheName() {
            return "undefined";
        }

        @Override
        protected CacheLoader<K, V> buildCacheLoader() {
            return null;
        }

        @Override
        public void initialize() {
            wrapper = new InfinispanCacheWrapper<K, V>(getCacheName(),
                    new InfinispanTestCacheContainer(), buildCacheLoader());
        }
    }
}
