package com.raft.mgmt;

public class OutBoundManagementWorker implements Runnable {
    @Override
    public void run() {
        while(true){
            System.out.println("Executing Outbound ops now");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
