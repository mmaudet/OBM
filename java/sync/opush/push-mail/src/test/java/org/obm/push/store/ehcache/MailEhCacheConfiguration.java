/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.store.ehcache;

import java.util.Map;

import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;

import com.google.common.collect.ImmutableMap;

public class MailEhCacheConfiguration implements EhCacheConfiguration {
	
	private Map<String, Percentage> stores;
	
	public MailEhCacheConfiguration() {
		stores = ImmutableMap.<String, Percentage>builder()
				.put(EhCacheStores.MONITORED_COLLECTION_STORE, Percentage.of(5))
				.put(EhCacheStores.SYNCED_COLLECTION_STORE, Percentage.of(5))
				.put(EhCacheStores.UNSYNCHRONIZED_ITEM_STORE, Percentage.of(5))
				.put(EhCacheStores.MAIL_SNAPSHOT_STORE, Percentage.of(70))
				.put(EhCacheStores.MAIL_WINDOWING_INDEX_STORE, Percentage.of(5))
				.put(EhCacheStores.MAIL_WINDOWING_CHUNKS_STORE, Percentage.of(5))
				.put(EhCacheStores.SYNC_KEYS_STORE, Percentage.of(5))
				.build();
	}
	
	@Override
	public int maxMemoryInMB() {
		return 10;
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		Percentage defaultValue = stores.get(cacheName);
		if (defaultValue != null) {
			return defaultValue;
		}
		return Percentage.UNDEFINED;
	}
	
	@Override
	public Map<String, Percentage> percentageAllowedToCaches() {
		return stores;
	}

	@Override
	public int statsSampleToRecordCount() {
		return 10;
	}

	@Override
	public int statsShortSamplingTimeInSeconds() {
		return 1;
	}
	
	@Override
	public int statsMediumSamplingTimeInSeconds() {
		return 10;
	}
	
	@Override
	public int statsLongSamplingTimeInSeconds() {
		return 60;
	}

	@Override
	public int statsSamplingTimeStopInMinutes() {
		return 10;
	}

	@Override
	public long timeToLiveInSeconds() {
		return 60;
	}

	@Override
	public TransactionalMode transactionalMode() {
		return TransactionalMode.XA;
	}
}