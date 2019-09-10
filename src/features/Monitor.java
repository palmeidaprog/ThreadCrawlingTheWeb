package features;

public class Monitor {

    Buffer buffer;


    public Monitor(Buffer buffer) {
        this.buffer = buffer;
    }

    public synchronized String readFromBuffer(){
        return this.buffer.readBuffer();
    }

    public synchronized void writeOnBuffer(String image){
        this.buffer.addImageToBuffer(image);
    }
}