<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Recurrent appointment canceled !
        </th>
    </tr>
    <tr>
        <td colspan="2">The following appointment has been canceled :</td>
    </tr>
    <tr>
        <td style="text-align:right;width:20%;padding-right:1em;">Subject</td><td style="font-weight:bold;">${subject}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">From</td><td style="font-weight:bold;">${start?date}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">To</td><td style="font-weight:bold;">${recurrenceEnd}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Time</td><td style="font-weight:bold;">${startTime?string.short} - ${endTime?string.short}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Timezone</td><td style="font-weight:bold;">${timezone}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Recurrence kind</td><td style="font-weight:bold;">${recurrenceKind}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Location</td><td style="font-weight:bold;">${location}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organizer</td><td style="font-weight:bold;">${organizer}</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Created by</td><td style="font-weight:bold;">${creator}</td>
    </tr>
    <tr valign="top">
        <td style="text-align:right;padding-right:1em;">Attendee(s)</td><td style="font-weight:bold;">${attendees}</td>
    </tr>
</table>
