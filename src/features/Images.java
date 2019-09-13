package features;

import org.jsoup.Jsoup;
import java.io.IOException;

public class Images extends Thread {
    private Buffer bufferImage;

    public Images(String url, Buffer bufferImage) {
        this.bufferImage = bufferImage;
    }

    @Override
    public void run() {
        String imageUrl = bufferImage.getFromBuffer();
        if(imageUrl != null){
            try {
                String data = Jsoup.connect(imageUrl).ignoreContentType(true).execute().body();
                bufferImage.setToBuffer(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}