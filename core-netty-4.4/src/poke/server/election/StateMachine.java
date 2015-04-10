package poke.server.election;

import poke.core.Mgmt;

/**
 * Created by mrugen on 4/9/15.
 */
public interface StateMachine {
    public void process(Mgmt req);

    void reset();

    void becomeFollower();

    void becomeCandidate();

    State getState();

    boolean isLeader();

    void becomeLeader();

    boolean isCandidate();
}
