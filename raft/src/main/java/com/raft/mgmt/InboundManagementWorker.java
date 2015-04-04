package com.raft.mgmt;

public class InboundManagementWorker implements Runnable {
    @Override
    public void run() {
    while(true){
        System.out.println("Executing Inbound ops now");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    }
}
