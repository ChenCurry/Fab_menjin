package test.test;

import test.server.Business;

public class AT_test {

	public static void main(String []args) {
		Business business = new Business("","7003");
		try {
			business.register();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
