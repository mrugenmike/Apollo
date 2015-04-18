package poke.server.conf;

import java.util.concurrent.atomic.AtomicReference;

public class ServerConfFactory {
    static AtomicReference<ServerConf> confAtomicReference = new AtomicReference<ServerConf>();
    public static void setConfInstance(ServerConf confInstance){
        confAtomicReference.compareAndSet(null,confInstance);
    }

    public static ServerConf instance(){
        return confAtomicReference.get();
    }
}
