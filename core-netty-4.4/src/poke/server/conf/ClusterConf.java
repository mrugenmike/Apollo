package poke.server.conf;

import java.util.List;

/**
 * Created by mrugen on 4/8/15.
 */
public class ClusterConf {
    int clusterId;

    public ClusterConf(){}

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public int getClusterId() {
        return clusterId;
    }

    public void setClusterId(int clusterId) {
        this.clusterId = clusterId;
    }

    List<Cluster> clusters;

    StorageInfo storageInfo;

    public StorageInfo getStorageInfo() {
        return storageInfo;
    }

    public void setStorageInfo(StorageInfo storageInfo) {
        this.storageInfo = storageInfo;
    }

}


class Cluster {
    public List<ClusterNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ClusterNode> nodes) {
        this.nodes = nodes;
    }

    List<ClusterNode> nodes;
}

class ClusterNode{
    String ip;
    int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

}

