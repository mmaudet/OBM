/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.cyrus.imap.admin;

import java.util.Collection;

import org.obm.configuration.ConfigurationService;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.push.minig.imap.impl.MailboxNameUTF7Converter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserPassword;

public class CyrusManagerImpl implements CyrusManager {

	public static class Factory implements CyrusManager.Factory {

		private final Connection.Factory connectionFactory;
		private final StoreClient.Factory storeClientFactory;
		private final ConfigurationService configurationService;

		@Inject
		public Factory(Connection.Factory connectionFactory, StoreClient.Factory storeClientFactory, ConfigurationService configurationService) {
			this.connectionFactory = connectionFactory;
			this.storeClientFactory = storeClientFactory;
			this.configurationService = configurationService;
		}

		@Override
		public CyrusManagerImpl create(String hostname, String login, UserPassword password) throws IMAPException, ImapTimeoutException {
			StoreClient storeClient = storeClientFactory.create(hostname, login, password.getStringValue().toCharArray());
			storeClient.login(false);
			return new CyrusManagerImpl(connectionFactory.create(storeClient), configurationService);
		}
		
	}

	private final Connection conn;
	private final ConfigurationService configurationService;

	@VisibleForTesting
	CyrusManagerImpl(Connection conn, ConfigurationService configurationService) {
		this.conn = conn;
		this.configurationService = configurationService;
	}

	@Override
	public void create(ObmUser obmUser) throws ImapOperationException, ConnectionException, ImapTimeoutException {
		String user = obmUser.getLogin();
		String domain = obmUser.getDomain().getName();

		if (configurationService.isCyrusPartitionEnabled()) {
			conn.createUserMailboxes(Partition.fromObmDomain(domain), buildUserImapPaths(user, domain));
		} else {
			conn.createUserMailboxes(buildUserImapPaths(user, domain));
		}
	}

	@Override
	public void delete(ObmUser obmUser) throws ImapOperationException, ConnectionException, ImapTimeoutException {
		final String domain = obmUser.getDomain().getName();
		String user = obmUser.getLogin();
		conn.delete(ImapPath.builder().user(user).domain(domain).build());
	}
	
	@Override
	public void setAcl(ObmUser obmUser, String identifier, Acl acl) throws ImapOperationException, ConnectionException, ImapTimeoutException {
		final String domain = obmUser.getDomain().getName();
		String user = obmUser.getLogin();
		conn.setAcl(
				ImapPath.builder().user(user).domain(domain).build(),
				identifier,
				acl);
	}

	@Override
	public void applyQuota(ObmUser obmUser) throws MailboxNotFoundException, ImapTimeoutException {
		conn.setQuota(ImapPath
				.builder()
				.user(obmUser.getLogin())
				.domain(obmUser.getDomain().getName())
				.build(), Quota.valueOf(obmUser.getMailQuota()));
	}

	@Override
	public void close() throws ImapTimeoutException {
		conn.shutdown();
	}

	private Collection<ImapPath> buildUserImapPaths(String user, String domain) {
		ImmutableList.Builder<ImapPath> paths = ImmutableList.builder();

		// Always create the Inbox...
		paths.add(ImapPath.builder().user(user).domain(domain).build());
		// ...and also create custom folders, as per the configuration
		for (String folder : configurationService.getUserMailboxDefaultFolders()) {
			paths.add(ImapPath.builder().user(user).domain(domain).pathFragment(MailboxNameUTF7Converter.decode(folder)).build());
		}

		return paths.build();
	}

}
