package poke.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class ClientTest {

	public static void main(String[] args) throws IOException {
		
		
		ClientConnection cc = new ClientConnection("localhost",6000);
		ClientListener listener = new ClientPrintListener("document listner");
		cc.addListener(listener);
		
		String filePath="README.txt";
		String line,content="No Content";
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		
		try {
			line = br.readLine();
			while (line != null) {
		            sb.append(line);
		            sb.append(System.getProperty("line.separator"));
		            line = br.readLine();
		        }
			content="";
			content = sb.toString();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally {
		        br.close();
		 }
		
		cc.poke(content, 1);
	}

}
