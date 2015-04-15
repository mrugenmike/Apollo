package poke.server.storage.jdbc;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import poke.server.conf.ClusterConf;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mrugen on 4/14/15.
 */
public class LogStorageFactory {
    private static  AtomicReference<LogStorage> instance =null;
    private static ClusterConf clusterConf;
    public static void  init(ClusterConf conf){
        clusterConf = conf;
    }
    static public LogStorage getInstance(){
        instance.compareAndSet(null,new LogStorage(clusterConf));
        return instance.get();
    }
}