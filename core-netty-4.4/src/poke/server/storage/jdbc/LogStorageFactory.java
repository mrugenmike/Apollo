package poke.server.storage.jdbc;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import poke.server.conf.ClusterConf;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mrugen on 4/14/15.
 */
public class LogStorageFactory {
    private static  AtomicReference<LogStorage> instance =new AtomicReference<LogStorage>();
    private static ClusterConf clusterConf;

    public static void  init(ClusterConf conf){
        clusterConf = conf;
        instance.compareAndSet(null,new LogStorage(clusterConf));
    }
    static public LogStorage getInstance(){
        return instance.get();
    }
}
