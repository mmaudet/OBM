/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.bean;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public enum FolderSyncStatus {
	
    OK("1"),
    SERVER_ERROR("6"), //An error occurred on the server.
    ACCESS_DENIED("7"),//Access denied.
    TIMED_OUT("8"),//The request timed out.                                      
    INVALID_SYNC_KEY("9"),//Synchronization key mismatch or invalid synchronization key.
    INVALID_REQUEST("10"),//Incorrectly formatted request.
    UNKNOW_ERROR("11");//An unknown error occurred.
    
    private String specificationValue;

	private FolderSyncStatus(String specificationValue) {
		this.specificationValue = specificationValue;
	}
    
    public String asXmlValue() {
    	return specificationValue;
	}
    
    public static FolderSyncStatus fromSpecificationValue(String specificationValue) {
    	if (specValueToEnum.containsKey(specificationValue)) {
    		return specValueToEnum.get(specificationValue);
    	}
		return null;
    }

    private static Map<String, FolderSyncStatus> specValueToEnum;
    
    static {
    	Builder<String, FolderSyncStatus> builder = ImmutableMap.builder();
    	for (FolderSyncStatus status : FolderSyncStatus.values()) {
    		builder.put(status.asXmlValue(), status);
    	}
    	specValueToEnum = builder.build();
    }
    
}
