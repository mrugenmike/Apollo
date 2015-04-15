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



import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.amazonaws.util.Base64;

import poke.client.ClientCommand;
import poke.client.ClientPrintListener;
import poke.client.comm.CommListener;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

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

	public void run() throws IOException {
		ClientCommand cc = new ClientCommand("10.189.172.25", 5572);
		CommListener listener = new ClientPrintListener("jab demo");
		cc.addListener(listener);

		
		/*String dirName="/Users/Akki/downloads/";
		ByteArrayOutputStream baos=new ByteArrayOutputStream(1000);
		BufferedImage img=ImageIO.read(new File(dirName,"background.jpg"));
		ImageIO.write(img, "jpg", baos);
		baos.flush();
 
		byte[] base64String=Base64.encode(baos.toByteArray());
		baos.close();
 
		byte[] bytearray = Base64.decode(base64String);
 
		BufferedImage imag=ImageIO.read(new ByteArrayInputStream(bytearray));*/
		//ImageIO.write(imag, "jpg", new File(dirName,"snap.jpg"));
		
		 File file = new File("/Users/Akki/downloads/background.jpg");
	        System.out.println(file.exists() + "!!");
	 
	        FileInputStream fis = new FileInputStream(file);
	        //create FileInputStream which obtains input bytes from a file in a file system
	        //FileInputStream is meant for reading streams of raw bytes such as image data. For reading streams of characters, consider using FileReader.
	 
	        //InputStream in = resource.openStream();
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] buf = new byte[1024];
	        try {
	            for (int readNum; (readNum = fis.read(buf)) != -1;) {
	                bos.write(buf, 0, readNum); 
	            
	            }
	        } catch (IOException ex) {
	        //    Logger.getLogger(ConvertImage.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        byte[] bytes = bos.toByteArray();
	        //bytes is the ByteArray we need		
		
	//	for (int i = 0; i < 3; i++) {
			count++;
			//cc.poke(tag, count);
			
	 //      cc.join(tag,count); // Join Message
			
			cc.payLoadCluster(tag, count, bytes, "background.jpg"); // Pay Load Cluster
			
			cc.payLoadClient(tag, count,bytes, "background.jpg"); //  Pay load Client
			
			
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
