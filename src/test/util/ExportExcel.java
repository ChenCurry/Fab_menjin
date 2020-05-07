package test.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;
import test.domain.AlarmInfo;
import test.domain.Correspondingrelation;
import test.domain.CurrentInfo;
import test.domain.InoutRecord;

public class ExportExcel {
	private UserDao userDao=new UserDaoImpl();

	/**
	 * 导出汇总数据
	 */
	public void exportSummaryExcel() {
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = "汇总" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<CurrentInfo> currentInfoList = new ArrayList<CurrentInfo>();
		try {
			currentInfoList = userDao.queryCurrentInfoAll();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,2000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,2000);   
		sheet.setColumnWidth(6,5000); 

		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("说明:导出信息为截止"+time_+"  Fab内的人员信息");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("通道");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("卡号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("进出");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("时间");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("工号");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("姓名");//姓名
		titleCell5.setCellStyle(style2);
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("部门");//姓名
		titleCell6.setCellStyle(style2);
		
		// 4.遍历数据,创建数据行
		for (CurrentInfo infoOne : currentInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(0).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(0).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(0).setCellValue("通道三");
			}
			dataRow.createCell(1).setCellValue(infoOne.getCarNo());
			dataRow.createCell(2).setCellValue("进");
			dataRow.createCell(3).setCellValue(infoOne.getStruTime());
			dataRow.createCell(4).setCellValue(infoOne.getJobNo());
			dataRow.createCell(5).setCellValue(infoOne.getEmpName());
			dataRow.createCell(6).setCellValue(infoOne.getDept());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将" + fileName + "保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 导出进出记录
	 * @param string
	 * @param string2
	 */
	public void exportInOutExcel(String string, String string2) {
		String dateFrom = string;//+" 00:00:00"
		String dateTo = string2;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = "进出记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		//
		List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
		try {
			alarmInfoList=userDao.queryInOutFromTo(dateFrom,dateTo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,6));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,2000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,2000);   
		sheet.setColumnWidth(6,5000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("通道");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("卡号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("进出");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("时间");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("工号");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("姓名");//姓名
		titleCell5.setCellStyle(style2);
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("部门");//姓名
		titleCell6.setCellStyle(style2);
		
		// 4.遍历数据,创建数据行
		for (AlarmInfo infoOne : alarmInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(0).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(0).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(0).setCellValue("通道三");
			}
			
			dataRow.createCell(1).setCellValue(infoOne.getCarNo());
			if("1".equals(infoOne.getDwCardReaderNo())) {
				dataRow.createCell(2).setCellValue("进");
			}else {
				dataRow.createCell(2).setCellValue("出");
			}
			dataRow.createCell(3).setCellValue(infoOne.getStruTime());
			dataRow.createCell(4).setCellValue(infoOne.getJobNo());
			dataRow.createCell(5).setCellValue(infoOne.getEmpName());
			dataRow.createCell(6).setCellValue(infoOne.getDept());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将" + fileName + "保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 导出进出记录  已做更改
	 */
	public void exportReportInOutExcel(String timeFrom,String timeTo,String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-进出记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList=userDao.queryReportInOutNew(dateFrom,dateTo,deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,8));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,2000);   
		sheet.setColumnWidth(6,5000);   
		sheet.setColumnWidth(7,4000);   
		sheet.setColumnWidth(8,4000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("通道");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("卡号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("进");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("出");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("工号");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("姓名");//姓名
		titleCell5.setCellStyle(style2);
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("部门");//部门
		titleCell6.setCellStyle(style2);
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("分部");//分部
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("班别");//分部
		titleCell8.setCellStyle(style2);
		
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(0).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(0).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(0).setCellValue("通道三");
			}
			dataRow.createCell(1).setCellValue(infoOne.getCarNo());
			dataRow.createCell(2).setCellValue(infoOne.getInTime());
			dataRow.createCell(3).setCellValue(infoOne.getOutTime());
			dataRow.createCell(4).setCellValue(infoOne.getJobNo());
			dataRow.createCell(5).setCellValue(infoOne.getEmpName());
			dataRow.createCell(6).setCellValue(infoOne.getDept());
			dataRow.createCell(7).setCellValue(infoOne.getParcel());
			dataRow.createCell(8).setCellValue(infoOne.getClassType());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 导出<10hours
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 */
	public void exportReport10hExcel(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		//String timeFrom,String timeTo,String deptRange,String parcelRange
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-Fab时数" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReport10h2(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,10));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,4000);   
		sheet.setColumnWidth(6,4000);   
		sheet.setColumnWidth(7,5000);   
		sheet.setColumnWidth(8,5000);   
		sheet.setColumnWidth(9,4000);   
		sheet.setColumnWidth(10,2000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//出
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//出
		titleCell5.setCellStyle(style2);
		
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("进入时间");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("最后离开记录");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("Fab时数");//分部
		titleCell9.setCellStyle(style2);
		HSSFCell titleCell10 = titlerRow1.createCell(10);
		titleCell10.setCellValue("进出次数");//分部
		titleCell10.setCellStyle(style2);
		
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getInTime());
			dataRow.createCell(8).setCellValue(infoOne.getOutTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
			dataRow.createCell(10).setCellValue(infoOne.getInOutCount());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 导出 常日班 fab时长
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 */
	public void exportReport10hExcelNor(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-Fab时数" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReport10h2Nor(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,10));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,4000);   
		sheet.setColumnWidth(6,4000);   
		sheet.setColumnWidth(7,5000);   
		sheet.setColumnWidth(8,5000);   
		sheet.setColumnWidth(9,4000);   
		sheet.setColumnWidth(10,2000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//出
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//出
		titleCell5.setCellStyle(style2);
		
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("进入时间");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("最后离开记录");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("Fab时数");//分部
		titleCell9.setCellStyle(style2);
		HSSFCell titleCell10 = titlerRow1.createCell(10);
		titleCell10.setCellValue("进出次数");//分部
		titleCell10.setCellStyle(style2);
		
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getInTime());
			dataRow.createCell(8).setCellValue(infoOne.getOutTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
			dataRow.createCell(10).setCellValue(infoOne.getInOutCount());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 导出迟到
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 */
	public void exportReportLaterExcel(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-迟到记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReportLater2(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,9));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,4000);   
		sheet.setColumnWidth(6,4000);   
		sheet.setColumnWidth(7,5000);   
		sheet.setColumnWidth(8,5000);   
		sheet.setColumnWidth(9,4000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//出
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//出
		titleCell5.setCellStyle(style2);
		
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("进");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("出");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("迟到分钟数");//分部
		titleCell9.setCellStyle(style2);
				
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getInTime());
			dataRow.createCell(8).setCellValue(infoOne.getOutTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 导出早退
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 */
	public void exportReportLeaveExcel(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-早退记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReportLeave2(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,9));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,4000);   
		sheet.setColumnWidth(6,4000);   
		sheet.setColumnWidth(7,5000);   
		sheet.setColumnWidth(8,5000);   
		sheet.setColumnWidth(9,4000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//工号
		titleCell5.setCellStyle(style2);
		
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("进");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("出");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("早退分钟数");//分部
		titleCell9.setCellStyle(style2);
						
		
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getInTime());
			dataRow.createCell(8).setCellValue(infoOne.getOutTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 查询用餐时间超过45分 绳之以法
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 */
	public void exportReport45mExcel(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-用餐时间大于45m记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReport45m(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,9));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,4000);   
		sheet.setColumnWidth(6,4000);   
		sheet.setColumnWidth(7,5000);   
		sheet.setColumnWidth(8,5000);   
		sheet.setColumnWidth(9,4000);   
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//工号
		titleCell5.setCellStyle(style2);
		
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("出去时间");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("归来时间");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("用餐分钟数");//分部
		titleCell9.setCellStyle(style2);
				
				
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getOutTime());
			dataRow.createCell(8).setCellValue(infoOne.getInTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
		}

		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}
	
	/**
	 * 选项卡二 外出时间大于30m记录
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 */
	public void exportReport30mExcel(String timeFrom, String timeTo, String deptRange,String parcelRange,String yearMonth,String jobNo) {
		String dateFrom = timeFrom;
		String dateTo = timeTo;
		Date date = new Date();
		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = deptRange+"-"+parcelRange+"-外出时间大于30m记录" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		List<InoutRecord> inoutRecordInfoList = new ArrayList<InoutRecord>();
		try {
			inoutRecordInfoList = userDao.queryReport30m(timeFrom, timeTo, deptRange,parcelRange,yearMonth,jobNo);
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,9));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);
		sheet.setColumnWidth(5,4000); 
		sheet.setColumnWidth(6,4000); 
		sheet.setColumnWidth(7,5000);
		sheet.setColumnWidth(8,5000);
		sheet.setColumnWidth(9,4000);
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("该记录为"+dateFrom+" 00:00:00至"+dateTo+" 23:59:59的"+deptRange+"的记录");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//工号
		titleCell4.setCellStyle(style2);
		
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("班别");//工号
		titleCell5.setCellStyle(style2);
		HSSFCell titleCell6 = titlerRow1.createCell(6);
		titleCell6.setCellValue("通道");//工号
		titleCell6.setCellStyle(style2);
		
		HSSFCell titleCell7 = titlerRow1.createCell(7);
		titleCell7.setCellValue("出Fab时间");//姓名
		titleCell7.setCellStyle(style2);
		HSSFCell titleCell8 = titlerRow1.createCell(8);
		titleCell8.setCellValue("之后进Fab时间");//部门
		titleCell8.setCellStyle(style2);
		HSSFCell titleCell9 = titlerRow1.createCell(9);
		titleCell9.setCellValue("外出分钟数");//分部
		titleCell9.setCellStyle(style2);
		// 4.遍历数据,创建数据行
		for (InoutRecord infoOne : inoutRecordInfoList) { // 获取最后一行的行号
			int lastRowNum = sheet.getLastRowNum();
			HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
			
			dataRow.createCell(0).setCellValue(infoOne.getCarNo());
			dataRow.createCell(1).setCellValue(infoOne.getJobNo());
			dataRow.createCell(2).setCellValue(infoOne.getEmpName());
			dataRow.createCell(3).setCellValue(infoOne.getDept());
			dataRow.createCell(4).setCellValue(infoOne.getParcel());
			dataRow.createCell(5).setCellValue(infoOne.getClassType());
			
			String access = infoOne.getAdrass();
			if("192.168.1.154".equals(access)) {
				dataRow.createCell(6).setCellValue("通道一");
			}else if("192.168.1.155".equals(access)) {
				dataRow.createCell(6).setCellValue("通道二");
			}else if("192.168.1.156".equals(access)) {
				dataRow.createCell(6).setCellValue("通道三");
			}
			dataRow.createCell(7).setCellValue(infoOne.getOutTime());
			dataRow.createCell(8).setCellValue(infoOne.getInTime());
			dataRow.createCell(9).setCellValue(infoOne.getSpareField());
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 保存到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "保存统计信息失败。");
		}
	}

	@SuppressWarnings("static-access")
	public void exportEmpInfo(String empDept, String empParcel, String jobNoJTF) {
		Date date = new Date();
		@SuppressWarnings("deprecation")
		int today = date.getDate();
		String time1 = "";//当月
		Calendar cal = Calendar.getInstance();
		cal.add(cal.MONTH, +1);
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
		String time3 = dft.format(cal.getTime());
		if (today < 26) {// 每月26号后 算作下一月的班别
			time1 = dft.format(date);//当月
		} else {
			time1 = time3;//当月
		}
		List<Correspondingrelation> correspondingrelationList = new ArrayList<Correspondingrelation>();
		try {
			correspondingrelationList = userDao.getDataEmpInfo2(empDept, empParcel, time1,jobNoJTF);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		System.currentTimeMillis();
		String time_ = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(date);
		String fileName = empDept+"-"+empParcel+"-员工班别信息" + time_ + ".xls";
		Properties prop = System.getProperties();
		String username = prop.getProperty("user.name");
		String path = "C:\\Users\\" + username + "\\Desktop\\" + fileName;
		
		// 1.在内存中创建一个excel文件
		@SuppressWarnings("resource")
		HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
		// 2.创建工作簿
		HSSFSheet sheet = hssfWorkbook.createSheet();
		sheet.addMergedRegion(new CellRangeAddress(0,0,0,5));
		sheet.setColumnWidth(0,4000);   
		sheet.setColumnWidth(1,3200);   
		sheet.setColumnWidth(2,5000);   
		sheet.setColumnWidth(3,5000);   
		sheet.setColumnWidth(4,4000);   
		sheet.setColumnWidth(5,5000);
		// 3.创建标题行
		HSSFRow titlerRow0 = sheet.createRow(0);
		HSSFCell commentsCell = titlerRow0.createCell(0);
		commentsCell.setCellValue("当月班别信息");
		HSSFCellStyle style = hssfWorkbook.createCellStyle(); //样式对象   设置背景颜色
		style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		commentsCell.setCellStyle(style);
		HSSFCellStyle style2 = hssfWorkbook.createCellStyle(); //表头
		style2.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFRow titlerRow1 = sheet.createRow(1);
		HSSFCell titleCell0 = titlerRow1.createCell(0);
		titleCell0.setCellValue("卡号");//通道
		titleCell0.setCellStyle(style2);
		HSSFCell titleCell1 = titlerRow1.createCell(1);
		titleCell1.setCellValue("工号");//卡号
		titleCell1.setCellStyle(style2);
		HSSFCell titleCell2 = titlerRow1.createCell(2);
		titleCell2.setCellValue("姓名");//进
		titleCell2.setCellStyle(style2);
		HSSFCell titleCell3 = titlerRow1.createCell(3);
		titleCell3.setCellValue("部门");//出
		titleCell3.setCellStyle(style2);
		HSSFCell titleCell4 = titlerRow1.createCell(4);
		titleCell4.setCellValue("分部");//工号
		titleCell4.setCellStyle(style2);
		HSSFCell titleCell5 = titlerRow1.createCell(5);
		titleCell5.setCellValue("当前班别");//姓名
		titleCell5.setCellStyle(style2);
		// 4.遍历数据,创建数据行
		if(0 != correspondingrelationList.size()) {
			for (Correspondingrelation infoOne : correspondingrelationList) { // 获取最后一行的行号
				int lastRowNum = sheet.getLastRowNum();
				HSSFRow dataRow = sheet.createRow(lastRowNum + 1);
				
				dataRow.createCell(0).setCellValue(infoOne.getCarNo());
				dataRow.createCell(1).setCellValue(infoOne.getJobNo());
				dataRow.createCell(2).setCellValue(infoOne.getEmpName());
				dataRow.createCell(3).setCellValue(infoOne.getDept());
				dataRow.createCell(4).setCellValue(infoOne.getParcel());
				dataRow.createCell(5).setCellValue(infoOne.getCurrentClassType());
			}
		}
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			hssfWorkbook.write(fileOut);
			fileOut.close();
			JOptionPane.showMessageDialog(null, "已经将 " + fileName + " 导出到您的电脑桌面.");
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "导出信息失败。");
		}
	
		
	}
}
