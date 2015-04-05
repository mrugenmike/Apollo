package com.raft.mgmt;

import com.raft.message.AppendEntryProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppendEntryHandler extends SimpleChannelInboundHandler<AppendEntryProto.AppendEntry> {
    private static Logger logger = LoggerFactory.getLogger(AppendEntryHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AppendEntryProto.AppendEntry msg) throws Exception {
        logger.info("Received AppendEntry Message from {} ",msg.getResponse().getSenderId());
        RaftManagementQueue.enQueue(ctx.channel(),msg);
    }
}
