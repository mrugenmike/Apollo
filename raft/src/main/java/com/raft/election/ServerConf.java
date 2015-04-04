package com.raft.election;

import java.util.List;

public class ServerConf {
    int mgmtPort;
    String host;
    int nodeId;
    List<NodeDescription> adjacentNodes;

    public int getMgmtPort() {
        return mgmtPort;
    }

    public void setMgmtPort(int mgmtPort) {
        this.mgmtPort = mgmtPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public List<NodeDescription> getAdjacentNodes() {
        return adjacentNodes;
    }

    public void setAdjacentNodes(List<NodeDescription> adjacentNodes) {
        this.adjacentNodes = adjacentNodes;
    }
}
