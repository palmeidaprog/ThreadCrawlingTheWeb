package features;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class getURL extends ThreadCrawler {

    public getURL(Monitor monitorInput, Monitor monitorOutput) {
        super(monitorInput, monitorOutput);
    }

    @Override
    public void run() {
        try {
            String url = this.monitorInput.readFromBuffer();
            if(url != null) {
                Document document = Jsoup.connect("http://" + url).get();
                for (Element element : document.select("img")) {
                    String imageUrl = element.absUrl("src");
                    this.monitorOutput.writeOnBuffer(imageUrl);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
