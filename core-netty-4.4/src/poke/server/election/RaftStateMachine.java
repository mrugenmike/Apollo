package poke.server.election;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.core.Mgmt;

enum State{
    Follower,Leader,Candidate;
}

public class RaftStateMachine implements StateMachine {

    static Logger logger = LoggerFactory.getLogger("StateMachine");

    private State machineState;


    public  RaftStateMachine(){
        logger.info("Node became a follower");
        this.machineState = State.Follower;
    }

    @Override
    public void process(Mgmt req) {

    }

    @Override
    public void reset() {
        logger.info("RESET: Node became a follower");
        machineState = State.Follower;

    }

    @Override
    public void becomeFollower() {
        machineState = State.Follower;
    }

    @Override
    public void becomeCandidate() {
        logger.info("Node became a candidate");
        machineState = State.Candidate;
    }

    @Override
    public State getState() {
        return machineState;
    }

    @Override
    public boolean isLeader() {
        return machineState.equals(State.Leader);
    }

    @Override
    public void becomeLeader() {
        logger.info("Node became a Leader");
        machineState = State.Leader;
    }

    @Override
    public boolean isCandidate() {
        return machineState.equals(State.Candidate);
    }

    @Override
    public String toString(){
        return "Node is in following state"+machineState;
    }
}
