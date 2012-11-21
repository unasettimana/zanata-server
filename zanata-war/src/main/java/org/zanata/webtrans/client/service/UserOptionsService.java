/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.service;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;


/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Singleton
public class UserOptionsService
{
   private static final int DEFAULT_DOC_LIST_PAGE_SIZE = 25;
   private static final int DEFAULT_EDITOR_PAGE_SIZE = 25;
   private static final boolean DEFAULT_SHOW_ERROR = false;
   private static final boolean DEFAULT_SHOW_SAVE_APPROVED_WARNING = true;
   private static final boolean DEFAULT_FILTER = false;
   private static final boolean DEFAULT_DISPLAY_BUTTONS = true;
   private static final boolean DEFAULT_ENTER_SAVES_APPROVED = false;

   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final UserConfigHolder configHolder;

   @Inject
   public UserOptionsService(EventBus eventBus, CachingDispatchAsync dispatcher, UserConfigHolder configHolder)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
   }

   public void persistOptionChange(Map<UserOptions, String> optsMap)
   {
      SaveOptionsAction action = new SaveOptionsAction(optsMap);

      dispatcher.execute(action, new AsyncCallback<SaveOptionsResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Could not save user options"));
         }

         @Override
         public void onSuccess(SaveOptionsResult result)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, "Saved user options"));
         }
      });
   }

   public HashMap<UserOptions, String> getCommonOptions()
   {
      HashMap<UserOptions, String> configMap = new HashMap<UserOptions, String>();
      configMap.put(UserOptions.ShowErrors, Boolean.toString(configHolder.getState().isShowError()));

      return configMap;
   }

   public HashMap<UserOptions, String> getDocumentListOptions()
   {
      HashMap<UserOptions, String> configMap;
      configMap = getCommonOptions();
      configMap.put(UserOptions.DocumentListPageSize, Integer.toString(configHolder.getState().getDocumentListPageSize()));

      return configMap;
   }

   public HashMap<UserOptions, String> getEditorOptions()
   {
      HashMap<UserOptions, String> configMap;
      configMap = getCommonOptions();
      configMap.put(UserOptions.DisplayButtons, Boolean.toString(configHolder.getState().isDisplayButtons()));
      configMap.put(UserOptions.EnterSavesApproved, Boolean.toString(configHolder.getState().isEnterSavesApproved()));
      configMap.put(UserOptions.EditorPageSize, Integer.toString(configHolder.getState().getEditorPageSize()));

      configMap.put(UserOptions.TranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByTranslated()));
      configMap.put(UserOptions.NeedReviewMessageFilter, Boolean.toString(configHolder.getState().isFilterByNeedReview()));
      configMap.put(UserOptions.UntranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByUntranslated()));
      configMap.put(UserOptions.Navigation, configHolder.getState().getNavOption().toString());
      configMap.put(UserOptions.ShowSaveApprovedWarning, Boolean.toString(configHolder.getState().isShowSaveApprovedWarning()));

      return configMap;
   }


   public void loadCommonOptions()
   {
      configHolder.setShowError(DEFAULT_SHOW_ERROR);
   }

   public void loadDocumentListDefaultOptions()
   {
      // default options
      loadCommonOptions();

      configHolder.setDocumentListPageSize(DEFAULT_DOC_LIST_PAGE_SIZE);
   }

   public void loadEditorDefaultOptions()
   {
      // default options
      loadCommonOptions();

      // default options
      configHolder.setDisplayButtons(DEFAULT_DISPLAY_BUTTONS);
      configHolder.setEnterSavesApproved(DEFAULT_ENTER_SAVES_APPROVED);
      configHolder.setFilterByTranslated(DEFAULT_FILTER);
      configHolder.setFilterByNeedReview(DEFAULT_FILTER);
      configHolder.setFilterByUntranslated(DEFAULT_FILTER);
      configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
      configHolder.setEditorPageSize(DEFAULT_EDITOR_PAGE_SIZE);
      configHolder.setShowSaveApprovedWarning(DEFAULT_SHOW_SAVE_APPROVED_WARNING);
   }

   public UserConfigHolder getConfigHolder()
   {
      return configHolder;
   }


}