/*
 * copyright 2012, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import poke.comm.App;
import poke.comm.App.Request;
import poke.server.conf.ServerConf;
import poke.server.managers.RaftManager;
import poke.server.queue.RequestEntry;
import poke.server.resources.Resource;
import poke.server.storage.jdbc.LogStorage;
import poke.server.storage.jdbc.LogStorageFactory;

import java.sql.Connection;

public class JobResource implements Resource {
	public static Logger logger = LoggerFactory.getLogger(JobResource.class);
	ServerConf cfg;
	private final LogStorage logStorage = LogStorageFactory.getInstance();

	public JobResource(){
	}
	private Connection getConnection() {
		return logStorage.getConnection();
	}
	@Override
	public Request process(RequestEntry requestEntry) {
		final Integer leaderId = RaftManager.getInstance().getLeaderId();
		if(cfg.getNodeId()==leaderId){
			// I am leader hence will store the log and start log replication
			Connection connection = getConnection();
			try {
				logger.info("Received Request: \n {} ", requestEntry);
				if(requestEntry.request().hasJoinMessage()){
					// process join message-store the new cluster leader in DB for dynamic processing
					logStorage.saveClusterEntry(new ClusterEntry(requestEntry));
				} else{
				if(requestEntry.request().hasBody()){
						RaftManager.getInstance().processRequest(requestEntry.request());
				}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			// I am a follower hence will have to redirect to the leaderNode
			return null;

		}
		requestEntry.request().getBody().getJobOp().getAction().getNumber();
		return null;
	}

	@Override
	public void setConfig(ServerConf conf) {
		this.cfg = conf;
	}

}
