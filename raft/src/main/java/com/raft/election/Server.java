package com.raft.election;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Server {
    ServerConf configuration;
    public Server(File conf) {
        byte[] raw = new byte[(int) conf.length()];
        BufferedInputStream br = null;
        try {
            br = new BufferedInputStream(new FileInputStream(conf));
            br.read(raw);
            this.configuration = JsonUtil.decode(new String(raw), ServerConf.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        //File conf = new File(args[0]);
        //final Server server = new Server(conf);
       // server.run();

        final byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
        final ServerConf decode = JsonUtil.decode(new String(bytes), ServerConf.class);
        System.out.println("done"+decode);
    }

    private void run() {

    }
}
