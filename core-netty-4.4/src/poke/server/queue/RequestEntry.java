package poke.server.queue;

import io.netty.channel.Channel;
import poke.comm.App;
import java.net.InetSocketAddress;

public class RequestEntry {
    private final App.Request req;
    private final Channel channel;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    private final String host;
    private final int port;

    public RequestEntry(App.Request req, Channel channel) {
        this.req = req;
        this.channel = channel;
        this.host = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        this.port = ((InetSocketAddress)channel.remoteAddress()).getPort();
    }


    public App.Request request() {
        return this.req;
    }
}
