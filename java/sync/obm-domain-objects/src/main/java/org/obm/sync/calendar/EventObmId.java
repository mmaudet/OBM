/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.calendar;

import java.io.Serializable;

import org.obm.push.utils.index.Indexed;

import com.google.common.base.Objects;

public class EventObmId implements Indexed<Integer>, Serializable {

	private final int obmId;

	public EventObmId(String obmId) {
		this(Integer.valueOf(obmId));
	}
	
	public EventObmId(int obmId) {
		this.obmId = obmId;
	}
	
	public int getObmId() {
		return obmId;
	}
	
	@Override
	public Integer getIndex() {
		return getObmId();
	}
	
	public String serializeToString() {
		return String.valueOf(obmId);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EventObmId) {
			EventObmId other = (EventObmId) obj;
			return Objects.equal(obmId, other.obmId);
		}
		return false;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(obmId);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("obmId", obmId)
			.toString();
	}
	
}
