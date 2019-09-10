package features;

import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private List<String> images;

    public Buffer(List<String> images) {
        this.images = images;
    }

    public String readBuffer() {
        if(images.size() > 0) {
            return images.remove(images.size() - 1);
        } 
        else {
            return null;
        }
    }

    public void addImageToBuffer(String image){
        images.add(image);
    }
}
