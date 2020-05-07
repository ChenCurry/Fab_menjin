package test.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;
import test.domain.Correspondingrelation;
import test.domain.FromExcel;

public class ImportExcel {
	private UserDao userDao=new UserDaoImpl();
	
	/**
	 * https://www.cnblogs.com/sprinkle/p/6426204.html
	 * @param args
	 */
	
	public List<FromExcel> toFromExcel(){
		String path = "C:/Users/yu.chen/Desktop/fromExcel.xlsx";
		List<List<String>> lists = readExcel(path);
        List<FromExcel> fromExcelList = new ArrayList<FromExcel>();
        FromExcel fromExcel = null;
        List<String> listx = null;
        for (int i=1;i<lists.size();i++) {
        	fromExcel = new FromExcel();
        	listx = new ArrayList<String>();
        	listx = lists.get(i);
        	if(listx.get(0).length()==8) {
        		fromExcel.setCarNo("00"+listx.get(0));
        	}else if(listx.get(0).length()==9) {
        		fromExcel.setCarNo("0"+listx.get(0));
        	}else {
        		fromExcel.setCarNo(listx.get(0));
        	}
        	fromExcel.setJobNo(listx.get(1));
        	fromExcel.setEmpName(listx.get(2));
        	fromExcel.setDept(listx.get(3));
        	fromExcelList.add(fromExcel);
        }
		return fromExcelList;
	}
	
	@SuppressWarnings("resource")
	public static List<List<String>> readExcel(String path) {
        String fileType = path.substring(path.lastIndexOf(".") + 1);
        // return a list contains many list
        List<List<String>> lists = new ArrayList<List<String>>();
        //读取excel文件
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            //获取工作薄
            Workbook wb = null;
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
            } else {
                return null;
            }

            //读取第一个工作页sheet
            Sheet sheet = wb.getSheetAt(0);
            //第一行为标题
            for (Row row : sheet) {
                ArrayList<String> list = new ArrayList<String>();
                for (Cell cell : row) {
                    //根据不同类型转化成字符串
                    cell.setCellType(CellType.STRING);
                    list.add(cell.getStringCellValue());
                }
                lists.add(list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lists;
    }
	
	/**
	 * 页面导入excel
	 * @param path
	 * @return
	 * @throws SQLException 
	 */
	@SuppressWarnings("resource")
	public List<String> frameImportExcel(File fis) throws SQLException {//FileInputStream
        String fileType = fis.getName().substring(fis.getName().lastIndexOf(".") + 1);
        // return a list contains many list
        List<List<String>> lists = new ArrayList<List<String>>();
        //读取excel文件
        InputStream is = null;
        //获取工作薄
        Workbook wb = null;
        try {
            is = new FileInputStream(fis);
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
            } else {
                return null;
            }

            //读取第一个工作页sheet
            Sheet sheet = wb.getSheetAt(0);
            //第一行为标题
            for (int i=1;i<=sheet.getLastRowNum();i++) {//第0行是表头
                ArrayList<String> list = new ArrayList<String>();
                if(null!=sheet.getRow(i)&&null!=sheet.getRow(i).getCell(0)) {
                	Cell cell = sheet.getRow(i).getCell(0);
                	cell.setCellType(CellType.STRING);
                	if("".equals(cell.getStringCellValue())) {
                		//System.out.println("zheli应该打印很多行");
                		continue;
                	}
                    list.add(cell.getStringCellValue());//工号
                    
                    cell = sheet.getRow(i).getCell(1);
                    cell.setCellType(CellType.STRING);
                    list.add(cell.getStringCellValue());//姓名
                    
                    cell = sheet.getRow(i).getCell(2);
                    cell.setCellType(CellType.STRING);
                    list.add(cell.getStringCellValue());//卡号
                    
                    cell = sheet.getRow(i).getCell(3);//部门
                    if(null!=cell) {
                    	cell.setCellType(CellType.STRING);
                    	list.add(cell.getStringCellValue());
                    }else {
                    	list.add("");
                    }
                    
                    lists.add(list);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (wb != null) wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        List<FromExcel> fromExcelList = new ArrayList<FromExcel>();
        FromExcel fromExcel = null;
        List<String> listx = null;
        for (int i=0;i<lists.size();i++) {
        	fromExcel = new FromExcel();
        	listx = new ArrayList<String>();
        	listx = lists.get(i);
        	if(null!=listx.get(2)&&!"".equals(listx.get(2))) {
//        		if(listx.get(2).length()==8) {
//            		fromExcel.setCarNo("00"+listx.get(2));
//            	}else if(listx.get(0).length()==9) {
//            		fromExcel.setCarNo("0"+listx.get(2));
//            	}else {
//            		fromExcel.setCarNo(listx.get(2));
//            	}
        		fromExcel.setCarNo(listx.get(2));
            	fromExcel.setJobNo(listx.get(0));
            	fromExcel.setDept(listx.get(3));
            	fromExcel.setEmpName(listx.get(1));
            	fromExcelList.add(fromExcel);
        	}
        }
        
        List<String> listStr = new ArrayList<String>();
        if(fromExcelList.size()>0) {
        	//y = testDao.insertRelation(fromExcelList);
        	listStr = userDao.insertRelation(fromExcelList);
        }
		return listStr;
    }
	
	/**
	 * 班别配置页面  班别导入
	 * @param fis
	 * @return
	 * @throws SQLException
	 */
	@SuppressWarnings("static-access")
	public List<String> f_classTypeImportExcel(File fis) throws SQLException {//FileInputStream
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
		
        String fileType = fis.getName().substring(fis.getName().lastIndexOf(".") + 1);
        // return a list contains many list
        List<List<String>> lists = new ArrayList<List<String>>();
        //读取excel文件
        InputStream is = null;
        //获取工作薄
        Workbook wb = null;
        try {
            is = new FileInputStream(fis);
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (fileType.equals("xlsx")) {
                wb = new XSSFWorkbook(is);
            } else {
                return null;
            }

            //读取第一个工作页sheet
            Sheet sheet = wb.getSheetAt(0);
            //第一行为标题
            Cell cell = null;
            for (int i=2;i<=sheet.getLastRowNum();i++) {
                ArrayList<String> list = new ArrayList<String>();
                if(null!=sheet.getRow(i)&&null!=sheet.getRow(i).getCell(0)) {
                	cell = sheet.getRow(i).getCell(0);
                	cell.setCellType(CellType.STRING);
                	if("".equals(cell.getStringCellValue())) {
                		//System.out.println("zheli应该打印很多行");
                		continue;
                	}
                    list.add(cell.getStringCellValue().trim());//0
                    
                    cell = sheet.getRow(i).getCell(1);
                    if(null!=cell) {
                    	cell.setCellType(CellType.STRING);
                    }else {
                    	continue;
                    }
                    list.add(cell.getStringCellValue().trim());//1
                    
                    cell = sheet.getRow(i).getCell(4);//分部
                    if(null!=cell) {
                    	cell.setCellType(CellType.STRING);
                    	list.add(cell.getStringCellValue().trim());//2
                    }else {
                    	list.add("");
                    }
                    
                    cell = sheet.getRow(i).getCell(5);//班别
                    if(null!=cell) {
                    	cell.setCellType(CellType.STRING);
                    	list.add(cell.getStringCellValue().trim());//3
                    }else {
                    	list.add("");
                    }
                    
                    lists.add(list);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (wb != null) wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> listStr = new ArrayList<String>();//返回前台
        List<Correspondingrelation> fromExcelList = new ArrayList<Correspondingrelation>();
        Correspondingrelation fromExcel = null;
        List<String> listx = null;
        for (int i=0;i<lists.size();i++) {
        	fromExcel = new Correspondingrelation();
        	listx = new ArrayList<String>();
        	listx = lists.get(i);
        	if(null!=listx.get(1)&&!"".equals(listx.get(1))) {
            	fromExcel.setCarNo(listx.get(0));
            	fromExcel.setJobNo(listx.get(1));
            	fromExcel.setParcel(listx.get(2));
            	String ct = listx.get(3);
            	if(!ct.equals("NA")&&!ct.equals("NB")&&!ct.equals("DA")&&!ct.equals("DB")&&!ct.equals("NOR")&&!ct.equals("")) {
            		//如果格式不对
            		listStr.add("error0");
            		listStr.add((i+1)+"");
            		return listStr;
            	}
            	fromExcel.setCurrentClassType(ct);
            	fromExcelList.add(fromExcel);
        	}
        }
        
        
        if(fromExcelList.size()>0) {
        	listStr = userDao.insertCurrentClassTypeInfo(fromExcelList,time1);
        }
		return listStr;
    }
}
