package poke.server.conf;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mrugen on 4/8/15.
 */
public class ClusterConfFactory {

    private static final AtomicReference<ClusterConf> instance = new AtomicReference<ClusterConf>();
    public static void setInstance(ClusterConf clusterConf) {
        instance.compareAndSet(null,clusterConf);
    }
    public static ClusterConf getInstance(){
        return instance.get();
    }
}
