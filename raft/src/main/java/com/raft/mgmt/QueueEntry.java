package com.raft.mgmt;

import com.raft.message.AppendEntryProto;
import io.netty.channel.Channel;

public class QueueEntry {
    private final Channel channel;
    private final AppendEntryProto.AppendEntry msg;

    public Channel getChannel() {
        return channel;
    }

    public AppendEntryProto.AppendEntry getMsg() {
        return msg;
    }

    public QueueEntry(Channel channel, AppendEntryProto.AppendEntry msg) {
        this.channel = channel;
        this.msg = msg;
    }
}
