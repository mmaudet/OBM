package org.obm.push.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.obm.annotations.transactional.Propagation;
import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MeetingResponse;
import org.obm.push.bean.MeetingResponseStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.MeetingProtocol;
import org.obm.push.protocol.bean.MeetingHandlerRequest;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.bean.MeetingHandlerResponse.ItemChangeMeetingResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.ServerFault;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the MeetingResponse cmd
 */
@Singleton
public class MeetingResponseHandler extends WbxmlRequestHandler {

	private final MeetingProtocol meetingProtocol;
	
	@Inject
	protected MeetingResponseHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,	StateMachine stMachine, 
			MeetingProtocol meetingProtocol, CollectionDao collectionDao) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao);
		this.meetingProtocol = meetingProtocol;
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <MeetingResponse>
	// <Request>
	// <UserResponse>1</UserResponse>
	// <CollectionId>62</CollectionId>
	// <ReqId>62:379</ReqId>
	// </Request>
	// </MeetingResponse>

	@Override
	protected void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType() + ")");
		MeetingHandlerRequest meetingRequest;
		try {
			
			meetingRequest = meetingProtocol.getRequest(doc);
			MeetingHandlerResponse meetingResponse = doTheJob(meetingRequest, bs);
			for (Document document: meetingProtocol.encodeResponses(meetingResponse)) {
				responder.sendResponse("MeetingResponse", document);
			}
			
		} catch (NoDocumentException e) {
			try {
				responder.sendResponse("MeetingResponse", 
						meetingProtocol.encodeErrorResponse(MeetingResponseStatus.INVALID_MEETING_RREQUEST) );
			} catch (IOException e1) {
				logger.error(e.getMessage(), e);
			}
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ServerFault e) {
			logger.error(e.getMessage(), e);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Transactional(propagation=Propagation.NESTED)
	private MeetingHandlerResponse doTheJob(MeetingHandlerRequest meetingRequest, BackendSession bs) 
			throws DaoException, ServerFault, CollectionNotFoundException {
		
		List<ItemChangeMeetingResponse> meetingResponses =  new ArrayList<ItemChangeMeetingResponse>();
		for (MeetingResponse item : meetingRequest.getMeetingResponses()) {
			
			ItemChange ic = retrieveMailWithMeetingRequest(bs, item);
			ItemChangeMeetingResponse meetingResponse = new ItemChangeMeetingResponse();
			
			if (ic != null && ic.getData() != null) {
				MSEmail invitation = ((MSEmail) ic.getData());
				if (invitation != null) {
					meetingResponse.setStatus(MeetingResponseStatus.SUCCESS);
					if (!AttendeeStatus.DECLINE.equals(item.getUserResponse())) {
						String calId = contentsImporter.importCalendarUserStatus(bs,
								item.getCollectionId(), invitation, item.getUserResponse());	
						meetingResponse.setCalId(calId);	
					}
				} else {
					meetingResponse.setStatus(MeetingResponseStatus.INVALID_MEETING_RREQUEST);
				}
			} else {
				meetingResponse.setStatus(MeetingResponseStatus.SERVER_ERROR);
			}
			
			meetingResponse.setReqId(item.getReqId());	
			meetingResponses.add(meetingResponse);
		}
		return new MeetingHandlerResponse(meetingResponses);
	}
	
	private ItemChange retrieveMailWithMeetingRequest(BackendSession bs, MeetingResponse item)
		throws DaoException, ServerFault, CollectionNotFoundException {
		
		List<ItemChange> lit = contentsExporter.fetch(bs, PIMDataType.EMAIL, Arrays.asList(item.getReqId()));
		if (lit.size() > 0) {
			return lit.get(0);
		} else {
			return null;
		}
	}
	
}
