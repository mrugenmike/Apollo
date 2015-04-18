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

    public static void  init(ClusterConf conf, ServerConf serverConf){
        if(conf==null||serverConf==null){
            System.out.print("\n cluster or server conf is null server:"+serverConf+" \n cluster: "+conf);
        }else{
            instance.compareAndSet(null,new LogStorage(conf,serverConf));
        }
    }
    static public LogStorage getInstance(){
        return instance.get();
    }
}
