/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.ConfigurationService;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Mailing;
import org.obm.sync.ObmSmtpService;
import org.obm.sync.base.EmailAddress;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomain;


public class MailerImplTest {

	private IMocksControl control;
	
	private ConfigurationService configurationService;
	private ObmSmtpService obmSmtpService;
	private Logger logger;
	private ObmDomain domain;
	private MailerImpl testee;

	
	@Before
	public void setup() {
		control = createControl();
		configurationService = control.createMock(ConfigurationService.class);
		obmSmtpService = control.createMock(ObmSmtpService.class);
		logger = control.createMock(Logger.class);
		
		domain = ObmDomain.builder().name("mydomain.org").build();
		testee = new MailerImpl(configurationService, obmSmtpService, logger);
	}
	
	@Test
	public void text() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");

		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("mydomain.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("/");
		
		control.replay();
		String text = testee.text(domain, runId, State.TERMINATED);
		control.verify();
		
		assertThat(text).isEqualTo(
				"IMAP Archive treatment has ended with state "+ State.TERMINATED + " for the domain mydomain.org\r\n" + 
				"Logs are available at https://mydomain.org/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize() + "\r\n");
	}
	
	@Test
	public void link() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");
		
		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("any.host.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("/prefix");
		
		control.replay();
		String link = testee.link(runId);
		control.verify();
		
		assertThat(link).isEqualTo("https://any.host.org/prefix/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize());
	}
	
	@Test
	public void linkWhenNoPrefix() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");
		
		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("any.host.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("");
		
		control.replay();
		String link = testee.link(runId);
		control.verify();
		
		assertThat(link).isEqualTo("https://any.host.org/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize());
	}
	
	@Test
	public void linkWhenPrefixWithSlash() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938");
		
		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("any.host.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("/");
		
		control.replay();
		String link = testee.link(runId);
		control.verify();
		
		assertThat(link).isEqualTo("https://any.host.org/imap_archive/imap_archive_index.php?action=log_page&run_id=" + runId.serialize());
	}
	
	@Test
	public void from() throws Exception {
		control.replay();
		Address from = testee.from(domain);
		control.verify();
		
		assertThat(from).isEqualTo(new InternetAddress("IMAP Archive notifier <imap-archive@mydomain.org>"));
	}
	
	@Test
	public void fromShouldLogThenReturnNullWhenAnyError() {
		IllegalStateException exception = new IllegalStateException();

		ObmDomain domainMock = control.createMock(ObmDomain.class);
		expect(domainMock.getName()).andThrow(exception);
		
		logger.error("Cannot build From address", exception);
		expectLastCall();
		
		control.replay();
		Address from = testee.from(domainMock);
		control.verify();

		assertThat(from).isNull();
	}
	
	@Test
	public void fromShouldAcceptWhenDomainHasOnePart() throws Exception {
		control.replay();
		Address from = testee.from(ObmDomain.builder().name("blabla").build());
		control.verify();

		assertThat(from).isEqualTo(new InternetAddress("IMAP Archive notifier <imap-archive@blabla>"));
	}
	
	@Test
	public void internetAddressesShouldBeEmptyWhenEmptyMailing() {
		Mailing mailing = Mailing.empty();
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).isEmpty();
	}
	
	@Test
	public void internetAddressesShouldBeEqualsToMailing() throws Exception {
		Mailing mailing = Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org")));
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).containsOnly(new InternetAddress("user@mydomain.org"), new InternetAddress("user2@mydomain.org"));
	}
	
	@Test
	public void internetAddressesShouldIgnoreInvalidAddress() throws Exception {
		Mailing mailing = Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain  .org"), EmailAddress.loginAtDomain("user2@mydomain.org")));
		
		logger.error(anyObject(String.class), anyObject(Exception.class));
		expectLastCall();
		
		control.replay();
		Address[] internetAddresses = testee.internetAddresses(mailing);
		control.verify();
		
		assertThat(internetAddresses).containsOnly(new InternetAddress("user2@mydomain.org"));
	}
	
	@Test
	public void sendShouldDoNothingWhenNoEmailAddress() throws Exception {
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.empty());
		control.verify();
	}
	
	@Test
	public void sendShouldSend() throws Exception {
		obmSmtpService.sendEmail(anyObject(MimeMessage.class), anyObject(Session.class));
		expectLastCall();

		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("any.host.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("/prefix");
		
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org"))));
		control.verify();
	}
	
	@Test(expected=MessagingException.class)
	public void sendShouldSendThrowWhenExceptionAppend() throws Exception {
		obmSmtpService.sendEmail(anyObject(MimeMessage.class), anyObject(Session.class));
		expectLastCall().andThrow(new MessagingException());

		expect(configurationService.getObmUIUrlProtocol()).andReturn("https");
		expect(configurationService.getObmUIUrlHost()).andReturn("any.host.org");
		expect(configurationService.getObmUIUrlPrefix()).andReturn("/prefix");
		
		logger.error(anyObject(String.class), anyObject(Exception.class));
		expectLastCall();
		
		control.replay();
		testee.send(domain, ArchiveTreatmentRunId.from("94fc9ba5-422e-48b0-86f0-2d00e218a938"), State.TERMINATED, Mailing.from(ImmutableList.of(EmailAddress.loginAtDomain("user@mydomain.org"), EmailAddress.loginAtDomain("user2@mydomain.org"))));
		control.verify();
	}
}
