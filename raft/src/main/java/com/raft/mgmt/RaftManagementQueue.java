package com.raft.mgmt;

import com.raft.message.AppendEntryProto;
import io.netty.channel.Channel;

import java.util.concurrent.LinkedBlockingDeque;

public class RaftManagementQueue {
    protected static LinkedBlockingDeque<QueueEntry> inbound = new LinkedBlockingDeque<QueueEntry>();
    protected static LinkedBlockingDeque<QueueEntry> outbound = new LinkedBlockingDeque<QueueEntry>();

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

    public static void enQueue(Channel channel, AppendEntryProto.AppendEntry msg) {
        inbound.add(new QueueEntry(channel,msg));

    }
}
