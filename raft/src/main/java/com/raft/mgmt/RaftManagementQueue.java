package com.raft.mgmt;

public class RaftManagementQueue {
    private static InboundManagementWorker inboundWorker;
    private static OutBoundManagementWorker outBoundWorker;
    public static void startup() {
     inboundWorker = new InboundManagementWorker();
     Thread inboundThread = new Thread(inboundWorker);
     inboundThread.start();
     outBoundWorker = new OutBoundManagementWorker();
     Thread outBoundThread = new Thread(outBoundWorker);
     outBoundThread.start();
    }
}
