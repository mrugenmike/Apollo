package poke.server.storage.jdbc;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.comm.App;
import poke.resources.ClusterEntry;
import poke.server.conf.ClusterConf;
import poke.server.conf.ServerConf;
import poke.server.conf.StorageInfo;
import poke.server.managers.LogEntry;
import poke.server.queue.RequestEntry;

import java.sql.*;

import static poke.resources.ClusterEntry.Schema.*;

public class LogStorage {
    private static Logger logger = LoggerFactory.getLogger("LogStorage");

    private final ClusterConf clusterConf;
    private final ServerConf serverConf;
    BoneCP cpool = null;
    final String CLUSTER_ENTRY_TABLE;
    final String LOG_ENTRY_TABLE;

    public LogStorage(ClusterConf clusterConf, ServerConf serverConf) {
        this.clusterConf = clusterConf;
        this.serverConf = serverConf;
        this.CLUSTER_ENTRY_TABLE = String.format("%d_cluster_entry",serverConf.getNodeId());
        this.LOG_ENTRY_TABLE=String.format("%d_log_entry",serverConf.getNodeId());
        try {
            final StorageInfo storageInfo = clusterConf.getStorage();
            Class.forName("com.mysql.jdbc.Driver");
            BoneCPDataSource dataSource = new BoneCPDataSource();
            dataSource.setJdbcUrl(storageInfo.getUrl());
            dataSource.setUsername(storageInfo.getUser());
            dataSource.setPassword(storageInfo.getPassword());
            dataSource.setMinConnectionsPerPartition(5);
            dataSource.setMaxConnectionsPerPartition(10);
            dataSource.setPartitionCount(1);
            cpool = new BoneCP(dataSource);
            initSchema();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSchema() throws SQLException {
        final Connection connection = getConnection();
        try {
            final Statement statement = connection.createStatement();
            String createLogEntryTable = String.format(" CREATE TABLE IF NOT EXISTS `raft`.`%s`  (\n" +
                    "  `index` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    `cluster_id` INT NOT NULL,\n" +
                    "    `node_id` INT NOT NULL,\n" +
                    "    `node_ip` VARCHAR(45) NOT NULL,\n" +
                    "    `node_port` VARCHAR(45) NOT NULL,\n" +
                    "    `currentTerm` INT NOT NULL,\n" +
                    "    `msgId` VARCHAR(50) NOT NULL,\n" +
                    "    `imageName` VARCHAR(50) NOT NULL,\n" +
                    "    `imageUrl` VARCHAR(150) NOT NULL,\n" +
                    "    `senderName` VARCHAR(50) NOT NULL,\n" +
                    "    `receiverName` VARCHAR(50) NOT NULL\n" +
                    "    );",LOG_ENTRY_TABLE);
            statement.execute(createLogEntryTable);
            logger.info("Creating {} table  for node {}",LOG_ENTRY_TABLE);
            String createClusterEntryTable = String.format("CREATE TABLE IF NOT EXISTS `raft`.`%s` (\n" +
                    "  `cluster_id` INT NOT NULL,\n" +
                    "  `node_id` INT NOT NULL,\n" +
                    "  `node_ip` VARCHAR(45) NOT NULL,\n" +
                    "  `node_port` VARCHAR(45) NOT NULL);",CLUSTER_ENTRY_TABLE);
            logger.info("Creating {} table  for node {}",CLUSTER_ENTRY_TABLE);
            statement.execute(createClusterEntryTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
        }

    }

    public Connection getConnection() {
        try {
            return cpool.getConnection();
        } catch (SQLException e) {
            logger.error("Failed to get connection from connection pool due to {}",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void saveClusterEntry(ClusterEntry clusterEntry) throws SQLException {
        final Connection connection = getConnection();
        final RequestEntry request = clusterEntry.getEntry();
        try {
            final Statement statement = connection.createStatement();
            final App.JoinMessage joinMessage = request.request().getJoinMessage();
            final int fromClusterId = joinMessage.getFromClusterId();
            final int fromNodeId = joinMessage.getFromNodeId();
            final String port = String.valueOf(clusterEntry.getEntry().getPort());
            final String host = clusterEntry.getEntry().getHost();

            if(!hasClusterEntry(fromClusterId)){
                String saveClusterEntry = String.format("insert into %s values(%d,%d,'%s','%s')",CLUSTER_ENTRY_TABLE,fromClusterId,fromNodeId,host,port);
                final int execute = statement.executeUpdate(saveClusterEntry);
            } else{
                //update cluster entry
                String updateClusterEntry = String.format("update %s SET %s=%d, %s=%d, %s='%s', %s='%s' where %s=%d ",
                        CLUSTER_ENTRY_TABLE, clusterId,fromClusterId,node_id,fromNodeId,node_ip,host,node_port,port,clusterId,fromClusterId);
                logger.info("Update Query {}",updateClusterEntry);
                final int updateCount = statement.executeUpdate(updateClusterEntry);
                if(updateCount==1){
                    logger.info("update cluster entry for Cluster Id {}",clusterId);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            connection.close();
        }
    }

    private boolean hasClusterEntry(int fromClusterId) throws SQLException {
        final Connection connection = getConnection();
        try {
            final String findClusterEntry = String.format("select * from %s where %s=%d", CLUSTER_ENTRY_TABLE, clusterId, fromClusterId);
            final ResultSet resultSet = connection.createStatement().executeQuery(findClusterEntry);
            if(!resultSet.isBeforeFirst()){
                logger.info("No CLuster Entry found for cluster ID {}",clusterId);
                return false;
            }else{
                return true;
            }
        } catch (SQLException e) {
            logger.error("DB error occured " + e.getMessage());
        }finally {
            connection.close();
        }
        return false;
    }

    public void saveLogEntry(LogEntry logEntry) throws SQLException {
        final Connection connection = getConnection();
        try {
            final Statement statement = connection.createStatement();
            
          
           int clusterId=logEntry.getClusterId();
           int nodeId=logEntry.getNodeId();
           String nodeIp=logEntry.getNodeIp();
          
           int currentTerm=logEntry.getCurrentTerm();
           String msgId=logEntry.getMsgId();
           String imageName=logEntry.getImageName();
           int senderName=logEntry.getSenderName();
           int receiverName=logEntry.getReceiverName();
           String imageUrl = logEntry.getImageUrl();
         
            
          String saveLogEntry = String.format("insert into %s (cluster_id,node_id,node_ip,node_port,currentTerm,msgId,imageName,imageUrl,senderName,receiverName) values( %d, %d,'%s','%s', %d, '%s', '%s','%s', '%s', '%s')",LOG_ENTRY_TABLE,clusterId,nodeId,nodeIp,"-1",currentTerm,msgId,imageName,imageUrl,senderName,receiverName);

          final int execute = statement.executeUpdate(saveLogEntry);

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {

        }
    }
}
