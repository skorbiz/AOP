package vehicle;

import java.io.Serializable;

public class Vehicle implements Serializable {
	
	private long waitTime;

	public Vehicle() {
	}
	
	public long getWaitTime() {
		return waitTime;
	}
	
	public void setWaitTime(long waitingTime) {
		waitTime = waitingTime;
	}
}
