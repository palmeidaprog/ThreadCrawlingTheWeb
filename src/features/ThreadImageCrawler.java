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

public class ThreadImageCrawler {
	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);  // Para ler a entrada do usuário.
		String[] urls;   // URL inicial, do usuário ou arg [0].
		int flagValid;
		int bufferSize = 14;
		
		System.out.print("Digite o URL de início:  ");
		urls = convertToJson(stdin.nextLine());
		
		do {
			try {
				URL validStartURL = new URL(urls[0]);
				flagValid = 0;
			}
			catch (MalformedURLException e) {
				System.out.println("Desculpe, URL não é valida.");
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

    public static List<Runnable> createUrlRunnables(Monitor monitorInput, Monitor monitorOutput, int size) {
        List<Runnable> runnables = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            try {
                runnables.add(new getURL(monitorInput, monitorOutput));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return runnables;
    }

    public static List<Runnable> createImageRunnables(Monitor monitorInput, Monitor monitorOutput, int size) {
        List<Runnable> runnables = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            try {
                runnables.add(new Images(monitorInput, monitorOutput));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return runnables;
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
