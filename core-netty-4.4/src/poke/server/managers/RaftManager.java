package poke.server.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.core.Mgmt;
import poke.server.conf.ServerConf;
import poke.server.election.RaftStateMachine;
import poke.server.election.StateMachine;

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
        // TODO throw exception if not initialized!
        return instance.get();
    }

    private void resetNode(){
    RaftManager.stateMachine.reset();
    this.leaderId = -1;
    this.votedFor = -1;
    this.voteCount.compareAndSet(-1,0);
    this.currentTerm = -1; // should read from storage
    }

   /* private void sendVoteNotice(Mgmt.Management mgmt){


        int destinationId = mgmt.getHeader().getOriginator();
        int term = mgmt.getRaftMessage().getTerm();
        if(this.currentTerm < term){
            this.currentTerm = term;

            Mgmt.Management.Builder mgmtBuilder = Mgmt.Management.newBuilder();
            Mgmt.MgmtHeader.Builder mgmtHeaderBuilder = Mgmt.MgmtHeader.newBuilder();
            mgmtHeaderBuilder.setOriginator(conf.getNodeId()); //setting self as voter
            CompleteRaftMessage.Builder raftMsgBuilder= CompleteRaftMessage.newBuilder();
            raftMsgBuilder.setTerm(currentTerm)
                    .setAction(ElectionAction.VOTE); //setting action so that candidate can use it appropriately.
            Mgmt.Management finalMsg = mgmtBuilder.setHeader(mgmtHeaderBuilder.build()).setRaftMessage(raftMsgBuilder.build()).build();
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
    }*/

    private int getRandomElectionTimeOut(){
        int randomTimeOut = new Random().nextInt(10000 - 5000 + 1) + 5000;
        logger.info("Current Timeout value is {} ",randomTimeOut);
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
                        // TODO Auto-generated catch block
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


        if(raftMessage.hasAction()){
            int electionActionVal = raftMessage.getAction().getNumber();
            switch (electionActionVal) {
                case Mgmt.RaftMsg.ElectionAction.REQUESTVOTE_VALUE:{
                    if(raftMessage.getTerm()>=this.currentTerm){
                        // become follower and cancel any ongoing election and update termId and leaderId
                        stateMachine.becomeFollower();
                        this.currentTerm = raftMessage.getTerm();
                        this.leaderId = mgmt.getHeader().getOriginator();
                        ConnectionManager.sendVote(mgmt,conf.getNodeId());
                        resetElectionTimeout();
                    }else{
                        //reject the message and continue in candidate state
                    }
                    break;
                }
                case Mgmt.RaftMsg.ElectionAction.VOTE_VALUE:{
                    if(voteCount.get()>=ConnectionManager.getNumMgmtConnections()/2){
                        electionTimeout.cancel();
                        stateMachine.becomeLeader();
                        this.leaderId = conf.getNodeId();
                        ConnectionManager.sendLeaderNotice(conf.getNodeId(),currentTerm);

                    }else if(stateMachine.isCandidate() && !timedOut){
                        voteCount.incrementAndGet();
                    } else if(timedOut){
                        resetElectionTimeout();
                        startElection();
                    }
                    break;
                }
                default:
                    break;

            }
        }



    }
}