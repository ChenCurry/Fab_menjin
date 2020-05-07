package test.util;

import test.server.Business;

public class AlarmThread extends Thread {
	String ipAd;
	String localPort;
	
	public AlarmThread(String ipAd,String localPort) {
		this.ipAd = ipAd;
		this.localPort = localPort;
	}

	public void run() {
		Business business = new Business(ipAd,localPort);
		try {
			business.register();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
