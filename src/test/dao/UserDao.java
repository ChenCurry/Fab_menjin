package test.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import test.domain.AlarmInfo;
import test.domain.Correspondingrelation;
import test.domain.CurrentInfo;
import test.domain.FromExcel;
import test.domain.InoutRecord;

public interface UserDao {

	/**
	 * 用以 导入excel中的关系对应信息
	 * 
	 * @param listRelation
	 * @return
	 * @throws SQLException
	 */
	public List<String> insertRelation(List<FromExcel> listRelation) throws SQLException;

	/**
	 * 持久化 原始数据 并且更新 实时人员表（进入加1出去减1）
	 * 
	 * @param alarmInfo
	 * @return
	 * @throws SQLException
	 */
	public int insertInfo(AlarmInfo alarmInfo) throws SQLException;

	/**
	 * 查询关系对应表
	 * 
	 * @param carNo
	 * @return
	 * @throws SQLException
	 */
	public FromExcel queryRelation(String carNo) throws SQLException;

	/**
	 * 
	 * @param carNo
	 * @return
	 * @throws SQLException
	 */
	public int queryCurrentInfo(String carNo) throws SQLException;

	/**
	 * 用以导出 fab内所有人员 excel
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<CurrentInfo> queryCurrentInfoAll() throws SQLException;

	/**
	 * 页面刷新 fab内所有人员
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<AlarmInfo> querySummaryAll() throws SQLException;

	/**
	 * 用以页面默认查询20条进出记录
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<AlarmInfo> queryInOutRecorder() throws SQLException;

	/**
	 * 导出excel 进出记录
	 * 
	 * @param dateFrom
	 * @param dateTo
	 * @return
	 * @throws SQLException
	 */
	public List<AlarmInfo> queryInOutFromTo(String dateFrom, String dateTo) throws SQLException;

	/**
	 * 选项卡二 查询进出记录
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @return
	 * @throws SQLException
	 */
	public List<AlarmInfo> queryReportInOut(String timeFrom, String timeTo, String deptRange, String parcelRange)
			throws SQLException;

	/**
	 * 选项卡二 查询进出记录New
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 */
	public List<InoutRecord> queryReportInOutNew(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException;

	/**
	 * 选项卡二 查询Fab时数小于10小时
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReport10h(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;
	
	/**
	 * 统计白/夜班 fab时长 解决一个月内，本来上白班（夜班）的人会有几天去上了夜班（白班） 导致fab时长统计出错的问题
	 * 方法是：不再读取班别了，根据纪录直接猜出班别，再根据班别分解记录，再根据记录计算时长              2020.4.10新需求：需要能够统计常日班时长
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReport10h2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;
	
	/**
	 * 新需求  查询 导出常日班 fab时数
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReport10h2Nor(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;

	/**
	 * 选项卡二 查询迟到
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReportLater(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;
	
	/**
	 * 选项卡二 查询迟到 不分班别
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReportLater2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;

	/**
	 * 选项卡二 查询早退
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReportLeave(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;
	
	/**
	 * 选项卡二 查询早退  不论班别
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReportLeave2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;

	/**
	 * 选项卡二 查询吃饭时间大于45分钟
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReport45m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;

	/**
	 * 选项卡二 查询外出时间大于30分钟
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 * @throws SQLException
	 * @throws ParseException
	 */
	public List<InoutRecord> queryReport30m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException;

	/**
	 * 不带条件查询所有的部门下拉选项 （第一级）
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<Correspondingrelation> queryComboBoxAllDept() throws SQLException;

	/**
	 * 查询下拉框分部信息
	 * 
	 * @param dept
	 * @return
	 * @throws SQLException
	 */
	public List<Correspondingrelation> queryComboBoxParcelDept(String dept) throws SQLException;

	/**
	 * 获取员工信息到set页面
	 * 
	 * @param empDept
	 * @param empParcel
	 * @param time8
	 * @param time7
	 * @param time6
	 * @param time1
	 * @param time2
	 * @param time4
	 * @param time5
	 * @return
	 * @throws SQLException
	 */
	public List<Correspondingrelation> getDataEmpInfo(String empDept, String empParcel, String time8, String time7,
			String time6, String time1, String time2, String time4, String time5) throws SQLException;

	/**
	 * 李春瑾 要求变更后的页面
	 * 
	 * @param empDept
	 * @param empParcel
	 * @param time1
	 * @param jobNoJTF
	 * @return
	 * @throws SQLException
	 */
	public List<Correspondingrelation> getDataEmpInfo2(String empDept, String empParcel, String time1, String jobNoJTF)
			throws SQLException;

	/**
	 * 设置员工信息（分部）
	 * 
	 * @param jobNo
	 * @param aValue
	 * @return
	 * @throws SQLException
	 */
	public int updateEmpParcel(String jobNo, String aValue) throws SQLException;

	/**
	 * 设置员工信息（班别）
	 * 
	 * @param jobNo
	 * @param aValue
	 * @param time3
	 * @return
	 * @throws SQLException
	 */
	public int updateEmpClassType(String jobNo, String aValue, String time3) throws SQLException;

	/**
	 * 插入班别信息
	 * 
	 * @param jobNo
	 * @param aValue
	 * @param time3
	 * @return
	 * @throws SQLException
	 */
	public int insertEmpClassType(String jobNo, String aValue, String time3) throws SQLException;

	/**
	 * 查询班别信息
	 * 
	 * @param jobNo
	 * @param time3
	 * @return
	 * @throws SQLException
	 */
	public int queryEmpClassTypeCount(String jobNo, String time3) throws SQLException;

	/**
	 * 定时执行（在上一层指定 每月26日凌晨） 配置下月班别信息
	 * 
	 * @return
	 * @throws SQLException
	 */
	public String updateInsertClassType() throws SQLException;

	/**
	 * 
	 * @param fromExcelList
	 * @param time1
	 * @return
	 * @throws SQLException
	 */
	public List<String> insertCurrentClassTypeInfo(List<Correspondingrelation> fromExcelList, String time1)
			throws SQLException;
}
