package org.obm.sync.server.mailer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

public class EventChangeMailerENTest extends EventChangeMailerTest {

	@Override
	protected EventChangeMailer newEventChangeMailer() {
		return getLocaleEventChangeMailer(Locale.ENGLISH);
	}

	@Override
	protected Locale getLocale() {
		return Locale.ENGLISH;
	}	

	private List<String> getPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"from          : Nov 8, 2010 12:00 PM",
				"to            : Nov 8, 2010 12:45 PM",
				"subject       : Sprint planning OBM",
				"timezone      : Europe/Paris",
				"location      : A random location",
				"organizer     : Raphael ROUGERON",
				"created by    : Emmanuel SURLEAU"
		);
	}

	private ArrayList<String> getHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"From Nov 8, 2010 12:00 PM",
				"To Nov 8, 2010 12:45 PM",
				"Timezone Europe/Paris",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"Created by Emmanuel SURLEAU"
		);
	}

	private ArrayList<String> getRecurrentPlainMessage(String header) {
		return Lists.newArrayList(
				header,
				"from          : Nov 8, 2010",
				"to            : Nov 23, 2012",
				"time          : 12:00 PM - 12:45 PM",
				"timezone      : Europe/Paris",
				"recurrence    : Every 2 weeks [Monday, Wednesday, Thursday]",
				"subject       : Sprint planning OBM",
				"location      : A random location",
				"organizer     : Raphael ROUGERON"
		);
	}

	private ArrayList<String> getRecurrentHtmlMessage(String header) {
		return Lists.newArrayList(
				header,
				"From Nov 8, 2010",
				"To Nov 23, 2012",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"Time 12:00 PM - 12:45 PM",
				"Timezone Europe/Paris",
				"Recurrence kind Every 2 weeks [Monday, Wednesday, Thursday]"
		);
	}	

	@Override
	protected List<String> getInvitationPlainMessage() {
		return getPlainMessage("NEW APPOINTMENT");
	}

	@Override
	protected List<String> getInvitationHtmlMessage() {
		return getHtmlMessage("New appointment");
	}

	@Override
	protected List<String> getUpdatePlainMessage() {
		return Lists.newArrayList(
				"APPOINTMENT UPDATED !",
				"from Nov 8, 2010 12:00 PM",
				"to Nov 8, 2010 12:45 PM",
				"from          : Nov 8, 2010 1:00 PM",
				"to            : Nov 8, 2010 2:00 PM",
				"timezone      : Europe/Paris",
				"subject       : Sprint planning OBM",
				"location      : A random location",
				"organizer     : Raphael ROUGERON",
				"created by    : Emmanuel SURLEAU"
		);
	}

	@Override
	protected List<String> getUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Appointment updated",
				"from Nov 8, 2010 12:00 PM",
				"to Nov 8, 2010 12:45 PM",
				"From Nov 8, 2010 1:00 PM",
				"To Nov 8, 2010 2:00 PM",
				"Timezone Europe/Paris",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"Created by Emmanuel SURLEAU"
		);
	}

	@Override
	protected List<String> getCancelPlainMessage() {
		return getPlainMessage("APPOINTMENT CANCELED");
	}

	@Override
	protected ArrayList<String> getCancelHtmlMessage() {
		return getHtmlMessage("Appointment canceled");
	}

	@Override
	protected ArrayList<String> getRecurrentInvitationPlainMessage() {
		return getRecurrentPlainMessage("NEW RECURRENT APPOINTMENT");
	}

	@Override
	protected ArrayList<String> getRecurrentInvitationHtmlMessage() {
		return getRecurrentHtmlMessage("New recurrent appointment");
	}

	@Override
	protected ArrayList<String> getRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RECURRENT APPOINTMENT UPDATED !",
				"from Nov 8, 2010",
				"to \"No end date\"",
				"from          : Nov 8, 2010",
				"to            : Nov 23, 2012",
				"at 12:00 PM - 12:45 PM",
				"time          : 1:00 PM - 2:00 PM",
				"timezone      : Europe/Paris",
				"recurrence    : Every 2 weeks [Monday, Wednesday, Thursday]",
				"subject       : Sprint planning OBM",
				"location      : A random location",
				"organizer     : Raphael ROUGERON"
		);
	}

	@Override
	protected ArrayList<String> getRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Recurrent appointment updated",
				"from Nov 8, 2010",
				"to \"No end date\"",
				"From Nov 8, 2010",
				"To Nov 23, 2012",
				"Timezone Europe/Paris",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"at 12:00 PM - 12:45 PM",
				"Time 1:00 PM - 2:00 PM",
				"Recurrence kind Every 2 weeks [Monday, Wednesday, Thursday]"
		);
	}

	@Override
	protected ArrayList<String> getNonRecurrentToRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"RECURRENT APPOINTMENT UPDATED !",
				"from Nov 8, 2010",
				"to Nov 8, 2010",
				"from          : Nov 8, 2010",
				"to            : Nov 23, 2012",
				"at 12:00 PM - 12:45 PM",
				"time          : 12:00 PM - 12:45 PM",
				"timezone      : Europe/Paris",
				"recurrence kind : Without recurrence",
				"recurrence    : Every 2 weeks [Monday, Wednesday, Thursday]",
				"subject       : Sprint planning OBM",
				"location      : A random location",
				"organizer     : Raphael ROUGERON"
		);
	}

	@Override
	protected ArrayList<String> getNonRecurrentToRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Recurrent appointment updated",
				"from Nov 8, 2010",
				"to Nov 8, 2010",
				"From Nov 8, 2010",
				"To Nov 23, 2012",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"at 12:00 PM - 12:45 PM",
				"Time 12:00 PM - 12:45 PM",
				"Timezone Europe/Paris",
				"recurrence kind : Without recurrence",
				"Recurrence kind Every 2 weeks [Monday, Wednesday, Thursday]"
		);
	}
	
	@Override
	protected ArrayList<String> getRecurrentToNonRecurrentUpdatePlainMessage() {
		return Lists.newArrayList(
				"APPOINTMENT UPDATED !",
				"from Nov 8, 2010 12:00 PM",
				"to Nov 8, 2010 12:45 PM",
				"from          : Nov 8, 2010 12:00 PM",
				"to            : Nov 8, 2010 12:45 PM",
				"timezone      : Europe/Paris",
				"subject       : Sprint planning OBM",
				"location      : A random location",
				"organizer     : Raphael ROUGERON",
				"created by    : Emmanuel SURLEAU"
		);
	}

	@Override
	protected ArrayList<String> getRecurrentToNonRecurrentUpdateHtmlMessage() {
		return Lists.newArrayList(
				"Appointment updated",
				"from Nov 8, 2010 12:00 PM",
				"to Nov 8, 2010 12:45 PM",
				"From Nov 8, 2010 12:00 PM",
				"To Nov 8, 2010 12:45 PM",
				"Timezone Europe/Paris",
				"Subject Sprint planning OBM",
				"Location A random location",
				"Organizer Raphael ROUGERON",
				"Created by Emmanuel SURLEAU"
		);
	}
	
	@Override
	protected ArrayList<String> getRecurrentCancelPlainMessage() {
		return getRecurrentPlainMessage("RECURRENT APPOINTMENT CANCELED");
	}

	@Override
	protected ArrayList<String> getRecurrentCancelHtmlMessage() {
		return getRecurrentHtmlMessage("Recurrent appointment canceled");
	}

	@Override
	protected ArrayList<String> getChangeParticipationPlainMessage() {
		return Lists.newArrayList(
				"ATTENDEE STATE UPDATED",
				"Matthieu BAECHLER has accepted",
				"the event Sprint planning OBM scheduled on Nov 8, 2010 12:00",
				"This is a random comment"
		);
	}
	
	@Override
	protected ArrayList<String> getChangeParticipationHtmlMessage() {
		return Lists.newArrayList(
				"Attendee state updated",
				"Matthieu BAECHLER has accepted",
				"the event Sprint planning OBM scheduled on Nov 8, 2010 12:00",
				"Comment This is a random comment"
		);
	}
	
	@Override
	protected String getNotice() {
		return "If you are using the Thunderbird extension or ActiveSync,"
				+ " you must synchronize to view this";
	}

	@Override
	protected String getNewEventSubject() {
		return "New event from Raphael ROUGERON: Sprint planning OBM";
	}

	@Override
	protected String getNewRecurrentEventSubject() {
		return "New recurrent event from Raphael ROUGERON : Sprint planning OBM";
	}

	@Override
	protected String getCancelEventSubject() {
		return "Event from Raphael ROUGERON cancelled on OBM: Sprint planning OBM";
	}

	@Override
	protected String getCancelRecurrentEventSubject() {
		return "Recurrent event from Raphael ROUGERON cancelled on OBM: Sprint\r\n planning OBM";
	}

	@Override
	protected String getUpdateEventSubject() {
		return "Event from Raphael ROUGERON updated on OBM: Sprint planning OBM";
	}

	@Override
	protected String getUpdateRecurrentEventSubject() {
		return "Recurrent event from Raphael ROUGERON updated on OBM: Sprint\r\n planning OBM";
	}
	
	@Override
	protected String getChangeParticipationSubject() {
		return "Participation updated on OBM: Sprint planning OBM";
	}
}