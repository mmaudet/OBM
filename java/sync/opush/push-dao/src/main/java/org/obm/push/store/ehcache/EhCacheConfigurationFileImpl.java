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

import org.obm.configuration.utils.IniFile;
import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

public class EhCacheConfigurationFileImpl implements EhCacheConfiguration {

	private static final String configFilePath = "/etc/opush/ehcache_conf.ini";
	private final IniFile iniFile;
	
	@Inject
	@VisibleForTesting EhCacheConfigurationFileImpl(IniFile.Factory factory) {
		iniFile = factory.build(configFilePath);
	}
	
	@Override
	public int maxMemoryInMB() {
		int value = iniFile.getIntValue("maxMemoryInMB", -1);
		if (value > JvmUtils.maxRuntimeJvmMemoryInMB()) {
			throw new IllegalStateException("maxMemoryInMB is higher than JVM Xmx value");
		}
		if (value > 0) {
			return value;
		}
		throw new IllegalStateException("illegal maxMemoryInMB value");
	}

	@Override
	public Percentage percentageAllowedToCache(String cacheName) {
		Integer value = iniFile.getIntegerValue(cacheName, null);
		if (value == null || value == 0) {
			return Percentage.UNDEFINED;
		}
		return Percentage.of(value);
	}
}
