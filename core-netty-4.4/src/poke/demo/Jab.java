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
package poke.demo;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;

/**
 * DEMO: how to use the command class
 * 
 * @author gash
 * 
 */
public class Jab {
	private String tag;
	private int count;

	public Jab(String tag) {
		this.tag = tag;
	}
// Mrugen-10.189.172.25
// Harshad- 10.189.79.55
	public void run() throws IOException {
		ClientCommand cc = new ClientCommand("169.254.54.145", 5572);
		CommListener listener = new ClientPrintListener("jab demo");
		cc.addListener(listener);
	 
	     byte[] buffer = Files.readAllBytes(Paths.get("/Users/mrugen/Desktop/ebay.png"));

	 //      cc.join(tag,count); // Join Message
			
			cc.payLoadCluster(tag, count, buffer, "back.jpg"); // Pay Load Cluster
			
		//	cc.payLoadClient(tag, count,bytes, "background.jpg"); //  Pay load Client

//		}
	}

	public static void main(String[] args) {
		try {
			Jab jab = new Jab("jab");
			jab.run();

			// we are running asynchronously
			System.out.println("\nExiting in 5 seconds");
			Thread.sleep(5000);
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
