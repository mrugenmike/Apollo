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

    StorageInfo storage;

    public StorageInfo getStorage() {
        return storage;
    }

    public void setStorage(StorageInfo storage) {
        this.storage = storage;
    }

}






