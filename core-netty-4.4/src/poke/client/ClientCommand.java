/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.client;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import poke.client.comm.CommConnection;
import poke.client.comm.CommListener;
import poke.comm.App.ClientMessage;
import poke.comm.App.ClusterMessage;
import poke.comm.App.Header;
import poke.comm.App.JoinMessage;
import poke.comm.App.Payload;
import poke.comm.App.Ping;
import poke.comm.App.Request;

/**
 * The command class is the concrete implementation of the functionality of our
 * network. One can view this as a interface or facade that has a one-to-one
 * implementation of the application to the underlining communication.
 * 
 * IN OTHER WORDS (pay attention): One method per functional behavior!
 * 
 * @author gash
 * 
 */
public class ClientCommand {
	protected static Logger logger = LoggerFactory.getLogger("client");

	private String host;
	private int port;
	private CommConnection comm;

	public ClientCommand(String host, int port) {
		this.host = host;
		this.port = port;

		init();
	}

	private void init() {
		comm = new CommConnection(host, port);
	}

	/**
	 * add an application-level listener to receive messages from the server (as
	 * in replies to requests).
	 * 
	 * @param listener
	 */
	public void addListener(CommListener listener) {
		comm.addListener(listener);
	}

	/**
	 * Our network's equivalent to ping
	 * 
	 * @param tag
	 * @param num
	 */
	public void poke(String tag, int num) {
		// data to send
		Ping.Builder f = Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
		
		p.setPing(f.build());
		r.setBody(p.build());

		// header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.PING);
		r.setHeader(h.build());

		Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}
	
// Only Join message	
	public void join(String tag, int num) {
		// data to send
		Ping.Builder f = Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
/******* Only Join Message****/
		JoinMessage.Builder j=JoinMessage.newBuilder(); // Join Message Builder
	
		j.setFromNodeId(500);
		j.setFromClusterId(300);
		j.setToClusterId(600);
		j.setToNodeId(400);
		r.setJoinMessage(j);
		
/*****/		
		p.setPing(f.build());
		r.setBody(p.build());

		// header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.PING);
		

		h.setRoutingId(Header.Routing.JOBS);
		
		
		r.setHeader(h.build());
		

		Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}

	public void payLoadCluster(String tag, int num, byte[] bytes, String imageName) throws UnsupportedEncodingException {

		
		// data to send
		Ping.Builder f = Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
		
		p.setPing(f.build());
		r.setBody(p.build());
		
/*** PayLoad with Cluster Message***/	
		
		
		p.getClusterMessageBuilder().getClientMessage();
		p.getClusterMessageBuilder().getClusterId();
		ClusterMessage.Builder cmgBuilder = ClusterMessage.newBuilder();
		p.getClusterMessageBuilder().setClusterId(300);
		ClientMessage.Builder clientMsgBuilder = p.getClusterMessageBuilder().getClientMessageBuilder();
		clientMsgBuilder.setMsgId("455");
		
		clientMsgBuilder.setMsgImageName(imageName);
		clientMsgBuilder.setMsgImageBits(ByteString.copyFrom(bytes));
		clientMsgBuilder.setMsgIdBytes(ByteString.copyFrom("hello world","UTF-8"));
		cmgBuilder.setClientMessage(clientMsgBuilder.build());
		p.setClusterMessage(cmgBuilder.build());
	
	  //p.getClusterMessageBuilder().setClientMessage("sss");
		
		r.setBody(p.build());
/****/
		// header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.JOBS);
		r.setHeader(h.build());

		Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}	


	public void payLoadClient(String tag, int num, byte[] bytes, String imageName) {
		// data to send
		Ping.Builder f = Ping.newBuilder();
		f.setTag(tag);
		f.setNumber(num);

		// payload containing data
		Request.Builder r = Request.newBuilder();
		Payload.Builder p = Payload.newBuilder();
		
		p.setPing(f.build());
		r.setBody(p.build());
		
/*** PayLoad with Client Message***/	
		ClientMessage.Builder cmgBuilder = p.getClusterMessageBuilder().getClientMessageBuilder();
		
		cmgBuilder.setMsgId("Client Message");
		cmgBuilder.setMsgImageName(imageName);
		cmgBuilder.setMsgText("Great Image");
		cmgBuilder.setReceiverUserName(121);
		cmgBuilder.setSenderUserName(111);
		cmgBuilder.setMsgIdBytes(ByteString.copyFrom(bytes));
		
		
		/*p.getClientMessageBuilder().getMsgId();
		p.getClientMessageBuilder().getMessageType();
		p.getClientMessageBuilder().getSenderUserName();
		p.getClientMessageBuilder().getReceiverUserName();
		p.getClientMessageBuilder().getMsgIdBytes();
		p.getClientMessageBuilder().getMsgImageBits();
		p.getClientMessageBuilder().getMsgImageNameBytes();
		p.getClientMessageBuilder().getMsgTextBytes();*/
		
		//p.getClientMessageBuilder().setMsgId(599);
		
		
		r.setBody(p);
/****/
		// header with routing info
		Header.Builder h = Header.newBuilder();
		h.setOriginator(1000);
		h.setTag("test finger");
		h.setTime(System.currentTimeMillis());
		h.setRoutingId(Header.Routing.PING);
		r.setHeader(h.build());

		Request req = r.build();

		try {
			comm.sendMessage(req);
		} catch (Exception e) {
			logger.warn("Unable to deliver message, queuing");
		}
	}	
	
	
}
