package poke.server.storage.jdbc;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import poke.server.conf.ClusterConf;
import poke.server.conf.ServerConf;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mrugen on 4/14/15.
 */
public class LogStorageFactory {
    private static  AtomicReference<LogStorage> instance =new AtomicReference<LogStorage>();
    private static ClusterConf clusterConf;
    private static ServerConf serverConf;

    public static void  init(ClusterConf conf, ServerConf serverConf){
        clusterConf = conf;
        LogStorageFactory.serverConf = serverConf;
        instance.compareAndSet(null,new LogStorage(clusterConf,serverConf));
    }
    static public LogStorage getInstance(){
        return instance.get();
    }
}
