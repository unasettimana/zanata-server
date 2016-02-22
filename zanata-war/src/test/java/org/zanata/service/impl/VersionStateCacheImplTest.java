package org.zanata.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.cache.impl.VersionStatisticsCache;
import org.zanata.common.LocaleId;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.VersionLocaleKey;
import org.zanata.ui.model.statistic.WordStatistic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class VersionStateCacheImplTest {

    private VersionStateCacheImpl cache;

    private SeamAutowire seam = SeamAutowire.instance();

    @Mock
    private VersionStatisticsCache versionStatisticsCache;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        seam.reset()
            .ignoreNonResolvable()
            .use("versionStatisticsCache", versionStatisticsCache);
        cache = seam.autowire(VersionStateCacheImpl.class);
    }

    @Test
    public void getStatisticTest() throws Exception {

        Long projectIterationId = 1L;
        LocaleId localeId = LocaleId.EN_US;
        VersionLocaleKey key = new VersionLocaleKey(projectIterationId, localeId);

        WordStatistic wordStatistic = new WordStatistic(0, 11, 100, 4, 500);

        // When:
        when(versionStatisticsCache.getWithLoader(key)).thenReturn(wordStatistic);

        WordStatistic result = cache.getVersionStatistics(projectIterationId,
            localeId);

        // Then:
        verify(versionStatisticsCache).getWithLoader(key); // only load the value once
        assertThat(result, equalTo(wordStatistic));
    }
}
