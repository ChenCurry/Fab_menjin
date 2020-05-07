package test.test;

import test.util.AlarmClassTypeThread;
import test.util.AlarmThread;

public class RecordStart {
	public static void main(String[] args) {
		AlarmThread testThread1 = new AlarmThread("192.168.1.154","7200");//后面的端口是本机的端口
		AlarmThread testThread2 = new AlarmThread("192.168.1.155","7201");
		AlarmThread testThread3 = new AlarmThread("192.168.1.156","7202");
		testThread1.start();
		testThread2.start();
		testThread3.start();
		
		AlarmClassTypeThread classType = new AlarmClassTypeThread();
		classType.start();
	}
}
