package test.test;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class UnitTest extends JFrame{
	

	public static void main(String args[]) {

		DecimalFormat df = new DecimalFormat("0.0");//设置保留位数
		long cumulative = 12600000L;
		String x = df.format(((float)cumulative/((float)1000*60*60)));
		System.out.println(x);
	}
	
	
	public static boolean notEmpty(String str) {
		if(null!=str&&!str.trim().equals("")) {
			return true;
		}else {
			return false;
		}
	}
	
	public static String nextDay(String date) {
		@SuppressWarnings("deprecation")
		Date dateTimeTo = new Date(date);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(dateTimeTo);
		calendar.add(Calendar.DATE, +1);
		dateTimeTo = calendar.getTime();
		SimpleDateFormat dft = new SimpleDateFormat("yyyy/MM/dd");// HH:mm:ss
		return dft.format(dateTimeTo);
	}
	
	
	public String classType_yearMonth(String timeFrom) {
		String yearMonth = "";
		String str = timeFrom.substring(8, 9);
		if("0".equals(str)) {
			str = timeFrom.substring(9, 10);
		}else {
			str = timeFrom.substring(8, 10);
		}
		if(Integer.parseInt(str)<26) {
			yearMonth = timeFrom.substring(0, 7);
		}else {
			//次月
			yearMonth = getPreMonth(timeFrom);
		}
		return 	yearMonth;
	}
	
	public static String getPreMonth(String repeatDate) {
        String lastMonth = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        int year = Integer.parseInt(repeatDate.substring(0, 4));
        String monthsString = repeatDate.substring(5, 7);
        int month;
        if ("0".equals(monthsString.substring(0, 1))) {
            month = Integer.parseInt(monthsString.substring(1, 2));
        } else {
            month = Integer.parseInt(monthsString.substring(0, 2));
        }
        cal.set(year,month,Calendar.DATE);
        lastMonth = dft.format(cal.getTime());
        return lastMonth;
    }
	
	public static Boolean N_is_sameday(String str1,String str2) throws ParseException {
		Boolean flag = true;
		SimpleDateFormat dft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//yyyy-MM-dd
		String temp2 = str1.substring(0, 10)+" 23:59:59";
		String temp3 = str1.substring(0, 10)+" 14:00:00";
		String temp4 = str1.substring(0, 10)+" 00:00:00";
		Calendar cld = Calendar.getInstance();
        cld.setTime(dft.parse(temp3));
        cld.add(Calendar.DATE, 1);
        long date5 = cld.getTime().getTime();//次日14点
        
		long date1 = dft.parse(str1).getTime();//
		long date2 = dft.parse(str2).getTime();//
		long date4 = dft.parse(temp2).getTime();//当日24点
		long date6 = dft.parse(temp4).getTime();//当日0点
		long date8 = dft.parse(temp3).getTime();//当日14点
		if((date1>date8&&date1<date4&&date2>date5)||(date1>date6&&date1<date8&&date2>date8)||(date2-date1)>14*60*60*1000) {
			System.out.println("date2:"+date2);
			System.out.println("date1:"+date1);
			System.out.println(date2-date1);
			System.out.println(14*60*60*1000);
			flag = false;
		}
		return flag;
	}
}
