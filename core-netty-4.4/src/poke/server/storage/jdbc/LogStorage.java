package poke.server.storage.jdbc;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.comm.App;
import poke.resources.ClusterEntry;
import poke.server.conf.ClusterConf;
import poke.server.conf.StorageInfo;
import poke.server.managers.LogEntry;
import poke.server.queue.RequestEntry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static poke.resources.ClusterEntry.Schema.*;

public class LogStorage {
    private static Logger logger = LoggerFactory.getLogger("LogStorage");

    private ClusterConf clusterConf;
    BoneCP cpool = null;
    String CLUSTER_ENTRY_TABLE="cluster_entry";
    String LOG_ENTRY_TABLE="log_entry";

    public LogStorage(ClusterConf clusterConf) {
        this.clusterConf = clusterConf;
        try {
            final StorageInfo storageInfo = clusterConf.getStorageInfo();
            Class.forName(storageInfo.getDriver());
            BoneCPConfig config = new BoneCPConfig();
            config.setJdbcUrl(storageInfo.getUrl());
            config.setUsername(storageInfo.getUser());
            config.setPassword(storageInfo.getPassword());
            config.setMinConnectionsPerPartition(5);
            config.setMaxConnectionsPerPartition(10);
            config.setPartitionCount(1);
            cpool = new BoneCP(config);
        } catch (Exception e) {
            e.printStackTrace();
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
                String updateClusterEntry = String.format("update %s where %s=%d %s=%d %s='%s' %s='%s'",
                        CLUSTER_ENTRY_TABLE, clusterId,fromClusterId,node_id,fromNodeId,node_ip,host,node_port,port);
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
            
          

            
          //  String saveLogEntry = String.format("insert into %s values(%d,%d,'%s','%s', %d, '%s', '%s', '%s', '%s',%d )",LOG_ENTRY_TABLE,fromClusterId,fromNodeId,host,port);
            //final int execute = statement.executeUpdate(saveLogEntry);

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {

        }
    }
}
