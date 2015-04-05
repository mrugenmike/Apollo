package com.raft.mgmt;

import com.raft.election.ServerConf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagementService implements Runnable {
    private final ServerConf serverConfig;
    Logger logger = LoggerFactory.getLogger(ManagementService.class);
    public ManagementService(ServerConf configuration) {
        serverConfig = configuration;
    }

    @Override
    public void run() {
    // bind to mgmt port for listening to AppendEntries from intra cluster messages
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .option(ChannelOption.SO_BACKLOG, 100)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(new ManagementInitializer(true));
            final ChannelFuture bindFuture = serverBootstrap.bind(serverConfig.getMgmtPort());
            logger.info("Binding to port {} for management messages",serverConfig.getMgmtPort());
            bindFuture.syncUninterruptibly();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}
