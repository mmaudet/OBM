/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.http.ContentType;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

public class ConfigurationResourceTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new TestImapArchiveModules.Simple()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));

	@Inject
	private H2InMemoryDatabase db;

	@Inject WebServer server;
	
	@Before
	public void setUp() throws Exception {
		Operation operation =
				Operations.sequenceOf(
						Operations.deleteAllFrom("domain", "mail_archive"),
						Operations.insertInto("domain")
						.columns("domain_id", "domain_name", "domain_label", "domain_uuid")
						.values(654, "my_domain_name", "my_domain.local", "a6af9131-60b6-4e3a-a9f3-df5b43a89309")
						.build(),
						Operations.insertInto("domain")
						.columns("domain_id", "domain_name", "domain_label", "domain_uuid")
						.values(321, "my_domain_name", "my_domain.local", "21aeb670-f49e-428a-9d0c-f11f5feaa688")
						.build(),
						Operations.insertInto("mail_archive")
						.columns("mail_archive_domain_id", 
								"mail_archive_activated", 
								"mail_archive_repeat_kind", 
								"mail_archive_day_of_week", 
								"mail_archive_day_of_month", 
								"mail_archive_day_of_year", 
								"mail_archive_hour", 
								"mail_archive_minute")
						.values(321, Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32)
						.build());

		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void getDomainConfigurationShouldReturnADefaultConfiguration() {
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(false))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void getDomainConfigurationShouldReturnStoredConfiguration() {
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("21aeb670-f49e-428a-9d0c-f11f5feaa688"),
				"enabled", equalTo(true))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/21aeb670-f49e-428a-9d0c-f11f5feaa688/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldThrowExceptionWhenBadInputs() {
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldCreateWhenNoData() {
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.WEEKLY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.TUESDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(10).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(100).getDayOfYear();
		domainConfigurationDto.hour = 11;
		domainConfigurationDto.minute = 32;
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.header("Location", endsWith("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration"))
			.statusCode(Status.CREATED.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(true),
				"dayOfWeek", equalTo(DayOfWeek.TUESDAY.getSpecificationValue()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldReturnNoContentWhenUpdating() {
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("21aeb670-f49e-428a-9d0c-f11f5feaa688");
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.WEEKLY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.WEDNESDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(10).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(100).getDayOfYear();
		domainConfigurationDto.hour = 11;
		domainConfigurationDto.minute = 32;
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/21aeb670-f49e-428a-9d0c-f11f5feaa688/configuration");
		
		given()
			.port(server.getHttpPort())
			.param("login", "cyrus")
			.param("password", "cyrus")
			.param("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("21aeb670-f49e-428a-9d0c-f11f5feaa688"),
				"enabled", equalTo(true),
				"dayOfWeek", equalTo(DayOfWeek.WEDNESDAY.getSpecificationValue()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/21aeb670-f49e-428a-9d0c-f11f5feaa688/configuration");
	}
}