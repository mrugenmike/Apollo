/*
 * copyright 2015, gaurav bajaj
 * 
 * Gaurav Bajaj licenses this file to you under the Apache License,
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
 *//*

package poke.server.managers;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.core.Mgmt;
import poke.core.Mgmt.CompleteRaftMessage;
import poke.core.Mgmt.CompleteRaftMessage.ElectionAction;
import poke.core.Mgmt.Management;
import poke.core.Mgmt.MgmtHeader;
import poke.core.Mgmt.RequestVoteMessage;
import poke.server.conf.ServerConf;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class CompleteRaftManager {
	protected static Logger logger = LoggerFactory.getLogger("election");
	protected static AtomicReference<CompleteRaftManager> instance = new AtomicReference<CompleteRaftManager>();

	private static ServerConf conf;
	private int currentTerm=0; //leader’s term
	private int leaderId=-1;
	private int votedFor=-1;
	private int voteCount=0;
	private int candidateId;
	private State state= State.FOLLOWER; //state of the node: Follower, Candidate, Leader
	private Timer electionTimeout = new Timer();

	public  enum State {
		FOLLOWER, CANDIDATE, LEADER
	}

	public static CompleteRaftManager initManager(ServerConf conf) {
		CompleteRaftManager.conf = conf;
		instance.compareAndSet(null, new CompleteRaftManager());
		return instance.get();
	}

	public static CompleteRaftManager getInstance() {
		// TODO throw exception if not initialized!
		return instance.get();
	}

	private void resetNode(){

		System.out.println("Resetting Node");
		this.state=State.FOLLOWER;
		this.leaderId=-1;
		this.votedFor=-1;
		this.voteCount=0;
	}

	private void sendVoteNotice(Management mgmt){


		int destinationId = mgmt.getHeader().getOriginator();
		int term = mgmt.getRaftMessage().getTerm();
		if(this.currentTerm < term){
			this.currentTerm = term;

			Management.Builder mgmtBuilder = Management.newBuilder();
			MgmtHeader.Builder mgmtHeaderBuilder = MgmtHeader.newBuilder();
			mgmtHeaderBuilder.setOriginator(conf.getNodeId()); //setting self as voter
			CompleteRaftMessage.Builder raftMsgBuilder= CompleteRaftMessage.newBuilder();
			raftMsgBuilder.setTerm(currentTerm)
			.setAction(ElectionAction.VOTE); //setting action so that candidate can use it appropriately.
			Management finalMsg = mgmtBuilder.setHeader(mgmtHeaderBuilder.build()).setRaftMessage(raftMsgBuilder.build()).build();
			Channel candidateChannel = ConnectionManager.getConnection(destinationId, true);	
			System.out.println("Sending to NodeId --> "+destinationId);

			candidateChannel.writeAndFlush(finalMsg);

			System.out.println("Node "+conf.getNodeId()+" voted node "+destinationId);
			electionTimeout.cancel();
			electionTimeout = new Timer();
			electionTimeout.schedule (new TimerTask() {

				@Override
				public void run() {
					System.out.println("Append not called by leader. Re-election!!!");
					resetNode();
					startElection();

				}
			}, getRandomElectionTimeOut());

		}

		//if(this.currentTerm > term) implement this scenario to make our raft 
		//partition tolerant
	}

	private int getRandomElectionTimeOut(){
		int randomTimeOut = new Random().nextInt(10000 - 5000 + 1) + 5000;
		System.out.println("New TTL --> "+randomTimeOut);
		return randomTimeOut;
	}

	//Prepare Raft Message for Voting
	private void sendRequestVote() {

		RequestVoteMessage.Builder reqVoteBuilder = RequestVoteMessage.newBuilder();
		reqVoteBuilder.setCandidateId(conf.getNodeId());

		CompleteRaftMessage.Builder raftMsgbuilder = CompleteRaftMessage.newBuilder();
		raftMsgbuilder.setAction(ElectionAction.REQUESTVOTE).setTerm(currentTerm).setRequestVote(reqVoteBuilder.build());

		Management.Builder mgmtBuilder = Management.newBuilder();

		MgmtHeader header = mgmtBuilder.getHeader();
		MgmtHeader.Builder mgmtHeaderBuilder = MgmtHeader.newBuilder();
		mgmtHeaderBuilder.setOriginator(conf.getNodeId());

		mgmtBuilder.setHeader(mgmtHeaderBuilder.build());
		mgmtBuilder.setRaftMessage(raftMsgbuilder.build());
		ConnectionManager.broadcastAndFlush(mgmtBuilder.build());


		System.out.println("Node "+conf.getNodeId()+" became candidate and sending requests!");
	}

	private boolean isLeader() {
		// TODO Auto-generated method stub
		if((voteCount>((ConnectionManager.getNumMgmtConnections())/2)))
			return true;
		else
			return false;
	}


	//Start election
	private void startElection() {
		state=State.CANDIDATE;

		//candidate will vote for itself and then request for votes
		currentTerm++;
		voteCount++;
		//can vote only once in a term.
		if(this.votedFor==-1){
			votedFor=conf.getNodeId();
			//request for votes
			sendRequestVote();
		}

	}


	private void sendAppendNotice(){

		Management.Builder mgmtBuilder = Management.newBuilder();

		MgmtHeader.Builder mgmtHeaderBuilder = MgmtHeader.newBuilder();
		mgmtHeaderBuilder.setOriginator(conf.getNodeId());

		CompleteRaftMessage.Builder raftMsgBuilder = CompleteRaftMessage.newBuilder();
		//	raftMsgBuilder.setAction(ElectionAction.LEADER);

		raftMsgBuilder.setTerm(currentTerm).setAction(ElectionAction.APPEND);

		Management mgmt = mgmtBuilder.setHeader(mgmtHeaderBuilder.build())
				.setRaftMessage(raftMsgBuilder.build()).build();

		ConnectionManager.broadcastAndFlush(mgmt);
	}


	// Time to celebrate on becoming new Leader
	private void sendLeaderNotice()  {
		//sendAppendNotice();


		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				while (true){
					System.out.println("Sending Append Notices!!");
					sendAppendNotice();
                   try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			}
		});

		t.start();

	}


	public void startMyRaft(){
		resetNode();
		electionTimeout.cancel();
		electionTimeout = new Timer();
		electionTimeout.schedule(
				new TimerTask() {

					@Override
					public void run() {
						System.out.println("Starting raft with election");
						startElection();

					}
				}

				, getRandomElectionTimeOut());

	}

	public void processRequest(Management mgmt) {

		//	if (!mgmt.hasRaftMessage())
		//	return;

		Mgmt.RaftMsg req = mgmt.getRaftMessage();

		//When another node sends a CompleteRaftMessage, the manager will check for 
		//its term

		*/
/*	if(req.hasTerm()){
			if(req.getTerm() > this.currentTerm)
				this.currentTerm = req.getTerm();
		}
		 *//*


		int electionActionVal = req.getAction().get;
		switch (electionActionVal) {

		case ElectionValue.APPEND_VALUE:
			*/
/*if(this.term < req.getTerm()){
				this.term = req.getTerm();
				this.leaderId = mgmt.getHeader().getOriginator();

			}*//*


			System.out.println("Leader ID -->"+leaderId);
			if(leaderId!=conf.getNodeId()){
				state=State.FOLLOWER;
				leaderId=mgmt.getHeader().getOriginator();
				currentTerm=mgmt.getRaftMessage().getTerm();
				votedFor=-1;
				//reset timer else call for election
				electionTimeout.cancel();
				electionTimeout=new Timer();
				electionTimeout.schedule (new TimerTask() {

					@Override
					public void run() {
						System.out.println("Append not called by leader. Re-election!!! ");
						resetNode();
						startElection();

					}
				}, getRandomElectionTimeOut());
			}
			break;

		case ElectionAction.REQUESTVOTE_VALUE:

			//send vote to the originator
			//System.out.println("Voted For before: "+votedFor+" term: "+msg.getTerm());
			if(this.votedFor == -1){
				votedFor=mgmt.getHeader().getOriginator();
				sendVoteNotice(mgmt);
				//System.out.println("Voted for: "+votedFor);
			}
			break;
		case ElectionAction.VOTE_VALUE:
			voteCount++;
			votedFor=-1;
			System.out.println("Leader --> "+isLeader());
			if(isLeader()){
				//currentTerm=mgmt.getRaftMessage().getTerm();
				state=State.LEADER;
				leaderId=conf.getNodeId();	
				sendLeaderNotice();
				votedFor=-1;
				voteCount=0;
				System.out.println("Node "+leaderId+" is the leader!");	
				//leaderId=-1;
				electionTimeout.cancel();
				electionTimeout=new Timer();
			}
			break;

		default:
			break;

		}

	}


}
*/
