package features;

import java.util.ArrayList;

public class Buffer {
    private ArrayList<String> images;
    
    public Buffer (ArrayList<String> images) {
    	this.images = images;
    }
 
    public synchronized void setToBuffer(String image) {
        images.add(image);
        notifyAll();
    }
 
    public synchronized String getFromBuffer() {
        if(images.size() > 0) {
        	notifyAll();
            return images.remove(images.size() - 1);
        } 
        else {
        	notifyAll();
            return null;
        }
        
    }
}
