/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync.provider.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;

public class ObmSyncEventProvider extends AbstractObmSyncProvider  {

	protected static final Log logger = LogFactory
	.getLog(ObmSyncEventProvider.class);
	
	private static AbstractObmSyncProvider instance;
	
	public static ICalendarProvider getInstance(){
		if(instance == null){
			instance = new ObmSyncEventProvider();
		}
		return instance;
	}
	
	protected ObmSyncEventProvider() {
		super(); 
	}
	
	@Override
	protected AbstractEventSyncClient getObmSyncClient(String url) {
		return new CalendarClient(url);
	}
	
	@Override
	public EventChanges getSync(AccessToken token, String userId, Date lastSync)
	throws AuthFault, ServerFault {
		logger.info("Get sync["+lastSync+"] from obm-sync");
		return client.getSync(token, userId, lastSync);
	}

	@Override
	public List<Event> getAll(AccessToken token, String calendar)
			throws ServerFault, AuthFault {
		logger.info("Get all Event from obm-sync");
		return super.getAll(token, calendar, EventType.VEVENT);
	}
	
	@Override
	public List<EventTimeUpdate> getAllEventTimeUpdate(AccessToken token,
			String calendar) throws ServerFault, AuthFault {
		logger.info("Get all EventTimeUpdate from obm-sync");
		return super.getAllEventTimeUpdate(token, calendar, EventType.VEVENT);
	}
}
