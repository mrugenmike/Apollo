/*
 * copyright 2012, gash
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
package poke.server.management;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.core.Mgmt.Management;
import poke.server.management.ManagementQueue.ManagementQueueEntry;
import poke.server.managers.ElectionManager;
import poke.server.managers.HeartbeatManager;
import poke.server.managers.JobManager;
import poke.server.managers.NetworkManager;

/**
 * The inbound management worker is the cortex for all work related to the
 * Health and Status (H&S) of the node.
 *
 * Example work includes processing job bidding, elections, network connectivity
 * building. An instance of this worker is blocked on the socket listening for
 * events. If you want to approximate a timer, executes on a consistent interval
 * (e.g., polling, spin-lock), you will have to implement a thread that injects
 * events into this worker's queue.
 *
 * HB requests to this node are NOT processed here. Nodes making a request to
 * receive heartbeats are in essence requesting to establish an edge (comm)
 * between two nodes. On failure, the connecter must initiate a reconnect - to
 * produce the heartbeatMgr.
 *
 * On loss of connection: When a connection is lost, the emitter will not try to
 * establish the connection. The edge associated with the lost node is marked
 * failed and all outbound (enqueued) messages are dropped (TBD as we could
 * delay this action to allow the node to detect and re-establish the
 * connection).
 *
 * Connections are bi-directional (reads and writes) at this time.
 *
 * @author gash
 *
 */
public class InboundMgmtWorker extends Thread {
    protected static Logger logger = LoggerFactory.getLogger("management");

    int workerId;
    boolean forever = true;

    int max=8;
    int min=2;

    Random rand = new Random();

    int  randomNum = rand.nextInt((max - min) + 1) + min;
    boolean isRunning=false;


    Timer timer = new Timer();


    TimerTask task = new TimerTask() {
        int i = randomNum;

        // if(!isRunning){
        public void run() {
            System.out.println(i--);
            isRunning=true;
            if (i< 0)
            {
                i=randomNum;
                timer.cancel();
                isRunning=false;
                return;
            }else if(i==1){
                ElectionManager.getInstance().startElection();
            }
        }
    };

    public InboundMgmtWorker(ThreadGroup tgrp, int workerId) {
        super(tgrp, "inbound-mgmt-" + workerId);
        this.workerId = workerId;

        if (ManagementQueue.outbound == null)
            throw new RuntimeException("connection worker detected null queue");
    }

    @Override
    public void run() {
        while (true) {
            if (!forever && ManagementQueue.inbound.size() == 0)
                break;

            try {
                // block until a message is enqueued
                ManagementQueueEntry msg = ManagementQueue.inbound.take();

                if (logger.isDebugEnabled())
                    logger.debug("Inbound management message received");

                Management mgmt = (Management) msg.req;
                if (mgmt.hasBeat()) {
                    /**
                     * Incoming: this is from a node we requested to create a
                     * connection (edge) to. In other words, we need to track
                     * that this connection is healthy by receiving HB messages.
                     *
                     * Incoming are connections this node establishes, which is
                     * handled by the HeartbeatPusher.
                     */
                    HeartbeatManager.getInstance().processRequest(mgmt);

                    /**
                     * If we have a network (more than one node), check to see
                     * if a election manager has been declared. If not, start an
                     * election.
                     *
                     * The flaw to this approach is from a bootstrap PoV.
                     * Consider a network of one node (myself), an event-based
                     * monitor does not detect the leader is myself. However, I
                     * cannot allow for each node joining the network to cause a
                     * leader election.
                     */

                    if(!ElectionManager.getInstance().isLeaderAlive(mgmt))
                    {
                        System.out.println("Random:"+randomNum);



                        try{
                            timer.scheduleAtFixedRate(task, 0, 1000);
                        }
                        catch(Exception e)
                        {
                            timer.cancel();
                            System.out.println("Continue");
                            final Timer timer = new Timer();
                            timer.scheduleAtFixedRate(new TimerTask() {

                                int i = randomNum;
                                boolean isRunning=false;
                                // if(!isRunning){
                                public void run() {
                                    System.out.println(i--);
                                    isRunning=true;
                                    if (i< 0)
                                    { timer.cancel();
                                        isRunning=false;
                                    }
                                }
                                // }
                            }, 0, 1000);
                        }

                        //ElectionManager.getInstance().assessCurrentState(mgmt); //Not required for RAFT
                    }
                } else if (mgmt.hasElection()) {
                    ElectionManager.getInstance().processRequest(mgmt);
                } else if (mgmt.hasGraph()) {
                    NetworkManager.getInstance().processRequest(mgmt, msg.channel);
                } else
                    logger.error("Unknown management message");

            } catch (InterruptedException ie) {
                break;
            } catch (Exception e) {
                logger.error("Unexpected processing failure, halting worker.", e);
                break;
            }
        }

        if (!forever) {
            logger.info("connection queue closing");
        }
    }
}