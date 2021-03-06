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
package org.obm.domain.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dao.utils.DaoTestModule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseRule;
import org.obm.dao.utils.H2TestClass;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.sync.Right;
import org.obm.sync.dao.EntityId;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
@GuiceModule(DaoTestModule.class)
public class EntityRightDaoJdbcImplTest implements H2TestClass {

	@Inject
	private EntityRightDaoJdbcImpl dao;

	@Rule public H2InMemoryDatabaseRule dbRule = new H2InMemoryDatabaseRule(this, "sql/initial.sql");
	@Inject H2InMemoryDatabase db;

	@Override
	public H2InMemoryDatabase getDb() {
		return db;
	}
	
	@Test(expected = DaoException.class)
	public void testGrantRightsWhenEntityIdDoesntExist() throws Exception {
		dao.grantRights(EntityId.valueOf(666), null, ImmutableSet.<Right> of());
	}

	@Test(expected = DaoException.class)
	public void testGrantRightsWhenConsumerIdDoesntExist() throws Exception {
		dao.grantRights(EntityId.valueOf(1), EntityId.valueOf(666), ImmutableSet.<Right> of());
	}

	@Test
	public void testGetAfterSetPublicRights() throws Exception {
		ImmutableSet<Right> rights = ImmutableSet.of(Right.ACCESS, Right.READ);

		dao.grantRights(EntityId.valueOf(1), null, rights);

		assertThat(dao.getPublicRights(EntityId.valueOf(1))).isEqualTo(rights);
	}

	@Test
	public void testGetAfterSetRights() throws Exception {
		ImmutableSet<Right> rights = ImmutableSet.of(Right.ACCESS, Right.READ, Right.ADMIN);

		dao.grantRights(EntityId.valueOf(1), EntityId.valueOf(2), rights);

		assertThat(dao.getRights(EntityId.valueOf(1), EntityId.valueOf(2))).isEqualTo(rights);
	}

	@Test
	public void testGetAfterDeletePublicRights() throws Exception {
		ImmutableSet<Right> rights = ImmutableSet.of(Right.ACCESS, Right.READ);

		dao.grantRights(EntityId.valueOf(1), null, rights);
		dao.deletePublicRights(EntityId.valueOf(1));

		assertThat(dao.getPublicRights(EntityId.valueOf(1))).isEmpty();
	}

	@Test
	public void testGetAfterDeleteRights() throws Exception {
		ImmutableSet<Right> rights = ImmutableSet.of(Right.ACCESS, Right.READ, Right.ADMIN);

		dao.grantRights(EntityId.valueOf(1), EntityId.valueOf(2), rights);
		dao.deleteRights(EntityId.valueOf(1), EntityId.valueOf(2));

		assertThat(dao.getRights(EntityId.valueOf(1), EntityId.valueOf(2))).isEmpty();
	}

}
