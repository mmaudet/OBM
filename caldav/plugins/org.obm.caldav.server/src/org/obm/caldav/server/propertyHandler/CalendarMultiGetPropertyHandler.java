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
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.propertyHandler;

import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.exception.AppendPropertyException;
import org.obm.sync.calendar.Event;
import org.w3c.dom.Element;

public interface CalendarMultiGetPropertyHandler {
	public void appendCalendarMultiGetPropertyValue(Element prop, IBackend proxy,
			Event event, String eventICS) throws AppendPropertyException;
}
