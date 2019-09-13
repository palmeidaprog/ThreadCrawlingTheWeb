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
		String[] urls;   // URL inicial, do usu�rio ou arg [0].
		int flagValid;
		int bufferSize = 14;
		
		System.out.print("Digite o URL de in�cio:  ");
		urls = convertToJson(stdin.nextLine());
		
		do {
			try {
				URL validStartURL = new URL(urls[0]);
				flagValid = 0;
			}
			catch (MalformedURLException e) {
				System.out.println("Desculpe, URL n�o � valida.");
				flagValid = 1;
				return;
			}
		} while(flagValid == 1);

		ExecutorService bufferURL = Executors.newFixedThreadPool(bufferSize);
        ExecutorService bufferImage = Executors.newFixedThreadPool(bufferSize);


       List<String> linkURLs = new ArrayList<>(Arrays.asList(urls));
       List<String> imagemURL = new ArrayList<>();

       Monitor urlMonitor = new Monitor(new Buffer(linkURLs));
       Monitor imagesMonitor = new Monitor(new Buffer(imagemURL));
       Monitor outputMonitor = new Monitor(new Buffer(new ArrayList<>()));

       List<Runnable> urlThreads = createUrlRunnables(urlMonitor, imagesMonitor,  bufferSize/ 2);
       List<Runnable> imageThreads = createImageRunnables(imagesMonitor, outputMonitor, bufferSize / 2);

            urlThreads.forEach(bufferURL::execute);
            imageThreads.forEach(bufferImage::execute);
            bufferURL.shutdown();
            bufferImage.shutdown();

	}

    public static String[] convertToJson(String filePath) {
        String json = null;
        try {
            json = readString(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(json);
        return new Gson().fromJson(json, String[].class);
    }
}
