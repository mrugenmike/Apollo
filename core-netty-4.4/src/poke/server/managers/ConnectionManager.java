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
package poke.server.managers;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.client.comm.ClientInitializer;
import poke.client.comm.CommConnection;
import poke.client.comm.CommHandler;
import poke.comm.App;
import poke.comm.App.ClientMessage;
import poke.comm.App.ClusterMessage;
import poke.comm.App.Header;
import poke.comm.App.Payload;
import poke.comm.App.Ping;
import poke.comm.App.Request;
import poke.comm.App.Request.Builder;
import poke.core.Mgmt;
import poke.core.Mgmt.Management;
import poke.core.Mgmt.MgmtHeader;
import poke.core.Mgmt.RaftMsg;
import poke.core.Mgmt.RequestVoteMessage;
import poke.server.conf.ClusterConf;
import poke.server.conf.NodeDesc;
import poke.server.conf.ServerConf;

/**
 * the connection map for server-to-server communication.
 * 
 * Note the connections/channels are initialized through the heartbeat manager
 * as it starts (and maintains) the connections through monitoring of processes.
 * 
 * 
 * TODO refactor to make this the consistent form of communication for the rest
 * of the code
 * 
 * @author gash
 * 
 */
public class ConnectionManager {
	protected static Logger logger = LoggerFactory.getLogger("ConnectionManager");

	/** node ID to channel */
	private static HashMap<Integer, Channel> connections = new HashMap<Integer, Channel>();
	private static HashMap<Integer, Channel> mgmtConnections = new HashMap<Integer, Channel>();

	private static ServerConf conf;
	private static ClusterConf clusterConf;

	public static void init(ServerConf conf,ClusterConf clusterConf){
		ConnectionManager.conf = conf;
		ConnectionManager.clusterConf = clusterConf;
	}

	public static void addDataConnection(Integer nodeId){
		for(NodeDesc node:conf.getAdjacent().getAdjacentNodes().values()){
			if(node.getNodeId()==nodeId){
				connections.put(nodeId,getConnection(node));
				break;
			}
		}
	}

	private static Channel getConnection(NodeDesc desc) {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			CommHandler handler = new CommHandler();
			final ClientInitializer initializer = new ClientInitializer(handler,false);
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(initializer);
			b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
			b.option(ChannelOption.TCP_NODELAY, true);
			b.option(ChannelOption.SO_KEEPALIVE, true);

			// Make the connection attempt.
			final ChannelFuture channelFuture = b.connect(desc.getHost(), desc.getPort()).awaitUninterruptibly();

			return channelFuture.channel();

		} catch (Exception ex) {
			logger.error("failed to initialize the client connection", ex);
		}
		return null;
	}


	public static void addConnection(Integer nodeId, Channel channel, boolean isMgmt) {
		logger.info("ConnectionManager adding connection to " + nodeId);

		if (isMgmt)
			mgmtConnections.put(nodeId, channel);
		else
			connections.put(nodeId, channel);
	}

	public static Channel getConnection(Integer nodeId, boolean isMgmt) {
	logger.info("fetching channel for node {}",nodeId);
		if (isMgmt)
			return mgmtConnections.get(nodeId);
		else
			return connections.get(nodeId);
	}

	public synchronized static void removeConnection(Integer nodeId, boolean isMgmt) {
		if (isMgmt)
			mgmtConnections.remove(nodeId);
		else
			connections.remove(nodeId);
	}

	public synchronized static void removeConnection(Channel channel, boolean isMgmt) {

		if (isMgmt) {
			if (!mgmtConnections.containsValue(channel)) {
				return;
			}

			for (Integer nid : mgmtConnections.keySet()) {
				if (channel == mgmtConnections.get(nid)) {
					mgmtConnections.remove(nid);
					break;
				}
			}
		} else {
			if (!connections.containsValue(channel)) {
				return;
			}

			for (Integer nid : connections.keySet()) {
				if (channel == connections.get(nid)) {
					connections.remove(nid);
					break;
				}
			}
		}
	}

	public synchronized static void broadcast(Request req) {
		if (req == null)
			return;

		for (Channel ch : connections.values())
			ch.write(req);
	}

	public synchronized static void broadcast(Management mgmt) {
		if (mgmt == null)
			return;

		for (Channel ch : mgmtConnections.values())
			ch.write(mgmt);
	}

	public synchronized  static  void broadCastImmediately(Management mgmt){
		if (mgmt == null)
			return;

		for (Channel ch : mgmtConnections.values())
			ch.writeAndFlush(mgmt);
	}

	public synchronized static void broadcast(Management mgmt,int toNode) {
		if (mgmt == null)
			return;

		if(mgmtConnections.get(toNode)!=null) {
			Channel ch = mgmtConnections.get(toNode);
			ch.write(mgmt);
		}
	}
	public static int getNumMgmtConnections() {
		return mgmtConnections.size();
	}

	public synchronized static void sendVote(Management mgmt,int originatorId) {
		final int destinationId = mgmt.getHeader().getOriginator();
		logger.info("Sending Vote--> to Node {} from Node {}", originatorId, destinationId);
		int term = mgmt.getRaftMessage().getTerm();
		Management.Builder mgmtBuilder = Management.newBuilder();
		MgmtHeader.Builder mgmtHeaderBuilder = MgmtHeader.newBuilder();
		mgmtHeaderBuilder.setOriginator(originatorId); //setting self as voter
		mgmtHeaderBuilder.setSecurityCode(-999);
		mgmtHeaderBuilder.setTime(new Date().getTime());

		final RaftMsg.Builder raftMessageBuilder = mgmtBuilder.getRaftMessageBuilder();
		raftMessageBuilder.setTerm(mgmt.getRaftMessage().getTerm()).setAction(RaftMsg.ElectionAction.VOTE); //setting action so that candidate can use it appropriately.
		Management finalMsg = mgmtBuilder.setHeader(mgmtHeaderBuilder.build()).setRaftMessage(raftMessageBuilder.build()).build();
		Channel candidateChannel = ConnectionManager.getConnection(destinationId, true);
		logger.info("Sending Vote to Node {}", destinationId);
		candidateChannel.writeAndFlush(finalMsg);
	}

	//Prepare Raft Message for Voting
	public synchronized static void sendRequestVote(int candidateId,int termId) {

		RequestVoteMessage.Builder reqVoteBuilder = RequestVoteMessage.newBuilder();
		reqVoteBuilder.setCandidateId(candidateId);
		Management.Builder mgmtBuilder = Management.newBuilder();

		RaftMsg.Builder raftMsgbuilder = mgmtBuilder.getRaftMessageBuilder();
		raftMsgbuilder.setAction(RaftMsg.ElectionAction.REQUESTVOTE).setTerm(termId).setRequestVote(reqVoteBuilder.build());


		MgmtHeader header = mgmtBuilder.getHeader();
		MgmtHeader.Builder mgmtHeaderBuilder = MgmtHeader.newBuilder();
		mgmtHeaderBuilder.setOriginator(candidateId);
		mgmtHeaderBuilder.setSecurityCode(-999);
		mgmtHeaderBuilder.setTime(new Date().getTime());

		mgmtBuilder.setHeader(mgmtHeaderBuilder.build());
		mgmtBuilder.setRaftMessage(raftMsgbuilder.build());
		logger.info("Node {} became candidate and sending requests!",candidateId);
		ConnectionManager.broadCastImmediately(mgmtBuilder.build());
	}
	
	public synchronized static void sendLeaderNotice(final int originator, final int termId){
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true){
						logger.info("Sending AppendNotices from node {} in term {}",originator,termId);
						Mgmt.Management.Builder mgmtBuilder = Mgmt.Management.newBuilder();
						Mgmt.MgmtHeader.Builder mgmtHeaderBuilder = Mgmt.MgmtHeader.newBuilder();
						mgmtHeaderBuilder.setOriginator(originator);
						mgmtHeaderBuilder.setSecurityCode(-999);
						mgmtHeaderBuilder.setTime(new Date().getTime());
						RaftMsg.Builder raftMsgBuilder = mgmtBuilder.getRaftMessageBuilder();
						raftMsgBuilder.setTerm(termId).setAction(RaftMsg.ElectionAction.APPEND);
						Mgmt.Management mgmt = mgmtBuilder.setHeader(mgmtHeaderBuilder.build())
								.setRaftMessage(raftMsgBuilder.build()).build();
						ConnectionManager.broadCastImmediately(mgmt);
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
	}

	public static void broadcastLogEntry() {
		
		
	}

	public static void broadcastIntraCluster(Request req, boolean clientMsg) {
		logger.info("Broadcasting the message inside the cluster now {}",connections.values().size());
		ClientMessage clientMessage;
		boolean isClient = clientMsg;
		if (req == null)
			return;

		if(clientMsg){
			clientMessage = req.getBody().getClientMessage();
		}
		else{
			clientMessage = req.getBody().getClusterMessage().getClientMessage();
		}

		Builder requestBuilder = App.Request.newBuilder();
		requestBuilder.setHeader(Header.getDefaultInstance());

		 Ping.Builder pingBuilder = Ping.newBuilder();
		 pingBuilder.setNumber(-1);
		 pingBuilder.setTag("IntraCluster-Broadcast");

			// payload containing data
			
			Payload.Builder payLoadBuilder = Payload.newBuilder();

			final ClusterMessage.Builder clusterMessageBuilder = payLoadBuilder.getClusterMessageBuilder();
			ClientMessage.Builder clientMsgBuilder = clusterMessageBuilder.getClientMessageBuilder();
			clientMsgBuilder.setMsgId(clientMessage.getMsgId());
			clientMsgBuilder.setSenderUserName(clientMessage.getSenderUserName());
			clientMsgBuilder.setReceiverUserName(clientMessage.getReceiverUserName());
			clientMsgBuilder.setMsgImageName(clientMessage.getMsgImageName());
			clientMsgBuilder.setMsgImageBits(clientMessage.getMsgImageBits());
			clientMsgBuilder.setIsClient(clientMsg);
			clientMsgBuilder.setBroadcastInternal(true);

			clusterMessageBuilder.setClientMessage(clientMsgBuilder.build());
			final ClusterMessage clusterMessage = clusterMessageBuilder.build();
			payLoadBuilder.setClusterMessage(clusterMessage);
			
			payLoadBuilder.setPing(pingBuilder.build());
			requestBuilder.setBody(payLoadBuilder.build());
			Request request = requestBuilder.build();
			logger.info("Broadcasting the request intra cluster for log replication");
			
			broadCastImmediately(request);
	}

	private static void broadCastImmediately(Request request) {
		if (request == null)
			return;

		logger.info("Found {} connections for intra-cluster transfer",connections.values().size());
		for (Channel ch : connections.values()){
			String host = ((InetSocketAddress) ch.remoteAddress()).getAddress().getHostAddress();
			int  port = ((InetSocketAddress)ch.remoteAddress()).getPort();
			logger.info("sending request to : {} port {}",host,port);
			ch.writeAndFlush(request);
		}

	}
}
