/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.service.impl;

import com.google.common.annotations.VisibleForTesting;
import org.infinispan.manager.CacheContainer;
import org.zanata.cache.impl.VersionStatisticsCache;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.VersionStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.IServiceLocator;
import org.zanata.util.ServiceLocator;
import org.zanata.util.Zanata;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("versionStateCacheImpl")
@javax.enterprise.context.ApplicationScoped
public class VersionStateCacheImpl implements VersionStateCache {

    @Inject
    private VersionStatisticsCache versionStatisticsCache;

    @Inject
    private IServiceLocator serviceLocator;

    @Override
    public void textFlowStateUpdated(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            TextFlowTargetStateEvent event) {
        VersionLocaleKey key =
                new VersionLocaleKey(event.getProjectIterationId(),
                        event.getLocaleId());

        versionStatisticsCache.submitUpdate(key, cacheEntry -> {
            // TODO try to do this without ServiceLocator (e.g. pass the
            // number of changed words in the event)
            TextFlowDAO textFlowDAO =
                    ServiceLocator.instance().getInstance(TextFlowDAO.class);
            HTextFlow textFlow = textFlowDAO.findById(event.getTextFlowId());

            cacheEntry.decrement(event.getPreviousState(),
                    textFlow.getWordCount().intValue());
            cacheEntry.increment(event.getNewState(),
                    textFlow.getWordCount().intValue());
            return cacheEntry;
        });
    }

    @Override
    public WordStatistic getVersionStatistics(Long projectIterationId,
        LocaleId localeId) {
        return versionStatisticsCache.getWithLoader(new VersionLocaleKey(
                projectIterationId, localeId));
    }

    @Override
    public void clearVersionStatsCache(Long versionId) {
        LocaleDAO localeDAO = serviceLocator.getInstance(LocaleDAO.class);
        for (HLocale locale : localeDAO.findAll()) {
            VersionLocaleKey key =
                    new VersionLocaleKey(versionId, locale.getLocaleId());
            versionStatisticsCache.remove(key);
        }
    }
}
