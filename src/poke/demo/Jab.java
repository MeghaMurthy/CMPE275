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
import java.io.InputStream;

import poke.client.ClientConnection;
import poke.client.ClientListener;
import poke.client.ClientPrintListener;

public class Jab {
	private String tag;
	private int count;

	public Jab(String tag) {
		this.tag = tag;
	}

	public void run() throws InterruptedException {
//		ClientConnection cc = ClientConnection.initConnection("192.168.2.43", 6000);
		ClientConnection cc = ClientConnection.initConnection("localhost", 6000);
		ClientListener listener = new ClientPrintListener("jab demo");
		cc.addListener(listener);

//		cc.addDoc("TestFolder", 2, "megha", "file_2.txt", "blah".getBytes());
//		cc.addDoc("TestFolder", 3, "megha", "file_3.txt", "blah blah".getBytes());
//		cc.addDoc("TestFolder", 4, "megha", "file_4.txt", "blah blah blah".getBytes());
//		cc.addDoc("TestFolder", 5, "megha", "file_5.txt", "blah blah blah blah".getBytes());

		cc.getDoc("TestFolder","file_2.txt");
		
		
//		cc.poke(tag, count);
//		Thread.sleep(1000000);
		
//		for (int i =0;true; i++) {
//			count++;
//			Thread.sleep(5000);
//			cc.poke(tag, count);
//		}
//		cc.addNameSpace("TestFolder");
//		
//		while (true){
//	//System.out.println("Creating File");
//			cc.addDoc("TestFolder","test_file.txt");
//			Thread.sleep(6000);			
//	//	System.out.println("Pulling the files from TestFolder, README.txt");
//		}
//		cc.getDoc("TestFolder","TestSendFile.zip");	
	}

	public static void main(String[] args) {
		try {
			Jab jab = new Jab("jab");
			jab.run();
		
		
			
	//		InputStream in= new FileInputStream("/home/mahajan/cmpe275/Project01/core-netty/README.txt");
			
			// we are running asynchronously
			System.out.println("\nExiting in 5 seconds");
			Thread.sleep(600000);
			System.exit(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
