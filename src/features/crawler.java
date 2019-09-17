package features;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class crawler {
	public static void main(String[] args) {
		int bufferSize = 14;
		String fileName = "links.txt";
		String line = null;
		ArrayList<String> urls = new ArrayList<String>();

		try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                urls.add(line);
            }   
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
		}

		//baseado no cliente consumidor

		ExecutorService bufferURL = Executors.newFixedThreadPool(bufferSize);
		Buffer bufferCompartilhadoLinks = new Buffer(urls);
		getURL getUrlStart = new getURL(bufferCompartilhadoLinks);
		Images imageURLStart = new Images(bufferCompartilhadoLinks);

       
	    try {
			getUrlStart.start();
			imageURLStart.start();
			bufferURL.execute(getUrlStart);
	    } catch  (Exception exception ) {
		   exception.printStackTrace();
	    }

	    bufferURL.shutdown();

	}
}
