/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.provisioning.processing.impl;

import static com.jayway.restassured.RestAssured.given;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.cyrus.imap.admin.CyrusImapService;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.EntityRightDao;
import org.obm.domain.dao.PGroupDao;
import org.obm.domain.dao.PUserDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.provisioning.CommonDomainEndPointEnvTest;
import org.obm.provisioning.Group;
import org.obm.provisioning.Group.Id;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.obm.provisioning.dao.exceptions.GroupRecursionException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.ldap.client.LdapService;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;
import org.obm.push.mail.IMAPException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.utils.DateUtils;
import org.obm.satellite.client.Configuration;
import org.obm.satellite.client.Connection;
import org.obm.satellite.client.SatelliteService;
import org.obm.sync.dao.EntityId;
import org.obm.sync.date.DateProvider;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.util.Modules;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.profile.Module;
import fr.aliacom.obm.common.profile.ModuleCheckBoxStates;
import fr.aliacom.obm.common.profile.Profile;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;


@RunWith(SlowGuiceRunner.class)
@GuiceModule(BatchProcessorImplTest.Env.class)
public class BatchProcessorImplTest extends CommonDomainEndPointEnvTest {

	public static class Env extends AbstractModule {

		@Override
		protected void configure() {
			install(Modules.override(new CommonDomainEndPointEnvTest.Env()).with(new AbstractModule() {

				@Override
				protected void configure() {
					bind(BatchProcessor.class).to(BatchProcessorImpl.class);
					bind(BatchTracker.class).to(BatchTrackerImpl.class);
				}

			}));
		}

	}

	@Inject
	private BatchProcessor processor;
	@Inject
	private UserSystemDao userSystemDao;
	@Inject
	private DateProvider dateProvider;
	@Inject
	private SatelliteService satelliteService;
	@Inject
	private LdapService ldapService;
	@Inject
	private CyrusImapService cyrusService;
	@Inject
	private EntityRightDao entityRightDao;
	@Inject
	private ObmHelper obmHelper;
	@Inject
	private PUserDao pUserDao;
	@Inject
	private PGroupDao pGroupDao;

	private final Date date = DateUtils.date("2013-08-01T12:00:00");

	private final ObmSystemUser obmSatelliteUser = ObmSystemUser
			.builder()
			.id(1)
			.login("obmsatelliterequest")
			.password("secret")
			.build();

	private final ObmSystemUser obmCyrusUser = ObmSystemUser
			.builder()
			.id(2)
			.login("cyrus")
			.password("secret")
			.build();
	private final Group usersGroup = Group
			.builder()
			.uid(Group.Id.valueOf(1))
			.gid(UserDao.DEFAULT_GID)
			.name("Users")
			.build();
	private final Profile profile = Profile
			.builder()
			.id(ProfileId.valueOf("1"))
			.name(ProfileName.valueOf("user"))
			.level(0)
			.domain(domain)
			.defaultCheckBoxState(Module.CALENDAR, ModuleCheckBoxStates
					.builder()
					.module(Module.CALENDAR)
					.build())
			.defaultCheckBoxState(Module.MAILBOX, ModuleCheckBoxStates
					.builder()
					.module(Module.MAILBOX)
					.build())
			.build();

	private final ObmDomain domainWithImapAndLdap = ObmDomain
			.builder()
			.name("domain")
			.id(1)
			.uuid(ObmDomainUuid.of("a3443822-bb58-4585-af72-543a287f7c0e"))
			.host(ServiceProperty.IMAP, ObmHost
					.builder()
					.name("Cyrus")
					.ip("127.0.0.1")
					.build())
			.host(ServiceProperty.IMAP, ObmHost
					.builder()
					.name("NewCyrus")
					.ip("127.0.0.1")
					.build())
			.host(ServiceProperty.LDAP, ObmHost
					.builder()
					.name("OpenLDAP")
					.ip("127.0.0.1")
					.build())
			.build();

	@Test
	public void testProcessWithInvalidJSONData() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/")
						.verb(HttpVerb.POST)
						.body(	"{" +
									"\"invalid\": \"json\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.error("org.obm.provisioning.exception.ProcessingException: Cannot parse ObmUser object from request body {\"invalid\": \"json\"}.")
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessCreateUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/")
						.verb(HttpVerb.POST)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"," +
										"\"mails\":[\"john@domain\"]" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		final ObmUser user = ObmUser
				.builder()
				.login("user1")
				.lastName("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").build())
				.build();
		final ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		groupDao.addUser(domain, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);
		expectLdapCreateUser(userFromDao, usersGroup);
		expectCyrusCreateMailbox(userFromDao);

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void expectSetDefaultRights(ObmUser user) throws Exception {
		expect(profileDao.getUserProfile(user)).andReturn(profile);
		expect(obmHelper.fetchEntityId("Calendar", 1)).andReturn(EntityId.valueOf(2));
		expect(obmHelper.fetchEntityId("Mailbox", 1)).andReturn(EntityId.valueOf(3));
		entityRightDao.grantRights(eq(EntityId.valueOf(2)), isNull(EntityId.class), isA(Set.class));
		expectLastCall();
		entityRightDao.grantRights(eq(EntityId.valueOf(3)), isNull(EntityId.class), isA(Set.class));
		expectLastCall();
	}

	private void expectPUserDaoDelete(ObmUser user) throws DaoException  {
		pUserDao.delete(user);
		expectLastCall();
	}
	
	private void expectPUserDaoArchive(ObmUser user) throws DaoException {
		pUserDao.archive(user);
		expectLastCall();
	}

	private void expectPUserDaoInsert(ObmUser user) throws DaoException  {
		pUserDao.insert(user);
		expectLastCall();
	}
	
	private void expectPGroupDaoDelete(Group group) throws DaoException  {
		pGroupDao.delete(group);
		expectLastCall();
	}

	private void expectPGroupDaoInsert(Group group) throws DaoException  {
		pGroupDao.insert(group);
		expectLastCall();
	}
	
	@Test
	public void testProcessCreateUserWhenDefaultGroupDoesntExist() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/")
						.verb(HttpVerb.POST)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"," +
										"\"mails\":[\"john@domain\"]" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.login("user1")
				.lastName("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").build())
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(null);
		expectLastCall();

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.error("org.obm.provisioning.exception.ProcessingException: Default group with GID 1000 not found for domain domain.")
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void expectCyrusCreateMailbox(final ObmUser user)
			throws DaoException, IMAPException {
		CyrusManager cyrusManager = expectCyrusBuild();
		cyrusManager.create(user);
		expectLastCall().once();
		cyrusManager.setAcl(user, "anyone", Acl.builder().user("user1").rights("p").build());
		expectLastCall().once();
		cyrusManager.applyQuota(user);
		expectLastCall();
		expectCyrusShutDown(cyrusManager);
	}

	@Test
	public void testProcessCreateUserAndUpdateSatellite() throws Exception {

		ObmDomain domainWithSmtpIn = ObmDomain.builder()
				.from(domainWithImapAndLdap)
				.host(ServiceProperty.SMTP_IN, ObmHost
						.builder()
						.name("Postfix")
						.ip("127.0.0.1")
						.build())
				.build();

		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/")
						.verb(HttpVerb.POST)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domainWithSmtpIn)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Connection satelliteConnection = mocksControl.createMock(Connection.class);

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.login("user1")
				.lastName("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithSmtpIn)
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithSmtpIn)
				.build();
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domainWithSmtpIn, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		groupDao.addUser(domainWithSmtpIn, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);

		expectLdapCreateUser(userFromDao, usersGroup);
		expect(userSystemDao.getByLogin("obmsatelliterequest")).andReturn(obmSatelliteUser);
		expect(satelliteService.create(isA(Configuration.class), eq(domainWithSmtpIn))).andReturn(satelliteConnection);
		satelliteConnection.updateMTA();
		expectLastCall();
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoInsert(userFromDao);

		mocksControl.replay();

		createBatchWithOneUserAndCommit();

		mocksControl.verify();
	}

	private void createBatchWithOneUserAndCommit() {
		given()
			.auth().basic("username@domain", "password")
			.post("/batches/");
		given()
			.auth().basic("username@domain", "password")
			.post("/batches/1/users");
		given()
            .auth().basic("username@domain", "password")
            .put("/batches/1");
	}

	private void createBatchWithOneUserUpdateAndCommit() {
		given()
			.auth().basic("username@domain", "password")
			.post("/batches/");
		given()
			.auth().basic("username@domain", "password")
			.put("/batches/1/users/1");
		given()
            .auth().basic("username@domain", "password")
            .put("/batches/1");
	}

	private void createBatchWithOneUserPatchAndCommit() {
		given()
			.auth().basic("username@domain", "password")
			.post("/batches/");
		given()
			.auth().basic("username@domain", "password")
			.patch("/batches/1/users/1");
		given()
	        .auth().basic("username@domain", "password")
	        .put("/batches/1");
	}

	private void expectBatchCreationAndRetrieval(Batch batch) throws Exception {
		expectSuccessfulAuthenticationAndFullAuthorization();
		expectSuccessfulAuthenticationAndFullAuthorization();
		expectSuccessfulAuthenticationAndFullAuthorization();

		expect(batchDao.create(isA(Batch.class))).andReturn(batch);
		expect(batchDao.get(batchId(1))).andReturn(batch);
		expect(batchDao.addOperation(eq(batchId(1)), isA(Operation.class))).andReturn(batch);
		expect(batchDao.get(batchId(1))).andReturn(batch);
		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(batchDao.get(batchId(1))).andReturn(batch);
	}

	private void expectLdapCreateUser(ObmUser userToAdd ,Group defaultGroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.createUser(userToAdd);
		expectLastCall();
		ldapManager.addUserToDefaultGroup(userToAdd.getDomain(), defaultGroup, userToAdd);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapModifyUser(ObmUser user, ObmUser oldUser) {
		LdapManager ldapManager = expectLdapBuild();

		ldapManager.modifyUser(user, oldUser);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}
	
	private void expectLdapdeleteUser(ObmUser userToRemove, Group defaultGroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.removeUserFromDefaultGroup(userToRemove.getDomain(), defaultGroup, userToRemove);
		expectLastCall();
		ldapManager.deleteUser(userToRemove);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapCreateGroup(Group group) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.createGroup(group, domain);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}
	
	private void expectLdapDeleteGroup(Group group) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.deleteGroup(domain, group);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapModifyGroup(Group group, Group oldGroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.modifyGroup(domain, group, oldGroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapAddGroupToGroup(Group group, Group subgroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.addSubgroupToGroup(domain, group, subgroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private void expectLdapRemoveGroupFromGroup(Group group, Group subgroup) {
		LdapManager ldapManager = expectLdapBuild();
		ldapManager.removeSubgroupFromGroup(domain, group, subgroup);
		expectLastCall();
		ldapManager.shutdown();
		expectLastCall();
	}

	private LdapManager expectLdapBuild() {
		LdapManager ldapManager = mocksControl.createMock(LdapManager.class);

		expect(ldapService.buildManager(isA(LdapConnectionConfig.class))).andReturn(ldapManager);
		return ldapManager;
	}

	@Test
	public void testCreateUserWithNoEmail() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/users/")
						.verb(HttpVerb.POST)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login("user1")
				.lastName("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.build();
		final ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.login("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.create(user)).andReturn(userFromDao);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		groupDao.addUser(domain, usersGroup.getUid(), userFromDao);
		expectLastCall();
		expectSetDefaultRights(userFromDao);
		expectLdapCreateUser(userFromDao, usersGroup);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoInsert(userFromDao);
		
		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteUserWithFalseExpunge() throws SQLException, DaoException, BatchNotFoundException, UserNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/users/extIdUser1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.archive(user);
		expectLastCall();
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(groupDao.getAllGroupsForUserExtId(user.getDomain(), user.getExtId())).andReturn(Collections.EMPTY_SET);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoArchive(user);
		
		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessDeleteUserWithTrueExpunge()
			throws SQLException, DaoException, BatchNotFoundException, UserNotFoundException, IMAPException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/users/extIdUser1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.param(Request.EXPUNGE_KEY, "true")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").ip("127.0.0.1").build())
				.email("user1@domain")
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.delete(user);
		expectLastCall();
		expectDeleteUserMailbox(user);
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(groupDao.getAllGroupsForUserExtId(user.getDomain(), user.getExtId())).andReturn(Collections.EMPTY_SET);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoDelete(user);
		
		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}
	
	@Test
	public void testProcessDeleteUserWithoutMailWithTrueExpunge()
			throws SQLException, DaoException, BatchNotFoundException, UserNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(Request
						.builder()
						.resourcePath("/users/extIdUser1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.param(Request.EXPUNGE_KEY, "true")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		final ObmUser user = ObmUser
				.builder()
				.login("user1")
				.password("secret")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").ip("127.0.0.1").build())
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		final UserExtId extId = UserExtId.valueOf("extIdUser1");
		expect(userDao.getByExtId(extId, domain)).andReturn(user);
		userDao.delete(user);
		expectLastCall();
		expect(groupDao.getByGid(domain, UserDao.DEFAULT_GID)).andReturn(usersGroup);
		expectLdapdeleteUser(user, usersGroup);
		expect(groupDao.getAllGroupsForUserExtId(user.getDomain(), user.getExtId())).andReturn(Collections.EMPTY_SET);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		expectPUserDaoDelete(user);
		
		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PUT)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"," +
										"\"mails\":[\"john@domain\"]" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domainWithImapAndLdap)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.lastName("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.mailHost(ObmHost.builder().name("Cyrus").ip("127.0.0.1").build())
				.build();
		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.mailHost(ObmHost.builder().name("Cyrus").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(userFromDao);
		expectLdapModifyUser(userFromDao, userFromDao);
		CyrusManager cyrusManager = expectCyrusBuild();
		expectApplyQuota(cyrusManager, userFromDao);
		expectCyrusShutDown(cyrusManager);
		expectPUserDaoDelete(userFromDao);
		expectPUserDaoInsert(userFromDao);
		
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserCannotChangeLogin() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PUT)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1new\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"," +
										"\"mails\":[\"john@domain\"]," +
										"\"mail_server\":\"Cyrus\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domainWithImapAndLdap)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.mailHost(ObmHost.builder().name("Cyrus").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.timecommit(date)
						.error("org.obm.provisioning.exception.ProcessingException: Cannot change user login")
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserCannotChangeArchivedState() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PUT)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"mails\":[\"john@domain\"]," +
										"\"archived\": true" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domainWithImapAndLdap)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.mailHost(ObmHost.builder().name("Cyrus").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.timecommit(date)
						.error("org.obm.provisioning.exception.ProcessingException: Cannot change user archived state")
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyUserCannotChangeMailHost() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PUT)
						.body(	"{" +
										"\"id\": \"extIdUser1\"," +
										"\"login\": \"user1\"," +
										"\"lastname\": \"user1\"," +
										"\"profile\": \"user\"," +
										"\"password\": \"secret\"," +
										"\"mails\":[\"john@domain\"]," +
										"\"mail_server\":\"NewCyrus\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domainWithImapAndLdap)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser userFromDao = ObmUser
				.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domainWithImapAndLdap)
				.mailHost(ObmHost.builder().name("Cyrus").ip("127.0.0.1").build())
				.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domainWithImapAndLdap)).andReturn(userFromDao);

		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.timecommit(date)
						.error("org.obm.provisioning.exception.ProcessingException: Cannot change user mail host")
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserUpdateAndCommit();

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchUser() throws Exception {
		Date date = DateUtils.date("2013-08-01T12:00:00");
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER)
				.request(org.obm.provisioning.beans.Request
						.builder()
						.resourcePath("/users/extIdUser1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PATCH)
						.body(	"{" +
										"\"lastname\": \"user1\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());

		ObmUser.Builder builder = ObmUser.builder()
				.uid(1)
				.entityId(EntityId.valueOf(1))
				.login("user1")
				.password("secret")
				.emailAndAliases("john@domain")
				.profileName(ProfileName.valueOf("user"))
				.extId(UserExtId.valueOf("extIdUser1"))
				.domain(domain)
				.mailHost(ObmHost.builder().name("host").ip("127.0.0.1").build());

		ObmUser user = builder
				.lastName("user1")
				.build();

		ObmUser userFromDao = builder.build();

		expectDomain();
		expectBatchCreationAndRetrieval(batchBuilder.build());
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain)).andReturn(userFromDao);
		expect(userDao.update(user)).andReturn(userFromDao);
		expectLdapModifyUser(userFromDao, userFromDao);
		CyrusManager cyrusManager = expectCyrusBuild();
		expectApplyQuota(cyrusManager, userFromDao);
		expectCyrusShutDown(cyrusManager);
		expectPUserDaoDelete(userFromDao);
		expectPUserDaoInsert(userFromDao);
		
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		createBatchWithOneUserPatchAndCommit();

		mocksControl.verify();
	}

	private void expectDeleteUserMailbox(final ObmUser user) throws DaoException, IMAPException {
		CyrusManager cyrusManager = expectCyrusBuild();
		cyrusManager.setAcl(user, "cyrus", Acl.builder().user("user1").rights("lc").build());
		expectLastCall().once();
		cyrusManager.delete(user);
		expectLastCall().once();
		expectCyrusShutDown(cyrusManager);
	}

	private void expectApplyQuota(CyrusManager cyrusManager, ObmUser user) {
		cyrusManager.applyQuota(user);
		expectLastCall();
	}

	private void expectCyrusShutDown(CyrusManager cyrusManager) {
		cyrusManager.shutdown();
		expectLastCall().once();
	}

	private CyrusManager expectCyrusBuild() throws DaoException, IMAPException {
		expect(userSystemDao.getByLogin("cyrus")).andReturn(obmCyrusUser);
		CyrusManager cyrusManager = mocksControl.createMock(CyrusManager.class);
		expect(cyrusService.buildManager("127.0.0.1", "cyrus", "secret")).andReturn(cyrusManager);
		return cyrusManager;
	}

	@Test
	public void testProcessDeleteGroup() throws DaoException, BatchNotFoundException, GroupNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		groupDao.delete(domain, extId);
		expectLastCall();
		expectLdapDeleteGroup(groupFromDao);
		expectPGroupDaoDelete(groupFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessModifyGroup() throws DaoException, BatchNotFoundException, GroupNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.verb(HttpVerb.PUT)
						.body(
								"{" +
										"\"id\": \"extIdGroup1\"," +
										"\"name\": \"group1\"," +
										"\"description\": \"newDescription\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();
		final Group newGroup = Group.builder()
				.name("group1")
				.extId(extId)
				.description("newDescription")
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.update(domain, newGroup)).andReturn(newGroup);
		expectLdapModifyGroup(newGroup, groupFromDao);
		expectPGroupDaoDelete(newGroup);
		expectPGroupDaoInsert(newGroup);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);
		
		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}
	
	@Test
	public void testProcessCreateGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(Request
						.builder()
						.resourcePath("/groups/")
						.verb(HttpVerb.POST)
						.body(
								"{" +
										"\"id\": \"newGroupExtId\"," +
										"\"name\": \"newGroup\"," +
										"\"description\": \"description\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");
		
		final GroupExtId extId = GroupExtId.valueOf("newGroupExtId");
		final Group groupFromDao = Group.builder()
										.name("newGroup")
										.gid(1)
										.uid(Id.valueOf(1))
										.description("description")
										.extId(extId)
										.build();
		final Group newGroup = Group.builder()
				.name("newGroup")
				.description("description")
				.extId(extId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.create(domain, newGroup)).andReturn(groupFromDao);
		expectLdapCreateGroup(groupFromDao);
		expectPGroupDaoInsert(groupFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessRenameGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.verb(HttpVerb.PUT)
						.body(
								"{" +
										"\"id\": \"extIdGroup1\"," +
										"\"name\": \"newName\"," +
										"\"description\": \"description\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.ERROR)
						.error("org.obm.provisioning.exception.ProcessingException: Cannot rename a group.")
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessPatchGroup() throws Exception {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.verb(HttpVerb.PATCH)
						.body(
								"{" +
										"\"name\": \"newName\"," +
										"\"description\": \"newDescription\"" +
								"}")
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.gid(1)
										.uid(Id.valueOf(1))
										.description("description")
										.extId(extId)
										.build();
		final Group newGroup = Group.builder()
				.name("newName")
				.gid(1)
				.uid(Id.valueOf(1))
				.description("newDescription")
				.extId(extId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.update(domain, newGroup)).andReturn(newGroup);
		expectLdapModifyGroup(newGroup, groupFromDao);
		expectPGroupDaoDelete(newGroup);
		expectPGroupDaoInsert(newGroup);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessAddUserToGroup()
			throws DaoException, BatchNotFoundException, GroupNotFoundException, UserNotFoundException, SQLException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER_MEMBERSHIP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1/users/extIdUser1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.PUT)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();
		final ObmUser userFromDao = ObmUser.builder()
										.extId(UserExtId.valueOf("extIdUser1"))
										.domain(domain)
										.login("log")
										.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain)).andReturn(userFromDao);
		groupDao.addUser(domain, extId, userFromDao);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expectLdapAddUserToAllParentsOfGroup(groupFromDao, userFromDao);
		expectPGroupDaoDelete(groupFromDao);
		expectPGroupDaoInsert(groupFromDao);
		
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	private void expectLdapAddUserToAllParentsOfGroup(Group group, ObmUser userToAdd) throws DaoException, GroupNotFoundException {
		ImmutableSet<Group.Id> groupIds = ImmutableSet.of(Group.Id.valueOf(1), Group.Id.valueOf(2));

		expect(groupDao.listParents(domain, group.getExtId())).andReturn(groupIds);
		LdapManager ldapManager = expectLdapBuild();

		 Group group1 = Group.builder()
					.uid(Group.Id.valueOf(1))
					.build();

		 Group group2 = Group.builder()
					.uid(Group.Id.valueOf(2))
					.build();

		expect(groupDao.get(Group.Id.valueOf(1))).andReturn(group1);
		expect(groupDao.get(Group.Id.valueOf(2))).andReturn(group2);

		ldapManager.addUserToGroup(domain, group1, userToAdd);
		expectLastCall();
		ldapManager.addUserToGroup(domain, group2, userToAdd);
		expectLastCall();

		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testProcessDeleteUserFromGroup()
			throws DaoException, BatchNotFoundException, GroupNotFoundException, UserNotFoundException, SQLException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.USER_MEMBERSHIP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1/users/extIdUser1")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.param(Request.USERS_ID_KEY, "extIdUser1")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();
		final ObmUser userFromDao = ObmUser.builder()
										.extId(UserExtId.valueOf("extIdUser1"))
										.domain(domain)
										.login("log")
										.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		expect(userDao.getByExtId(UserExtId.valueOf("extIdUser1"), domain)).andReturn(userFromDao);
		groupDao.removeUser(domain, extId, userFromDao);
		expectLastCall();
		expectLdapDeleteUserFromAllParentGroup(groupFromDao, userFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	private void expectLdapDeleteUserFromAllParentGroup(Group group, ObmUser userToDelete) throws GroupNotFoundException, DaoException {
		ImmutableSet<Group.Id> groupIds = ImmutableSet.of(Group.Id.valueOf(1), Group.Id.valueOf(2));

		expect(groupDao.listParents(domain, group.getExtId())).andReturn(groupIds);
		LdapManager ldapManager = expectLdapBuild();

		 Group group1 = Group.builder()
					.uid(Group.Id.valueOf(1))
					.build();

		 Group group2 = Group.builder()
					.uid(Group.Id.valueOf(2))
					.build();

		expect(groupDao.get(Group.Id.valueOf(1))).andReturn(group1);
		expect(groupDao.get(Group.Id.valueOf(2))).andReturn(group2);

		ldapManager.removeUserFromGroup(domain, group1, userToDelete);
		expectLastCall();
		ldapManager.removeUserFromGroup(domain, group2, userToDelete);
		expectLastCall();

		ldapManager.shutdown();
		expectLastCall();
	}

	@Test
	public void testProcessAddGroupToGroup()
			throws DaoException, BatchNotFoundException, GroupNotFoundException, GroupRecursionException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP_MEMBERSHIP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1/subgroups/extIdGroup2")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.param(Request.SUBGROUPS_ID_KEY, "extIdGroup2")
						.verb(HttpVerb.PUT)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();
		final GroupExtId subgroupExtId = GroupExtId.valueOf("extIdGroup2");
		final Group subgroupFromDao = Group.builder()
				.name("group2")
				.extId(subgroupExtId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		groupDao.addSubgroup(domain, extId, subgroupExtId);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.get(domain, subgroupExtId)).andReturn(subgroupFromDao);
		expectLdapAddGroupToGroup(groupFromDao, subgroupFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}

	@Test
	public void testProcessRemoveGroupFromGroup()
			throws DaoException, BatchNotFoundException, GroupNotFoundException, DomainNotFoundException {
		Operation.Builder opBuilder = Operation
				.builder()
				.id(operationId(1))
				.status(BatchStatus.IDLE)
				.entityType(BatchEntityType.GROUP_MEMBERSHIP)
				.request(Request
						.builder()
						.resourcePath("/groups/extIdGroup1/subgroups/extIdGroup2")
						.param(Request.GROUPS_ID_KEY, "extIdGroup1")
						.param(Request.SUBGROUPS_ID_KEY, "extIdGroup2")
						.verb(HttpVerb.DELETE)
						.build());
		Batch.Builder batchBuilder = Batch
				.builder()
				.id(batchId(1))
				.domain(domain)
				.status(BatchStatus.IDLE)
				.operation(opBuilder.build());
		Date date = DateUtils.date("2013-08-01T12:00:00");

		final GroupExtId extId = GroupExtId.valueOf("extIdGroup1");
		final Group groupFromDao = Group.builder()
										.name("group1")
										.extId(extId)
										.build();
		final GroupExtId subgroupExtId = GroupExtId.valueOf("extIdGroup2");
		final Group subgroupFromDao = Group.builder()
				.name("group2")
				.extId(subgroupExtId)
				.build();

		expect(dateProvider.getDate()).andReturn(date).anyTimes();
		groupDao.removeSubgroup(domain, extId, subgroupExtId);
		expectLastCall();
		expect(groupDao.get(domain, extId)).andReturn(groupFromDao);
		expect(groupDao.get(domain, subgroupExtId)).andReturn(subgroupFromDao);
		expectLdapRemoveGroupFromGroup(groupFromDao, subgroupFromDao);
		expect(batchDao.update(batchBuilder
				.operation(opBuilder
						.status(BatchStatus.SUCCESS)
						.timecommit(date)
						.build())
				.status(BatchStatus.SUCCESS)
				.timecommit(date)
				.build())).andReturn(null);

		mocksControl.replay();

		processor.process(batchBuilder.build());

		mocksControl.verify();
	}
}
