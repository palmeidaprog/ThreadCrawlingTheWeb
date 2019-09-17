package features;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class getURL extends Thread {
    private Buffer bufferURL;

    public getURL(Buffer bufferURL) {
        this.bufferURL = bufferURL;
    }
    
    public void run() {
        try {
            String url = bufferURL.getFromBuffer();
            if(url != null) {
                Document document = Jsoup.connect(url).get();
                // mais de um tipo de tag
                for (Element element : document.select("img[src~=(?i)\\.(png|jpe?g|gif)]")) {
                    String imageUrl = element.attr("src");
                    bufferURL.setToBuffer(imageUrl);
                    // passar a tag pra poder ser mais facil de fazer download
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
