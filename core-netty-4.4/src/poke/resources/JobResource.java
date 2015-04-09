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

import poke.comm.App.Request;
import poke.server.conf.ServerConf;
import poke.server.managers.ElectionManager;
import poke.server.resources.Resource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JobResource implements Resource {
	ServerConf cfg;
	final String className = "com.mysql.jdbc.Driver";
	final String jdbcUrl = "jdbc:mysql://localhost:3306/raft";
	final String dbUserName = "root";
	final String password = "";
	public JobResource(){
	}
	private Connection getConnection() {

		try {
			Class.forName(className).newInstance();
			return DriverManager.getConnection(jdbcUrl, dbUserName, password);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public Request process(Request request) {
		final Integer leaderId = ElectionManager.getInstance().whoIsTheLeader();
		if(cfg.getNodeId()==leaderId){
			// I am leader hence will store the log and start log replication
			Connection connection = getConnection();
			try {
				final int jobActionNumber = request.getBody().getJobOp().getAction().getNumber();
				switch(jobActionNumber){
					case 3:{
						final Statement statement = connection.createStatement();
						break;
					}
					default:
						System.out.println("................action not recognized.......");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			// I am a follower hence will have to redirect to the leaderNode

		}
		request.getBody().getJobOp().getAction().getNumber();
		return null;
	}

	@Override
	public void setConfig(ServerConf conf) {
		this.cfg = conf;
	}

}
