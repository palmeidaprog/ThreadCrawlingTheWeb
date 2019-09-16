package features;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Images extends Thread {
    private final String outputFolder = "src/images/";;
    private Buffer bufferImage;

    public Images(Buffer bufferImage) {
        this.bufferImage = bufferImage;
    }

    public void run() {
        String imageURL = bufferImage.getFromBuffer();
        if(imageURL != null){
            try {
                //fazer split pra separar links
                String[] files = imageURL.split("/");
                URL urlImage = new URL(imageURL);
                //usar javaIO pra poder fazer certiho
                InputStream in = urlImage.openStream();
                byte[] buffer = new byte[4096];
                int n = -1;
                // files.lenght - 1 pra nao ultrapassar o buffer
                OutputStream os = new FileOutputStream(outputFolder + files[files.length - 1]);  
                while ( (n = in.read(buffer)) != -1 ){
                    os.write(buffer, 0, n);
                }
                //close the stream
                os.close();
                in.close();
                System.out.println("Image saved");  	
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}