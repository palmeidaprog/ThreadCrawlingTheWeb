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
		Scanner stdin = new Scanner(System.in);  // Para ler a entrada do usuario.
		String urls;   // URL inicial, do usuario ou arg [0].
		int flagValid;
		int bufferSize = 14;
		Gson g = new Gson();
		
		System.out.print("Digite o URL de inicio:  ");
		urls = g.toJson(stdin.nextLine());
		
		ExecutorService bufferURL = Executors.newFixedThreadPool(bufferSize);
        ExecutorService bufferImage = Executors.newFixedThreadPool(bufferSize);
        
        List<String> linkURLs = new ArrayList<>(Arrays.asList(urls));
        List<String> imagemURL = new ArrayList<>();
        
		Buffer bufferCompartilhadoLinks = new Buffer(linkURLs);
		
		getURL getUrlStart = new getURL(bufferCompartilhadoLinks);
		Images imagesStart = new Images(bufferCompartilhadoLinks);

       
       try {
    	   bufferURL.execute(getUrlStart);
    	   bufferImage.execute(imagesStart);
    	   getUrlStart.start();
    	   imagesStart.start();
       } catch  (Exception exception ) {
           exception.printStackTrace();
       }
       
       bufferURL.shutdown();
       bufferURL.shutdown();

	}
}
