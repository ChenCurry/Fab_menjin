package test.util;

import java.sql.SQLException;
import java.util.Calendar;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;

public class AlarmClassTypeThread extends Thread {
	
    final long timeInterval = 1000*60*60*24;// 每天执行
	private UserDao userDao = new UserDaoImpl();
	
	public AlarmClassTypeThread() {
        super();
    }

	@Override
	public void run() {
		while (true) {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			try {
				// ------- code for task to run
				if(25==day) {
					userDao.updateInsertClassType();
				}
	            // ------- ends here
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SQLException e) {
				e.printStackTrace();
			}  
        }
    }
	
	
}
