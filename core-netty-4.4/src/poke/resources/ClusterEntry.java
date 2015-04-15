package poke.resources;

import poke.server.queue.RequestEntry;

/**
 * Created by mrugen on 4/14/15.
 */
public class ClusterEntry {
    private final RequestEntry entry;

    public ClusterEntry(RequestEntry entry) {
        this.entry = entry;
    }

    public RequestEntry getEntry() {
        return entry;
    }

    public String getHost(){
        return entry.getHost();
    }
    public String getPort(){
        return String.valueOf(entry.getPort());
    }

    public static class Schema{
        public static String clusterId = "cluster_id";
        public static String node_id = "node_id";
        public static String node_ip = "node_ip";
        public static String node_port = "node_port";
    }
}
