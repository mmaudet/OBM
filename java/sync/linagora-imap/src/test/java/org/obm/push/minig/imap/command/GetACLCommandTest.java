package org.obm.push.minig.imap.command;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.exception.InvalidIMAPResponseException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.collect.ImmutableList;



public class GetACLCommandTest {
	
	@Test
	public void testGetACLCommand() {
		assertThat(new GetACLCommand("user/admin@vm.obm.org")
			.getImapCommand()).isEqualTo("GETACL \"user/admin@vm.obm.org\"");
	}
	
	@Test
	public void testGetACLCommandInUTF7() {
		assertThat(new GetACLCommand("user/adminé@vm.obm.org")
			.getImapCommand()).isEqualTo("GETACL \"user/admin&AOk-@vm.obm.org\"");
	}
	
	@Test
	public void testGetACLCommandHandleResponse() {
		String response = 
				"* ACL user/admin@vm.obm.org anyone p admin@vm.obm.org lrswikxtecd";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/admin@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).contains(
				Acl.builder().user("anyone").rights("p").build(),
				Acl.builder().user("admin@vm.obm.org").rights("lrswikxtecd").build());
	}
	
	@Test(expected=InvalidIMAPResponseException.class)
	public void testGetACLCommandHandleResponseWithOddNumberOfArguments() {
		String response = 
				"* ACL user/admin@vm.obm.org anyone p admin@vm.obm.org lrswikxtecd thAnnoyingOne";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/admin@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
	}
	
	@Test
	public void testGetACLCommandHandleResponseWithZeroPair() {
		String response = 
				"* ACL user/admin@vm.obm.org";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/admin@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).isEmpty();
	}
	
	@Test
	public void testGetACLCommandHandleResponseOnFolderWithOneSpace() {
		String response = 
				"* ACL user/projets tiers@vm.obm.org anyone p admin@vm.obm.org lrswikxtecd";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/projets tiers@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).contains(
				Acl.builder().user("anyone").rights("p").build(),
				Acl.builder().user("admin@vm.obm.org").rights("lrswikxtecd").build());
	}
	
	@Test
	public void testGetACLCommandHandleResponseOnFolderWithMultipleSpaces() {
		String response = 
				"* ACL user/projets tiers others@vm.obm.org anyone p admin@vm.obm.org lrswikxtecd";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/projets tiers others@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).contains(
				Acl.builder().user("anyone").rights("p").build(),
				Acl.builder().user("admin@vm.obm.org").rights("lrswikxtecd").build());
	}
	
	@Test
	public void testGetACLCommandHandleResponseOnFolderHierarchyWithMultipleSpaces() {
		String response = 
				"* ACL user/projets tiers others/then other@vm.obm.org anyone p admin@vm.obm.org lrswikxtecd";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("user/projets tiers others/then other@vm.obm.org");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).contains(
				Acl.builder().user("anyone").rights("p").build(),
				Acl.builder().user("admin@vm.obm.org").rights("lrswikxtecd").build());
	}
	
	@Test
	public void testGetACLCommandHandleResponseWithDoubleQuote() {
		String response = 
				"* ACL \"user/projets tiers others/then other@vm.obm.org\" anyone p admin@vm.obm.org lrswikxtecd";
		IMAPResponse imapResponse = new IMAPResponse("OK", response);
		
		GetACLCommand getACLCommand = new GetACLCommand("\"user/projets tiers others/then other@vm.obm.org\"");
		getACLCommand.responseReceived(ImmutableList.of(imapResponse, new IMAPResponse("OK", "")));
		assertThat(getACLCommand.data).contains(
				Acl.builder().user("anyone").rights("p").build(),
				Acl.builder().user("admin@vm.obm.org").rights("lrswikxtecd").build());
	}
}
