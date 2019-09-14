package features;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;

public class Images extends Thread {
    private Buffer bufferImage;

    public Images(Buffer bufferImage) {
        this.bufferImage = bufferImage;
    }

    public void run() {
        String imageURL = bufferImage.getFromBuffer();
        if(imageURL != null){
            try {
            	Connection.Response imageFromBuffer = Jsoup.connect(imageURL).ignoreContentType(true).execute();                	
            	File file = File.createTempFile("image", ".pgn");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}