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
package org.obm.push.minig.imap.command;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class CreateCommandTest {
	
	@Test
	public void testCreateMailboxWithAccent() {
		assertThat(new CreateCommand("déèÄç@placements").getImapCommand())
			.isEqualTo("CREATE \"d&AOkA6ADEAOc-@placements\"");
	}
	
	@Test
	public void testCreateMailboxChinese() {
		assertThat(new CreateCommand("中国记录").getImapCommand())
			.isEqualTo("CREATE \"&Ti1W,YuwX1U-\"");
	}

	@Test
	public void testCreateMailboxChineseWithUserPath() {
		assertThat(new CreateCommand("user/myusername/中国记录").getImapCommand())
			.isEqualTo("CREATE \"user/myusername/&Ti1W,YuwX1U-\"");
	}

	@Test
	public void testCreateMailboxWithPartitionNull() {
		assertThat(new CreateCommand("user/myusername/mailbox", null).getImapCommand())
			.isEqualTo("CREATE \"user/myusername/mailbox\"");
	}
	
	@Test
	public void testCreateMailboxWithPartitionEmpty() {
		assertThat(new CreateCommand("user/myusername/mailbox", "").getImapCommand())
		.isEqualTo("CREATE \"user/myusername/mailbox\"");
	}

	@Test
	public void testCreateMailboxWithPartition() {
		assertThat(new CreateCommand("user/myusername/mailbox", "partition_name").getImapCommand())
			.isEqualTo("CREATE \"user/myusername/mailbox\" partition_name");
	}

	@Test
	public void testCreateMailboxWithPartitionUTF7() {
		assertThat(new CreateCommand("user/myusername/mailbox", "partition_name_accent_é").getImapCommand())
			.isEqualTo("CREATE \"user/myusername/mailbox\" partition_name_accent_&AOk-");
	}
	
}