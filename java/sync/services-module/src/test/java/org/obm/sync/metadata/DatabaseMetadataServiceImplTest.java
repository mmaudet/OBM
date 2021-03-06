/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.metadata;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.sync.dao.TableDescription;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@GuiceModule(DatabaseMetadataServiceImplTest.Env.class)
@RunWith(GuiceRunner.class)
public class DatabaseMetadataServiceImplTest {

	public static class Env extends AbstractModule {

		private final IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DatabaseMetadataDao.class);
			bindWithMock(TableDescription.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
		
	}
 	
	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private DatabaseMetadataDao metadataDao;
	
	@Inject
	private TableDescription tableDescription;
	
	private DatabaseMetadataServiceImpl dms;
	
	private static final String TABLE = "event";
	
	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	@Test
	public void testGetTableDescription() throws SQLException {
		expect(metadataDao.getResultSetMetadata(TABLE)).andReturn(tableDescription).once();
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		
		TableDescription tableDescription = dms.getTableDescriptionOf(TABLE);
		
		assertThat(tableDescription).isNotNull();
	}
	
	@Test
	public void testGetTableDescriptionTwiceInARowCallDaoOnce() throws SQLException {
		expect(metadataDao.getResultSetMetadata(TABLE)).andReturn(tableDescription).once();
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		
		TableDescription tableDescription = dms.getTableDescriptionOf(TABLE);
		TableDescription tableDescription2 = dms.getTableDescriptionOf(TABLE);
		
		assertThat(tableDescription).isNotNull();
		assertThat(tableDescription2).isEqualTo(tableDescription);
	}
	
	@Test(expected=UncheckedExecutionException.class)
	public void testGetTableDescriptionFailsWithSQLException() throws SQLException {
		expect(metadataDao.getResultSetMetadata(TABLE)).andThrow(new SQLException());
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		dms.getTableDescriptionOf(TABLE);
	}
}
