package com.raft.mgmt;

import com.raft.election.ServerConf;

public class ManagementService implements Runnable {
    private final ServerConf serverConfig;

    public ManagementService(ServerConf configuration) {
        serverConfig = configuration;
    }

    @Override
    public void run() {
            // Opening the mgmt port
    }
}
