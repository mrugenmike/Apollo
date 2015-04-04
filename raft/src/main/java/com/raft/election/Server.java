package com.raft.election;

import com.raft.mgmt.ManagementService;
import com.raft.mgmt.RaftManagementQueue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    ServerConf configuration;
    public Server(ServerConf conf) {
     this.configuration = conf;
    }
    public static void main(String[] args) throws IOException {
        final byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
        final ServerConf conf = JsonUtil.decode(new String(bytes), ServerConf.class);
        Server server = new Server(conf);
        server.run();
    }

    public void run() {
     // StartManagement Workers for
     RaftManagementQueue.startup();
     // ManagementService service-Encapsulates
     ManagementService managementService = new ManagementService(configuration);
     final Thread mgmtServiceThread = new Thread(managementService);
     mgmtServiceThread.start();
    }
}
