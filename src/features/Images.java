package features;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Images extends Thread {
    private static int cont = 0;
    private final String outputFolder = "src/images";
    private final String name = "output" + cont;
    private Buffer bufferImage;

    public Images(Buffer bufferImage) {
        this.bufferImage = bufferImage;
    }

    public void run() {
        String imageURL = bufferImage.getFromBuffer();
        if(imageURL != null){
            try {
                Connection.Response imageFromBuffer = Jsoup.connect(imageURL).ignoreContentType(true).execute();           
                OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new java.io.File(this.outputFolder + this.name)));  
                out.write(imageFromBuffer.body());
                out.close();   
                cont++;	
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}