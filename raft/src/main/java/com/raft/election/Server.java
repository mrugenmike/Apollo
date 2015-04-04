package com.raft.election;

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
        System.out.println("done"+conf);
    }

    public void run() {
            // StartManagement Ports for
        System.out.println("Starting management port");
       // ManagementService service
            // Start Data Ports
        System.out.println("Starting Data Ports");

    }
}
