package poke.cluster;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.comm.App;
import poke.server.ServerInitializer;
import poke.server.conf.*;
import poke.server.managers.ConnectionManager;
import poke.server.managers.RaftManager;

import java.util.*;

public  class ClusterConnectionManager extends Thread {
	static Logger logger = LoggerFactory.getLogger("ClusterConnectionManager");
	private final ServerConf serverConf = ServerConfFactory.instance();
	public Map<Integer, Channel> interClustersChannels = new HashMap<Integer, Channel>();//nodeid and channel
	private ClusterConf clusterConf;

	public ClusterConnectionManager() {
			clusterConf = ClusterConfFactory.getInstance();
	}

		public ChannelFuture connect(String host, int port) {

			ChannelFuture channel = null;
			EventLoopGroup workerGroup = new NioEventLoopGroup();

			try {
				logger.info("Attempting to  connect to : "+host+" : "+port);
				Bootstrap b = new Bootstrap();
				b.group(workerGroup).channel(NioSocketChannel.class)
						.handler(new ServerInitializer(false));

				b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
				b.option(ChannelOption.TCP_NODELAY, true);
				b.option(ChannelOption.SO_KEEPALIVE, true);

				channel = b.connect(host, port).syncUninterruptibly();
			} catch (Exception e) {
				return null;
			}

			return channel;
		}

		public App.Request createClusterJoinMessage(int fromCluster, int fromNode, int toCluster, int toNode) {
			App.Request.Builder req = App.Request.newBuilder();

			App.JoinMessage.Builder jm = App.JoinMessage.newBuilder();
			jm.setFromClusterId(fromCluster);
			jm.setFromNodeId(fromNode);
			jm.setToClusterId(toCluster);
			jm.setToNodeId(toNode);

			req.setJoinMessage(jm.build());
			return req.build();
		}

		@Override
		public void run() {
			while (true) {
				List<Cluster> clusters = clusterConf.getClusters();
				if(RaftManager.stateMachine.isLeader()){
					try {
						for (Cluster c : clusters) {
							if (!interClustersChannels.containsKey(c.getId())) {
								for(ClusterNode node:c.getNodes()){
									ChannelFuture future = connect(node.getIp(), node.getPort());
									future.addListener(new ClusterLostListener(this));
									App.Request req = createClusterJoinMessage(clusterConf.getClusterId(), serverConf.getNodeId(), c.getId(), node.getId());
									if (future != null) {
										future = future.channel().writeAndFlush(req);
										if (future.channel().isWritable()) {
											interClustersChannels.put(c.getId(),future.channel());
											ConnectionManager.addClusterConnection(c.getId(),future.channel());
											logger.info("Connection to cluster {} and node {}",c.getId(),node.getId());
											break;
										}
									}
								}
								}
							}
					} catch (NoSuchElementException e) {
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

class ClusterLostListener implements ChannelFutureListener {
		static Logger logger = LoggerFactory.getLogger("ClusterLostListener");
		ClusterConnectionManager ccm;

		public ClusterLostListener(ClusterConnectionManager ccm) {
			this.ccm = ccm;
		}

		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			logger.info("Cluster " + future.channel() + " closed. Removing connection");
		}
	}
