package features;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private List<String> images;
 
    public synchronized void setToBuffer(String image) {
        images.add(image);
        notifyAll();
    }
 
    public synchronized String getFromBuffer() {
        if(images.size() > 0) {
            return images.remove(images.size() - 1);
        } 
        else {
            notifyAll();
            return null;
        }
    }
}
