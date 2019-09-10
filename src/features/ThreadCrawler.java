package features;

public abstract class ThreadCrawler implements Runnable {
	  Monitor monitorInput;
	  Monitor monitorOutput;
	  
	  public ThreadCrawler(Monitor monitorInput, Monitor monitorOutput) {
	        this.monitorInput = monitorInput;
	        this.monitorOutput = monitorOutput;
	  }
}