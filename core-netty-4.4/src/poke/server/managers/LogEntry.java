package poke.server.managers;

public class LogEntry {
    public int getIndex() {
		return index;
	}

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

    public int getNodeId() {
		return nodeId;
	}

	private final int currentTerm;
    private final String msgId;
    private final String imageName;
    private final int clusterId;
    private final int senderName;
    private final int receiverName;
    private final String imageUrl;
    private final int nodeId;
    private final String nodeIp;
    private final int index;
    

    public String getNodeIp() {
		return nodeIp;
	}

	public LogEntry(int currentTerm, String msgId, String imageName, int clusterId, int senderName, int receiverName, String imageUrl, int nodeId, String nodeIp, int index) {

        this.currentTerm = currentTerm;
        this.msgId = msgId;
        this.imageName = imageName;
        this.clusterId = clusterId;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.imageUrl=imageUrl;
        this.nodeId=nodeId;
        this.nodeIp=nodeIp;
        this.index=index;
        
        
        
    }

	public String getImageUrl() {
		return imageUrl;
	}
}
