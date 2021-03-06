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

import org.obm.ElementNotFoundException;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.ImapArchiveModule.LoggerModule;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveScheduledTreatment;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.RealRunTaskStatusChanged;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.ScheduledTask.State;

@Singleton
public class ArchiveDaoTracking implements ArchiveSchedulerBus.Client {

	private final Logger logger;
	private final DateTimeProvider dateTimeProvider;
	private final ArchiveTreatmentDao archiveTreatmentDao;

	@Inject 
	@VisibleForTesting ArchiveDaoTracking(@Named(LoggerModule.TASK) Logger logger,
			ArchiveTreatmentDao archiveTreatmentDao, DateTimeProvider dateTimeProvider) {
		
		this.logger = logger;
		this.archiveTreatmentDao = archiveTreatmentDao;
		this.dateTimeProvider = dateTimeProvider;
	}
	
	@Subscribe
	@Transactional
	public void onTreatmentStateChange(RealRunTaskStatusChanged event) {
		State state = event.state();
		if (State.NEW == state) {
			logger.info("A task has been created but the state {} is not tracked", State.NEW);
			return;
		}
		
		try {
			ArchiveConfiguration archiveConfiguration = event.task().getArchiveConfiguration();
			Optional<ArchiveTreatment> treatment = archiveTreatmentDao.find(archiveConfiguration.getRunId());
			if (!treatment.isPresent()) {
				insert(archiveConfiguration, state);
			} else {
				update(treatment.get(), archiveConfiguration, state);
			}
		} catch (Exception e) {
			logger.error("Cannot insert or update a treatment", e);
		}
	}

	private void insert(ArchiveConfiguration archiveConfiguration, State state) throws DaoException {
		if (State.WAITING != state) {
			logger.error("Only task with status {} can be created, received {}", State.WAITING, state);
			return;
		}
		
		logger.info("Insert a task as {} for domain {}, scheduled at {} with id {}", 
				ArchiveStatus.SCHEDULED, archiveConfiguration.getDomainId().get(), archiveConfiguration.getWhen(), archiveConfiguration.getRunId());
		
		archiveTreatmentDao.insert(ArchiveScheduledTreatment
				.forDomain(archiveConfiguration.getDomainId())
				.runId(archiveConfiguration.getRunId())
				.recurrent(archiveConfiguration.isRecurrent())
				.higherBoundary(archiveConfiguration.getHigherBoundary())
				.scheduledAt(archiveConfiguration.getWhen())
				.build());
	}

	private void update(ArchiveTreatment from, ArchiveConfiguration archiveConfiguration, State state) throws DaoException, ElementNotFoundException {
		ArchiveTreatment.Builder<ArchiveTreatment> treatmentBuilder = ArchiveTreatment
			.builder(archiveConfiguration.getDomainId())
			.runId(archiveConfiguration.getRunId())
			.recurrent(archiveConfiguration.isRecurrent())
			.higherBoundary(archiveConfiguration.getHigherBoundary())
			.scheduledAt(archiveConfiguration.getWhen());
		
		switch (state) {
		case NEW:
			return;
		case CANCELED:
			archiveTreatmentDao.remove(archiveConfiguration.getRunId());
			logger.info("A task has been canceled {}", archiveConfiguration.getRunId());
			return;
		case WAITING:
			treatmentBuilder.status(ArchiveStatus.SCHEDULED);
			break;
		case RUNNING:
			treatmentBuilder.status(ArchiveStatus.RUNNING)
				.startedAt(dateTimeProvider.now());
			break;
		case FAILED:
			treatmentBuilder.status(ArchiveStatus.ERROR)
				.startedAt(from.getStartTime())
				.terminatedAt(dateTimeProvider.now());
			break;
		case TERMINATED:
			treatmentBuilder.status(ArchiveStatus.SUCCESS)
				.startedAt(from.getStartTime())
				.terminatedAt(dateTimeProvider.now());
			break;
		}
		
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				state, archiveConfiguration.getDomainId().get(),  archiveConfiguration.getWhen(), archiveConfiguration.getRunId());
		
		archiveTreatmentDao.update(treatmentBuilder.build());
	}
}
