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
package org.obm.push.backend;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class DataDelta {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private List<ItemChange> changes;
		private List<ItemDeletion> deletions;
		private Date syncDate;
		
		private Builder() {
			super();
		}

		public Builder changes(List<ItemChange> changes) {
			this.changes = changes;
			return this;
		}

		public Builder deletions(List<ItemDeletion> deletions) {
			this.deletions = deletions;
			return this;
		}

		public Builder syncDate(Date syncDate) {
			this.syncDate = syncDate;
			return this;
		}
		
		public DataDelta build() {
			Preconditions.checkNotNull(syncDate);
			if (changes == null) {
				changes = ImmutableList.of();
			}
			if (deletions == null) {
				deletions = ImmutableList.of();
			}
			return new DataDelta(changes, deletions, syncDate);
		}
		
	}
	
	private final List<ItemChange> changes;
	private final List<ItemDeletion> deletions;
	private final Date syncDate;
	
	private DataDelta(List<ItemChange> changes, List<ItemDeletion> deletions, Date syncDate) {
		this.syncDate = syncDate;
		this.changes = ImmutableList.copyOf(changes);
		this.deletions = ImmutableList.copyOf(deletions);
	}

	public List<ItemChange> getChanges() {
		return changes;
	}
	
	public List<ItemDeletion> getDeletions() {
		return deletions;
	}
	
	public Date getSyncDate() {
		return syncDate;
	}
	
	public int getItemEstimateSize() {
		int count = 0;
		if (changes != null) {
			count += changes.size();
		}
		if (deletions != null) {
			count += deletions.size();
		}
		return count;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("changes", changes)
			.add("deletions", deletions)
			.add("syncDate", syncDate)
			.toString();
	}
	
	public String statistics() {
		return String.format("%d changes, %d deletions, syncdate %tc", 
					changes.size(), deletions.size(), syncDate);
	}

	public static DataDelta newEmptyDelta(Date lastSync) {
		return DataDelta.builder()
				.changes(ImmutableList.<ItemChange>of())
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(lastSync)
				.build();
	}
}
