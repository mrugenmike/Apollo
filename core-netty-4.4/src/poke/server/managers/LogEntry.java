package poke.server.managers;

public class LogEntry {
    public int getCurrentTerm() {
        return currentTerm;
    }

    public String getMsgId() {
        return msgId;
    }

    public String getImageName() {
        return imageName;
    }

    public int getClusterId() {
        return clusterId;
    }

    public int getSenderName() {
        return senderName;
    }

    public int getReceiverName() {
        return receiverName;
    }

    private final int currentTerm;
    private final String msgId;
    private final String imageName;
    private final int clusterId;
    private final int senderName;
    private final int receiverName;

    public LogEntry(int currentTerm, String msgId, String imageName, int clusterId, int senderName, int receiverName) {

        this.currentTerm = currentTerm;
        this.msgId = msgId;
        this.imageName = imageName;
        this.clusterId = clusterId;
        this.senderName = senderName;
        this.receiverName = receiverName;
    }
}
