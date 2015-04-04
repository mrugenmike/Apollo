package com.raft.election;

public class NodeDescription {
    int mgmtPort;
    int nodeId;
    String host;

    public int getMgmtPort() {
        return mgmtPort;
    }

    public void setMgmtPort(int mgmtPort) {
        this.mgmtPort = mgmtPort;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
