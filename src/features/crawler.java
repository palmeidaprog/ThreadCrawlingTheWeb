package features;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

public class crawler {
	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);  // Para ler a entrada do usu�rio.
		String urls;   // URL inicial, do usu�rio ou arg [0].
		int flagValid;
		int bufferSize = 14;
		Gson g = new Gson();
		
		System.out.print("Digite o URL de inicio:  ");
		urls = g.toJson(stdin.nextLine());
		
		ExecutorService bufferURL = Executors.newFixedThreadPool(bufferSize);
        ExecutorService bufferImage = Executors.newFixedThreadPool(bufferSize);
        
        List<String> linkURLs = new ArrayList<>(Arrays.asList(urls));
        List<String> imagemURL = new ArrayList<>();
        
		Buffer bufferCompartilhadoImages = new Buffer(imagemURL);
		Buffer bufferCompartilhadoLinks = new Buffer(linkURLs);
		
		getURL getUrlStart = new getURL(bufferCompartilhadoLinks);
		Images imagesStart = new Images(bufferCompartilhadoImages);

       
       try {
    	   getUrlStart.start();
    	   imagesStart.start();
    	   bufferURL.execute(new getURL(bufferCompartilhadoLinks));
    	   bufferImage.execute(new Images(bufferCompartilhadoImages));
       } catch  (Exception exception ) {
           exception.printStackTrace();
       }

	}
}
