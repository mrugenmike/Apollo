package poke.server.managers;

import com.google.protobuf.ByteString;

import gash.leaderelection.raft.RaftMessage;

import org.omg.CORBA.COMM_FAILURE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.comm.App;
import poke.comm.App.ClientMessage;
import poke.comm.App.ClusterMessage;
import poke.core.Mgmt;
import poke.server.conf.ServerConf;
import poke.server.election.RaftStateMachine;
import poke.server.election.StateMachine;
import poke.server.storage.aws.UploadFile;
import poke.server.storage.jdbc.LogStorageFactory;

import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RaftManager {
    protected static Logger logger = LoggerFactory.getLogger("RaftManager");
    protected static AtomicReference<RaftManager> instance = new AtomicReference<RaftManager>();

    private static ServerConf conf;
    private int currentTerm=0; //leader’s term

    public int getLeaderId() {
        return leaderId;
    }

    private int leaderId=-1;
    private int votedFor=-1;
    private AtomicInteger voteCount = new AtomicInteger(-1);
    private int candidateId;
    public Timer electionTimeout = new Timer();
    public static StateMachine stateMachine = new RaftStateMachine();
    private boolean timedOut = false;

    public static RaftManager initManager(ServerConf conf) {
        RaftManager.conf = conf;
        instance.compareAndSet(null, new RaftManager());
        return instance.get();
    }

    public static RaftManager getInstance() {
        return instance.get();
    }

    private void resetNode(){
        RaftManager.stateMachine.reset();
        this.leaderId = -1;
        this.votedFor = -1;
        this.voteCount.compareAndSet(-1,0);
        this.currentTerm = -1; // should read from storage
    }


    private void sendAppendNotice(){

        Mgmt.Management.Builder mgmtBuilder = Mgmt.Management.newBuilder();

        Mgmt.MgmtHeader.Builder mgmtHeaderBuilder = Mgmt.MgmtHeader.newBuilder();
        mgmtHeaderBuilder.setOriginator(conf.getNodeId());
        mgmtHeaderBuilder.setSecurityCode(-999);
        mgmtHeaderBuilder.setTime(new Date().getTime());

        Mgmt.RaftMsg.Builder raftMsgBuilder = Mgmt.RaftMsg.newBuilder();

        raftMsgBuilder.setTerm(currentTerm).setAction(Mgmt.RaftMsg.ElectionAction.APPEND);

        Mgmt.Management mgmt = mgmtBuilder.setHeader(mgmtHeaderBuilder.build())
                .setRaftMessage(raftMsgBuilder.build()).build();

        ConnectionManager.broadCastImmediately(mgmt);
    }
    private int getRandomElectionTimeOut(){
        int randomTimeOut = new Random().nextInt(8000) + 8000;
        logger.info("Current Timeout value is {} ", randomTimeOut);
        return randomTimeOut;
    }


    private boolean isLeader() {
        // TODO Auto-generated method stub
        if((voteCount.get()>((ConnectionManager.getNumMgmtConnections())/2)))
            return true;
        else
            return false;
    }


    //Start election
    private void startElection() {
        stateMachine.becomeCandidate();

        currentTerm++;
        voteCount.incrementAndGet();
        //can vote only once in a term.
        if(this.votedFor==-1){
            votedFor=conf.getNodeId();
            //request for votes
            ConnectionManager.sendRequestVote(conf.getNodeId(),currentTerm);
        }
    }

    // Time to celebrate on becoming new Leader
    private void sendLeaderNotice()  {
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                while (true){
                    System.out.println("Sending Append Notices!!");
                    ConnectionManager.sendLeaderNotice(conf.getNodeId(),currentTerm);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void resetElectionTimeout(){
        electionTimeout.cancel();
        electionTimeout = new Timer();
        logger.info("Scheduling timer now");
        electionTimeout.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        logger.info("Node {} timed out initially", conf.getNodeId());
                        startElection();
                        timedOut = true;
                    }
                }

                , getRandomElectionTimeOut());

    }


    public void initRaft(){
        resetNode();
        logger.info("Node {} Starting the Initial Timer...... ", conf.getNodeId());
        resetElectionTimeout();
    }

    public void processRequest(Mgmt.Management mgmt) {

        final Mgmt.RaftMsg raftMessage = mgmt.getRaftMessage();
        logger.info("Processing request now {}", raftMessage);

        if(raftMessage.hasAction()){
            int electionActionVal = raftMessage.getAction().getNumber();
            switch (electionActionVal) {
                case Mgmt.RaftMsg.ElectionAction.REQUESTVOTE_VALUE:{
                    if(raftMessage.getTerm()>=this.currentTerm){
                   // become follower and cancel any ongoing election and update termId and leaderId
                        stateMachine.becomeFollower();
                        this.currentTerm = raftMessage.getTerm();
                        this.leaderId = mgmt.getHeader().getOriginator();
                        ConnectionManager.sendVote(mgmt, conf.getNodeId());
                    }else{
                        //reject the message and continue in candidate state
                    }
                    break;
                }
                case Mgmt.RaftMsg.ElectionAction.VOTE_VALUE:{
                    logger.info("Vote received ");
                    if(voteCount.get()>=ConnectionManager.getNumMgmtConnections()/2){
                        electionTimeout.cancel();
                        stateMachine.becomeLeader();
                        this.leaderId = conf.getNodeId();
                        ConnectionManager.sendLeaderNotice(conf.getNodeId(),currentTerm);
                        sendAppendNotice();
                    }
                    break;
                }
                case Mgmt.RaftMsg.ElectionAction.LEADER_VALUE:{
                    if(raftMessage.getTerm()>=currentTerm){
                        stateMachine.becomeFollower();
                        resetElectionTimeout();
                    }
                    break;
                }
                case Mgmt.RaftMsg.ElectionAction.APPEND_VALUE:{
                    if(leaderId!=conf.getNodeId()){
                        stateMachine.becomeFollower();
                        leaderId = mgmt.getHeader().getOriginator();
                        currentTerm = mgmt.getRaftMessage().getTerm();
                        votedFor =-1;
                        resetElectionTimeout();
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void processRequest(App.Request request)  {
        if(request.hasBody()){
        final App.Payload payload = request.getBody();
        if(leaderId==conf.getNodeId()) {
             if(payload.hasClusterMessage()){
            	 logger.info("***** Payload has cluster message ******");
            	 ClusterMessage clusterMessage2=payload.getClusterMessage();
            	 ClientMessage clientMessage2=clusterMessage2.getClientMessage();
            	 final String msgId = clientMessage2.getMsgId();
                 final String imageName = clientMessage2.getMsgImageName();
                 final ByteString msgImageBits = clientMessage2.getMsgImageBits();
                 final int clusterId = clusterMessage2.getClusterId();
                 final int senderName = clientMessage2.getSenderUserName();
                 final int receiverName = clientMessage2.getReceiverUserName();
                 
               String imageUrl=  UploadFile.uploadImage(msgImageBits, imageName);
                 logger.info("*****Replicating the log now on client message *******");
                 try {
                     LogStorageFactory.getInstance().saveLogEntry(new LogEntry(currentTerm, msgId, imageName, clusterId, senderName,receiverName, imageUrl, -1, "-1"));
                     ConnectionManager.broadcastIntraCluster(request, false);
                 } catch (Exception e) {
                     //logger.error("Failed to save logentry {}",e.getErrorCode());
                 }
             }else{
                 if(payload.hasClientMessage()){
                	 ClientMessage clientMessage2=payload.getClientMessage();
                	 final String msgId = clientMessage2.getMsgId();
                     final String imageName = clientMessage2.getMsgImageName();
                     final ByteString msgImageBits = clientMessage2.getMsgImageBits();
                    // final int clusterId = clusterMessage2.getClusterId();
                     final int senderName = clientMessage2.getSenderUserName();
                     final int receiverName = clientMessage2.getReceiverUserName();
                     String imageUrl=  UploadFile.uploadImage(msgImageBits, imageName);

                     //log replication for clientMessage
                	 try {
                         LogStorageFactory.getInstance().saveLogEntry(new LogEntry(currentTerm, msgId, imageName, -1, senderName,receiverName, imageUrl, -1, "-1"));
                         ConnectionManager.broadcastIntraCluster(request, true);
                     } catch (SQLException e) {
                         logger.error("Failed to save logentry {}",e.getErrorCode());
                     }

                       }
         } 
         }else{
            logger.info("Hello client I'm a follower");
            if(request.getBody().getClusterMessage().getClientMessage().getBroadcastInternal()){
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
                logger.info("***********I am a follower Node-----> Need to replicate logs*********************** ");
            }
         }
        }

    }
}