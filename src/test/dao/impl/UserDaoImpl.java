package test.dao.impl;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import test.dao.UserDao;
import test.domain.AlarmInfo;
import test.domain.ClassType;
import test.domain.Correspondingrelation;
import test.domain.CurrentInfo;
import test.domain.FromExcel;
import test.domain.InoutRecord;
import test.util.BaseUtils;

public class UserDaoImpl implements UserDao {

	// 记住查询是BeanListHandler区别增删改的方法BeanHandler

	@Override
	public List<String> insertRelation(List<FromExcel> listRelation) throws SQLException {
		List<String> listStr = new ArrayList<String>();
		QueryRunner qr = BaseUtils.getQueryRunner();
		FromExcel fromExcel;
		int updateNo = 0;
		int newCount = 0;
		Object[][] params = new Object[listRelation.size()][];
		for (int i = 0; i < params.length; i++) {
			fromExcel = new FromExcel();
			fromExcel = listRelation.get(i);
			params[i] = new Object[] { fromExcel.getJobNo(), fromExcel.getDept(), fromExcel.getEmpName(),
					fromExcel.getCarNo() };
		}
		String sql = "update CorrespondingRelation set jobNo=?,dept=?,empName=? where carNo=?";
		int[] x = qr.batch(sql, params);
		for (int xs : x) {
			updateNo = updateNo + xs;
		}
		for (int i = 0; i < listRelation.size(); i++) {
			fromExcel = new FromExcel();
			fromExcel = listRelation.get(i);
			FromExcel fromExcel_ = queryRelation(fromExcel.getCarNo());
			if (null != fromExcel_ && null != fromExcel_.getCarNo()) {
			} else {
				String sql3 = "INSERT INTO CorrespondingRelation (carNo,jobNo,dept,empName)" + "VALUES(?,?,?,?)";
				newCount = newCount + qr.execute(sql3, fromExcel.getCarNo(), fromExcel.getJobNo(), fromExcel.getDept(),
						fromExcel.getEmpName());
			}
		}
		listStr.add(newCount + "");
		listStr.add(updateNo + "");
		return listStr;
	}

	@Override
	public int insertInfo(AlarmInfo alarmInfo) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		int rs = 0;
		String sql = "INSERT INTO AlarmInfo (adrass,carNo,dwCardReaderNo,struTime"
				+ ",cardType,dwMajor,dwMinor,dwDoorNo,dwIOTChannelNo)\r\n" + "VALUES(?,?,?,?,?,?,?,?,?)";
		rs = qr.execute(sql, alarmInfo.getAdrass(), alarmInfo.getCarNo(), alarmInfo.getDwCardReaderNo(),
				alarmInfo.getStruTime(), alarmInfo.getCardType(), alarmInfo.getDwMajor(), alarmInfo.getDwMinor(),
				alarmInfo.getDwDoorNo(), alarmInfo.getDwIOTChannelNo());
		String inOut = alarmInfo.getDwCardReaderNo();
		if ("1".equals(inOut)) {// 进的信号
			String cardNo = alarmInfo.getCarNo();
			if (queryCurrentInfo(cardNo) > 0) {// currentInfo中you记录 作更新
				String sql2 = "UPDATE currentinfo set adrass=?,dwCardReaderNo=?, struTime=?,dwDoorNo=?"
						+ ", dwIOTChannelNo=? where carNo=?";
				qr.update(sql2, alarmInfo.getAdrass(), alarmInfo.getDwCardReaderNo(), alarmInfo.getStruTime(),
						alarmInfo.getDwDoorNo(), alarmInfo.getDwIOTChannelNo(), alarmInfo.getCarNo());
			} else {// currentInfo中无记录 作insert
				FromExcel fromExcel = queryRelation(cardNo);
				if (null != fromExcel) {
					// System.out.println("往汇总表里插入："+fromExcel.toString());
					String sql2 = "INSERT INTO currentInfo (adrass,carNo,dwCardReaderNo,struTime,cardType,dwMajor"
							+ ",dwMinor,dwDoorNo,dwIOTChannelNo,jobNo,dept,empName)\r\n"
							+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
					qr.execute(sql2, alarmInfo.getAdrass(), alarmInfo.getCarNo(), alarmInfo.getDwCardReaderNo(),
							alarmInfo.getStruTime(), alarmInfo.getCardType(), alarmInfo.getDwMajor(),
							alarmInfo.getDwMinor(), alarmInfo.getDwDoorNo(), alarmInfo.getDwIOTChannelNo(),
							fromExcel.getJobNo(), fromExcel.getDept(), fromExcel.getEmpName());
				} else {
					String sql2 = "INSERT INTO currentInfo (adrass,carNo,dwCardReaderNo,struTime,cardType,dwMajor"
							+ ",dwMinor,dwDoorNo,dwIOTChannelNo,jobNo,dept,empName)\r\n"
							+ "VALUES(?,?,?,?,?,?,?,?,?,'','','')";
					qr.execute(sql2, alarmInfo.getAdrass(), alarmInfo.getCarNo(), alarmInfo.getDwCardReaderNo(),
							alarmInfo.getStruTime(), alarmInfo.getCardType(), alarmInfo.getDwMajor(),
							alarmInfo.getDwMinor(), alarmInfo.getDwDoorNo(), alarmInfo.getDwIOTChannelNo());
				}
			}

			// 插入新记录到inoutrecord 需要先执行一条 状态：0进入1正常出门3进异常4出异常
			String sql4 = "update inoutrecord set state=3 where carNo=? and state=0 and date_format(inTime,'%Y-%c-%d %H:%i:%s')>date_sub(now(),interval 1 day)";
			rs = qr.execute(sql4, alarmInfo.getCarNo());

			String sql3 = "insert into inoutrecord (adrass,carNo,inTime,outTime,state) values (?,?,?,'',0)";// 0表示进入
																											// 3表示进入未出的异常
			rs = qr.execute(sql3, alarmInfo.getAdrass(), alarmInfo.getCarNo(), alarmInfo.getStruTime());
		} else if ("4".equals(inOut)) {// 出的信号
			String sql2 = "DELETE from currentInfo where carNo=?";
			qr.execute(sql2, alarmInfo.getCarNo());

			// 更新inoutrecord（更新过去一天24h以来，状态为0的记录；） 情况：1一条进的记录2多条进的记录3没有进的记录
			String sql3 = "update inoutrecord set outTime=?,state=1 \r\n"
					+ "where carNo=? and state=0 and date_format(inTime,'%Y-%c-%d %H:%i:%s')>date_sub(now(),interval 1 day)";
			int if0 = qr.execute(sql3, alarmInfo.getStruTime(), alarmInfo.getCarNo());// 这里需要检查
			// 执行之后 看能否获取到update的条数
			// 如果可以，条数为1没问题；条数为0 那么
			if (0 == if0) {
				String sql4 = "insert into inoutrecord (adrass,carNo,inTime,outTime,state) values (?,?,'',?,4)";// 4表示只有出的异常
				qr.execute(sql4, alarmInfo.getAdrass(), alarmInfo.getCarNo(), alarmInfo.getStruTime());
			}
		}
		return rs;
	}

	@Override
	public FromExcel queryRelation(String carNo) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		// select carNo,jobNo,empName,dept from CorrespondingRelation where carNo like
		// '%28230857%';
		String sql = "select  carNo,jobNo,empName,dept  from  CorrespondingRelation where carNo = ?";
		List<FromExcel> list = qr.query(sql, new BeanListHandler<FromExcel>(FromExcel.class), carNo);
		return (null != list && list.size() != 0) ? list.get(0) : null;
	}

	@Override
	public int queryCurrentInfo(String carNo) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		// select carNo,jobNo,empName,dept from CorrespondingRelation where carNo like
		// '%28230857%';
		String sql = "select  carNo  from  currentInfo where carNo = ?";
		List<CurrentInfo> list = qr.query(sql, new BeanListHandler<CurrentInfo>(CurrentInfo.class), carNo);
		return list.size();
	}

	@Override
	public List<CurrentInfo> queryCurrentInfoAll() throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<CurrentInfo> currentInfoList = new ArrayList<CurrentInfo>();
		String sql = "select adrass,carNo,dwCardReaderNo,struTime,cardType,dwMajor,dwMinor,dwDoorNo,dwIOTChannelNo,jobNo,empName,dept\r\n"
				+ "from currentInfo where 1=1 ORDER BY id_current DESC";
		currentInfoList = qr.query(sql, new BeanListHandler<CurrentInfo>(CurrentInfo.class));
		return currentInfoList;
	}

	@Override
	public List<AlarmInfo> querySummaryAll() throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
		String sql = "select adrass,carNo,dwCardReaderNo,struTime,cardType,dwMajor,dwMinor,dwDoorNo,dwIOTChannelNo,jobNo,empName,dept\r\n"
				+ "from currentInfo where 1=1 ORDER BY id_current DESC";
		alarmInfoList = qr.query(sql, new BeanListHandler<AlarmInfo>(AlarmInfo.class));
		return alarmInfoList;
	}

	@Override
	public List<AlarmInfo> queryInOutRecorder() throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
		String sql = "select a.adrass,a.carNo,a.dwCardReaderNo,a.struTime,a.cardType,a.dwMajor\r\n"
				+ ",a.dwMinor,a.dwDoorNo,a.dwIOTChannelNo,b.jobNo,b.dept,b.empName\r\n"
				+ "from alarminfo a left join CorrespondingRelation b on a.carNo=b.carNo "
				+ " ORDER BY a.id_A DESC LIMIT 20";
		alarmInfoList = qr.query(sql, new BeanListHandler<AlarmInfo>(AlarmInfo.class));
		return alarmInfoList;
	}

	@Override
	public List<AlarmInfo> queryInOutFromTo(String dateFrom, String dateTo) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
		String sql = "select a.adrass,a.carNo,a.dwCardReaderNo,a.struTime,a.cardType,a.dwMajor\r\n"
				+ ",a.dwMinor,a.dwDoorNo,a.dwIOTChannelNo,b.jobNo,b.dept,b.empName\r\n"
				+ "from alarminfo a left join CorrespondingRelation b on a.carNo=b.carNo "
				+ "where date_format(a.struTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00') "
				+ "AND date_format(?,'%Y-%c-%d 23:59:59')" + " ORDER BY a.id_A DESC";
		alarmInfoList = qr.query(sql, new BeanListHandler<AlarmInfo>(AlarmInfo.class), dateFrom, dateTo);
		return alarmInfoList;
	}

	@Override
	public List<AlarmInfo> queryReportInOut(String timeFrom, String timeTo, String deptRange, String parcelRange)
			throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<AlarmInfo> alarmInfoList = new ArrayList<AlarmInfo>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		String sql = "select a.adrass,a.carNo,a.dwCardReaderNo,a.struTime,a.cardType,a.dwMajor"
				+ ",a.dwMinor,a.dwDoorNo,a.dwIOTChannelNo,b.jobNo,b.dept,b.empName,b.parcel\r\n"
				+ "from alarminfo a, CorrespondingRelation b\r\n"
				+ "where date_format(a.struTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?"
				+ ",'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59')\r\n" + "and a.carNo=b.carNo\r\n";
		if (!"all fab".equals(deptRange)) {
			sql += "and b.dept=?\r\n";
		} else {
			sql += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql += "and 'all'=?\r\n";
		} else {
			sql += "and b.parcel=?\r\n";
		}
		sql += "ORDER BY a.struTime DESC";
		alarmInfoList = qr.query(sql, new BeanListHandler<AlarmInfo>(AlarmInfo.class), timeFrom, timeTo, deptRange,
				parcelRange);
		return alarmInfoList;
	}

	@Override
	public List<InoutRecord> queryReportInOutNew(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		String sql = "select a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.dept,b.empName,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a, CorrespondingRelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?"
				+ ",'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') " + "or"
				+ " date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')"
				+ " AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n";
		if (!"all fab".equals(deptRange)) {
			sql += "and b.dept=?\r\n";
		} else {
			sql += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql += "and 'all'=?\r\n";
		} else {
			sql += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql += "ORDER BY a.id_R DESC";
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		return inoutRecordList;
	}

	@Override
	public List<InoutRecord> queryReport10h(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		/**
		 * 这里边需要做的是：针对限定条件下已有记录的统计 限定条件：时间（请选择某月26日至次月25日） 前提：请要查询人员的班别已经配置 功能：
		 * 查出所有NOR记录，统计总时长小于10h的记录； 对于N班的统计当日14：00到次日14：00的总时长小于10h的记录；
		 * 对于D班的统计当日02：00到次日02：00的总时长小于10h的记录；
		 */
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		String day = "";
		int minutes = 0;
		String cardNo = "";
		List<Integer> record = new ArrayList<Integer>();
		DecimalFormat df=new DecimalFormat("0.0");//设置保留位数
		 

		// 夜班：时间段内 所有人 找出小于10h的日期，并把卡号，日期，时长，班别返回
		// 对于N班的统计当日14：00到次日14：00的总时长小于10h的记录；
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		int size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date1 = new Date();
			Date date2 = new Date();
			if (4 != state) {
				date1 = dft2.parse(inTim);
			}
			if (!"".equals(outTim)) {
				date2 = dft2.parse(outTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim;
				} else {
					day = outTim;
				}
				if (1 == state) {
					long from3 = date1.getTime();
					long to3 = date2.getTime();
					minutes = (int) ((to3 - from3) / (1000 * 60));
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim;
					} else {
						day_ = outTim;
					}
					if (N_is_sameday(day, day_)) {// 说明是同一天
						if (1 == state) {
							long from3 = date1.getTime();
							long to3 = date2.getTime();
							minutes = minutes + (int) ((to3 - from3) / (1000 * 60));
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (true) {// 小于10h  minutes < 595
							InoutRecord inoutRecord_ = new InoutRecord();
//							for (int j = 0; j < record.size(); j++) {
//								inoutRecord_ = inoutRecordList.get(record.get(j));
//								inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//								inoutRecordList2.add(inoutRecord_);
//							}
							if (0 != record.size()) {
								inoutRecord_ = inoutRecordList.get(record.get(0));
								//       minutes / 60 + "h" + minutes % 60 + "min"
								inoutRecord_.setSpareField(df.format((float)minutes/60));
								inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
								inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
								inoutRecordList2.add(inoutRecord_);
							}
						}
						record.clear();
						minutes = 0;
						if (1 == state) {
							long from3 = date1.getTime();
							long to3 = date2.getTime();
							minutes = minutes + (int) ((to3 - from3) / (1000 * 60));
						}
					}
					day = day_;
				} else {// 换人了，相当于第一次开始
					if (true) {// 小于10h  minutes < 595
						InoutRecord inoutRecord_ = new InoutRecord();
//						for (int j = 0; j < record.size(); j++) {
//							inoutRecord_ = inoutRecordList.get(record.get(j));
//							inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//							inoutRecordList2.add(inoutRecord_);
//						}
						if (0 != record.size()) {
							inoutRecord_ = inoutRecordList.get(record.get(0));
							inoutRecord_.setSpareField(df.format((float)minutes/60));
							inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
							inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
							inoutRecordList2.add(inoutRecord_);
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					minutes = 0;
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim;
					} else {
						day = outTim;
					}
					if (1 == state) {
						long from3 = date1.getTime();
						long to3 = date2.getTime();
						minutes = (int) ((to3 - from3) / (1000 * 60));
					}
				}
			}
			record.add(i);
		}
		if (true) {// 循环结束 收尾工作  minutes < 595
			InoutRecord inoutRecord_ = new InoutRecord();
//			for (int j = 0; j < record.size(); j++) {
//				inoutRecord_ = inoutRecordList.get(record.get(j));
//				inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//				inoutRecordList2.add(inoutRecord_);
//			}
			if (0 != record.size()) {
				inoutRecord_ = inoutRecordList.get(record.get(0));
				inoutRecord_.setSpareField(df.format((float)minutes/60));
				inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
				inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
				inoutRecordList2.add(inoutRecord_);
			}
		}
		record.clear();
		minutes = 0;
		cardNo = "";
		day = "";

		// 白班：时间段内 所有人 找出小于10h的日期，并把卡号，日期，时长，班别返回
		// * 对于D班的统计当日02：00到次日02：00的总时长小于10h的记录；
		String sql3 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql3 += "and b.dept=?\r\n";
		} else {
			sql3 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql3 += "and 'all'=?\r\n";
		} else {
			sql3 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql3 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql3 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date1 = new Date();
			Date date2 = new Date();
			if (4 != state) {
				date1 = dft2.parse(inTim);
			}
			if (!"".equals(outTim)) {
				date2 = dft2.parse(outTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim.substring(0, 10);
				} else {
					day = outTim.substring(0, 10);
				}
				if (1 == state) {
					long from3 = date1.getTime();
					long to3 = date2.getTime();
					minutes = (int) ((to3 - from3) / (1000 * 60));
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim.substring(0, 10);
					} else {
						day_ = outTim.substring(0, 10);
					}
					// day_ 跟day天的次日的14点相比
					if (day.equals(day_)) {// 说明是同一天
						if (1 == state) {
							long from3 = date1.getTime();
							long to3 = date2.getTime();
							minutes = minutes + (int) ((to3 - from3) / (1000 * 60));
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (true) {// 小于10h  minutes < 595
							InoutRecord inoutRecord_ = new InoutRecord();
//							for (int j = 0; j < record.size(); j++) {
//								inoutRecord_ = inoutRecordList.get(record.get(j));
//								inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//								inoutRecordList2.add(inoutRecord_);
//							}
							if (0 != record.size()) {
								inoutRecord_ = inoutRecordList.get(record.get(0));
								inoutRecord_.setSpareField(df.format((float)minutes/60));
								inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
								inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
								inoutRecordList2.add(inoutRecord_);
							}
						}
						record.clear();
						minutes = 0;
						if (1 == state) {
							long from3 = date1.getTime();
							long to3 = date2.getTime();
							minutes = minutes + (int) ((to3 - from3) / (1000 * 60));
						}
					}
					day = day_;
				} else {// 换人了，相当于第一次开始
					if (true) {// 小于10h  minutes < 595
						InoutRecord inoutRecord_ = new InoutRecord();
//						for (int j = 0; j < record.size(); j++) {
//							inoutRecord_ = inoutRecordList.get(record.get(j));
//							inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//							inoutRecordList2.add(inoutRecord_);
//						}
						if (0 != record.size()) {
							inoutRecord_ = inoutRecordList.get(record.get(0));
							inoutRecord_.setSpareField(df.format((float)minutes/60));
							inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
							inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
							inoutRecordList2.add(inoutRecord_);
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					minutes = 0;
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim.substring(0, 10);
					} else {
						day = outTim.substring(0, 10);
					}
					if (1 == state) {
						long from3 = date1.getTime();
						long to3 = date2.getTime();
						minutes = (int) ((to3 - from3) / (1000 * 60));
					}
				}
			}
			record.add(i);
		}
		if (true) {// 循环结束 收尾工作  minutes < 595
			InoutRecord inoutRecord_ = new InoutRecord();
//			for (int j = 0; j < record.size(); j++) {
//				inoutRecord_ = inoutRecordList.get(record.get(j));
//				inoutRecord_.setSpareField(minutes / 60 + "h");// 待确认
//				inoutRecordList2.add(inoutRecord_);
//			}
			if (0 != record.size()) {
				inoutRecord_ = inoutRecordList.get(record.get(0));
				inoutRecord_.setSpareField(df.format((float)minutes/60));
				inoutRecord_.setOutTime(inoutRecordList.get(record.get(record.size() - 1)).getOutTime());
				inoutRecord_.setInOutCount((record.size()+1)/2);//进出次数
				inoutRecordList2.add(inoutRecord_);
			}
		}
		record.clear();
		minutes = 0;
		cardNo = "";
		day = "";
		return inoutRecordList2;
	}
	
	@Override
	public List<InoutRecord> queryReport10h2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		timeFrom = timeFrom.replace('-', '/');
		timeTo = timeTo.replace('-', '/');
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		 
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 13:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 13:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB' OR c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		//inoutRecordList = new ArrayList<InoutRecord>();
		timeTo = nextDay(timeTo);
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		List<InoutRecord> tempList = new ArrayList<InoutRecord>();
		List<List<InoutRecord>> inoutRecordListList = new ArrayList<List<InoutRecord>>();
		int size = inoutRecordList.size();
		String carNo = "";
		for (int i=0; i<size; i++) {
			if(""!=carNo) {
				if(inoutRecordList.get(i).getCarNo().equals(carNo)) {
					tempList.add(inoutRecordList.get(i));
				} else {
					inoutRecordListList.add(tempList);
					tempList = new ArrayList<InoutRecord>(); 
					tempList.add(inoutRecordList.get(i));
					carNo = inoutRecordList.get(i).getCarNo();
				}
			} else {//第一次
				carNo = inoutRecordList.get(i).getCarNo();
				tempList.add(inoutRecordList.get(i));
			}
		}
		inoutRecordListList.add(tempList);//到此，inoutRecordListList中的每一项就是一个人的所有记录
		
		/**
		 * 总结出这个问题是：时间段内的统计应该这么来：
		 * 起始日期的白班开始，到截至日期的夜班结束（夜班结束的时间应该是截至日期的次日14/12点）
		 * 所以：
		 * 起始日期前一天的夜班出入记录要去掉
		 * 截止日期次日的白班/夜班的记录都要去掉（只留次日前一日的夜班！！）
		 * 这样，在此基础上再做统计，数据才会准确！
		 * 现在开始去掉头和尾的数据！！！！！！！
		 * 我的sql中有个or，是个麻烦的点
		 */
		
		List<List<Integer>> allSite = new ArrayList<List<Integer>>();
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			List<Integer> each26site = new ArrayList<Integer>();
			eachList = inoutRecordListList.get(i);
			boolean f2 = false;//截至日期的后一天上午有记录的
			int m1 = 0;
			int m2 = 0;
			for(int j=0;j<eachList.size();j++) {//对每一个人的记录进行处理
				//需要记录是按时间顺序排列的   待确认
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//if里边应该是一个比较值，试想早上下班回家，晚上来上班，就有问题
				if((notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)
						||(notEmpty(each.getOutTime())?(dft2.parse(each.getOutTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)) {
					m1 = j-1;
					break;
				}
			}
			each26site.add(m1);
			for(int j=0;j<eachList.size();j++) {
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false 不用如上一样处理
				if((notEmpty(each.getInTime())?(each.getInTime().substring(0, 10).equals(timeTo)):false)
						||(notEmpty(each.getOutTime())?(each.getOutTime().substring(0, 10).equals(timeTo)):false)) {
					f2 = true;
					m2 = j;
					break;
				}
			}
			if(f2) {
				each26site.add(m2);
			} else {
				each26site.add(-1);
			}
			allSite.add(each26site);//allSite.get(X).get(0)是头部、allSite.get(X).get(1)是尾部
		}
		//到此，allSite中已经包含了每一个人的所有记录中 可能需要去掉的起始日期之前的夜班和截至日期的次日的非前一日夜班 所在的节点位置
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			eachList = inoutRecordListList.get(i);
			int site2 = allSite.get(i).get(1).intValue();
			if(-1 != site2) {
				int lengthChange = eachList.size();
				if(((notEmpty(eachList.get(site2).getInTime())?(dft2.parse(eachList.get(site2).getInTime()).before(dft2.parse(timeTo+" 06:00:00"))):false)
						||(notEmpty(eachList.get(site2).getOutTime())?(dft2.parse(eachList.get(site2).getOutTime()).before(dft2.parse(timeTo+" 06:00:00"))):false))
						&&(true)) {
					/**
					 * (notEmpty(eachList.get(eachList.size()-1).getOutTime())?(dft2.parse(eachList.get(eachList.size()-1).getOutTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
								||(notEmpty(eachList.get(eachList.size()-1).getInTime())?(dft2.parse(eachList.get(eachList.size()-1).getInTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
					 */
					//表示的确是上一天的夜班，需要统计进去
				} else {
					//是次日的班了，不属于上一天的夜班
					for(int j=lengthChange-1;j>=site2;j--) {
						eachList.remove(j);//
					}
				}
			}
			//此时，eachList的尾巴已经可能被剪掉一截了，现在对其头部进行操作
			int site1 = allSite.get(i).get(0).intValue();
			if(-1 != site1) {
				if(((notEmpty(eachList.get(0).getInTime())?(dft2.parse(eachList.get(0).getInTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false)
						||(notEmpty(eachList.get(0).getOutTime())?(dft2.parse(eachList.get(0).getOutTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false))
						&&((notEmpty(eachList.get(site1).getOutTime())?(dft2.parse(eachList.get(site1).getOutTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false)
								||(notEmpty(eachList.get(site1).getInTime())?(dft2.parse(eachList.get(site1).getInTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false))) {
					//早6点以前有进，在10点以前回家了，则为25号的夜班
					for(int j=0;j<=site1;j++) {
						eachList.remove(0);
					}
				}
			}
			//至此，eachList的记录已经是干净的了（尾巴可能没干净,可能性几乎为0，没关系，后面有容错）
			//下面eachList是该员很多天的很多进出记录     还在大的循环体内
			Integer startSite = null;//包含
			Integer endSite = null;//不包含
			for(int j=0;j<eachList.size();j++) {
				InoutRecord inoutRecord = new InoutRecord();
				inoutRecord = eachList.get(j);
				/**
				 * 难点在于如何识别哪些记录是什么班别：
				 * 这样来：因为需要统计的员工只有两种、而且很单纯：白班和夜班
				 * 那么每次只需要抓取开始的日期  因为记录是干净的
				 * 如果第一条进入时间是白天06（夜班可能）点到16点   就可以确定是白班
				 * 如果第一条进入时间是19点到23.59.59   可确定是夜班   
				 * 
				 * 程序模拟大脑分析班别的过程！
				 */
				boolean dayNight = true;//true：白天    false：夜晚
				if(0==j) {
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							
							if (((notEmpty(inoutRecord2.getInTime())
									? (dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 00:00:01")))
									: false)
									|| (notEmpty(inoutRecord2.getOutTime())
											? (dft2.parse(inoutRecord2.getOutTime())
													.after(dft2.parse(thisDay + " 00:00:01")))
											: false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						inoutRecordList2.add(calculationFabTime(eachList, startSite, endSite, dayNight));
					}else {
						//夜班
						dayNight = false;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);

							if (((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 14:00:01"))): false)
									|| (notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay + " 14:00:01"))):false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						inoutRecordList2.add(calculationFabTime(eachList, startSite, endSite, dayNight));
					}
				}
				if(j==endSite) {//&&endSite!=eachList.size()不会
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						inoutRecordList2.add(calculationFabTime(eachList, startSite, endSite, dayNight));
					}else {
						//夜班
						dayNight = false;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						inoutRecordList2.add(calculationFabTime(eachList, startSite, endSite, dayNight));
					}
				}
			}
		}
		return inoutRecordList2;
	}
	
	@Override
	public List<InoutRecord> queryReport10h2Nor(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		timeFrom = timeFrom.replace('-', '/');
		timeTo = timeTo.replace('-', '/');
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		 
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and c.classType='NOR'\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		//timeTo = nextDay(timeTo);
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		List<InoutRecord> tempList = new ArrayList<InoutRecord>();
		List<List<InoutRecord>> inoutRecordListList = new ArrayList<List<InoutRecord>>();
		int size = inoutRecordList.size();
		String carNo = "";
		for (int i=0; i<size; i++) {
			if(""!=carNo) {
				if(inoutRecordList.get(i).getCarNo().equals(carNo)) {
					tempList.add(inoutRecordList.get(i));
				} else {
					inoutRecordListList.add(tempList);
					tempList = new ArrayList<InoutRecord>(); 
					tempList.add(inoutRecordList.get(i));
					carNo = inoutRecordList.get(i).getCarNo();
				}
			} else {//第一次
				carNo = inoutRecordList.get(i).getCarNo();
				tempList.add(inoutRecordList.get(i));
			}
		}
		inoutRecordListList.add(tempList);//到此，inoutRecordListList中的每一项就是一个人的所有记录
		
		//到此，allSite中已经包含了每一个人的所有记录中 可能需要去掉的起始日期之前的夜班和截至日期的次日的非前一日夜班 所在的节点位置
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			eachList = inoutRecordListList.get(i);
			//至此，eachList的记录已经是干净的了（尾巴可能没干净,可能性几乎为0，没关系，后面有容错）
			//下面eachList是该员很多天的很多进出记录     还在大的循环体内
			Integer startSite = null;//包含
			Integer endSite = null;//不包含
			for(int j=0;j<eachList.size();j++) {
				InoutRecord inoutRecord = new InoutRecord();
				inoutRecord = eachList.get(j);
				if(0==j) {
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					//说明是白班
					//dayNight = true;
					startSite = 0;//包含
					endSite = null;//不包含
					boolean yijinggetstart = false;//只会取符合条件的第一个值
					for(int k=0;k<eachList.size();k++) {
						InoutRecord inoutRecord2 = new InoutRecord();
						inoutRecord2 = eachList.get(k);
						
						if (((notEmpty(inoutRecord2.getInTime())
								? (dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 00:00:01")))
								: false)
								|| (notEmpty(inoutRecord2.getOutTime())
										? (dft2.parse(inoutRecord2.getOutTime())
												.after(dft2.parse(thisDay + " 00:00:01")))
										: false))
								&& !yijinggetstart) {
							startSite = k;
							yijinggetstart = true;
						}

						if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
								||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
							endSite = k;
							break;
						}
					}
					if(null==endSite) {
						endSite = eachList.size();
					}
					inoutRecordList2.add(calculationFabTimeNor(eachList, startSite, endSite));
				}
				if(j==endSite) {
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					//说明是白班
					//dayNight = true;
					startSite = endSite;//包含
					endSite = null;//不包含
					for(int k=0;k<eachList.size();k++) {
						InoutRecord inoutRecord2 = new InoutRecord();
						inoutRecord2 = eachList.get(k);
						if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
								||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
							endSite = k;
							break;
						}
					}
					if(null==endSite) {
						endSite = eachList.size();
					}
					inoutRecordList2.add(calculationFabTimeNor(eachList, startSite, endSite));
				}
			}
		}
		return inoutRecordList2;
	}
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean notEmpty(String str) {
		if(null!=str&&!str.trim().equals("")) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * 获取下一个日期的字符串
	 * @param date
	 * @return
	 */
	public static String nextDay(String date) {
		date = date.replace('-', '/');
		@SuppressWarnings("deprecation")
		Date dateTimeTo = new Date(date);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(dateTimeTo);
		calendar.add(Calendar.DATE, +1);
		dateTimeTo = calendar.getTime();
		SimpleDateFormat dft = new SimpleDateFormat("yyyy/MM/dd");// HH:mm:ss
		return dft.format(dateTimeTo);
	}
	
	/**
	 * 具体计算Fab时长的方法  白班夜班
	 * @param eachList
	 * @param startSite
	 * @param endSite
	 * @param dayNight
	 * @return
	 * @throws ParseException
	 */
	public static InoutRecord calculationFabTime(List<InoutRecord> eachList, Integer startSite,Integer endSite,boolean dayNight) throws ParseException {
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("0.0");//设置保留位数
		//通过位置进行计算
		long cumulative = 0;
		for(int k=startSite;k<endSite;k++) {
			InoutRecord inoutRecord2 = new InoutRecord();
			inoutRecord2 = eachList.get(k);
			if(1 != inoutRecord2.getState()) {
				continue;
			}
			long intime = dft2.parse(inoutRecord2.getInTime()).getTime();
			long outtime = dft2.parse(inoutRecord2.getOutTime()).getTime();
			cumulative += (outtime - intime);
		}
		//返回的值
		InoutRecord inoutRecord3 = new InoutRecord();
		inoutRecord3 = eachList.get(startSite);
		inoutRecord3.setOutTime(eachList.get(endSite-1).getOutTime());//最后出门时间
		String fabTime = "";
		if(dayNight) {
			fabTime = df.format(((float)cumulative/(float)(1000*60*60)))+"h day";
		}else {
			fabTime = df.format(((float)cumulative/(float)(1000*60*60)))+"h night";
		}
		inoutRecord3.setSpareField(fabTime);//fab时长
		inoutRecord3.setInOutCount((endSite-startSite));//进出次数
		return inoutRecord3;
	}
	
	/**
	 * 常日班计算fab时长
	 * @param eachList
	 * @param startSite
	 * @param endSite
	 * @param dayNight
	 * @return
	 * @throws ParseException
	 */
	public static InoutRecord calculationFabTimeNor(List<InoutRecord> eachList, Integer startSite,Integer endSite) throws ParseException {
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("0.0");//设置保留位数
		//通过位置进行计算
		long cumulative = 0;
		for(int k=startSite;k<endSite;k++) {
			InoutRecord inoutRecord2 = new InoutRecord();
			inoutRecord2 = eachList.get(k);
			if(1 != inoutRecord2.getState()) {
				continue;
			}
			long intime = dft2.parse(inoutRecord2.getInTime()).getTime();
			long outtime = dft2.parse(inoutRecord2.getOutTime()).getTime();
			cumulative += (outtime - intime);
		}
		//返回的值
		InoutRecord inoutRecord3 = new InoutRecord();
		inoutRecord3 = eachList.get(startSite);
		inoutRecord3.setOutTime(eachList.get(endSite-1).getOutTime());//最后出门时间
		String fabTime = "";
		fabTime = df.format(((float)cumulative/(float)(1000*60*60)))+"h normal";
//		if(dayNight) {
//		}else {
//			fabTime = df.format(((float)cumulative/(float)(1000*60*60)))+"h night";
//		}
		inoutRecord3.setSpareField(fabTime);//fab时长
		inoutRecord3.setInOutCount((endSite-startSite));//进出次数
		return inoutRecord3;
	}
	
	public static InoutRecord calculationLater(List<InoutRecord> eachList, Integer startSite,Integer endSite,boolean dayNight) throws ParseException {
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		InoutRecord inoutRecord2 = new InoutRecord();
		inoutRecord2 = eachList.get(startSite);
		boolean flag = false;
		if(notEmpty(inoutRecord2.getInTime())) {
			if(dayNight) {//白班
				if(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(inoutRecord2.getInTime().substring(0, 10)+" 07:56:00"))) {
					//说明迟到了
					long time1 = dft2.parse(inoutRecord2.getInTime().substring(0, 10)+" 07:56:00").getTime();
					long time2 = dft2.parse(inoutRecord2.getInTime()).getTime();
					long cumulative = (time2-time1)/(1000*60);
					inoutRecord2.setSpareField(cumulative+"min day later");
					flag = true;
				}
			}else {//夜班
				if(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(inoutRecord2.getInTime().substring(0, 10)+" 19:56:00"))) {
					//说明迟到了(若其为第二天凌晨来的，不算，应该有特殊原因)
					long time1 = dft2.parse(inoutRecord2.getInTime().substring(0, 10)+" 19:56:00").getTime();
					long time2 = dft2.parse(inoutRecord2.getInTime()).getTime();
					long cumulative = (time2-time1)/(1000*60);
					inoutRecord2.setSpareField(cumulative+"min night later");
					flag = true;
				}
			}
		}
		if(flag) {
			return inoutRecord2;
		}else {
			return null;
		}
		
	}
	
	public static InoutRecord calculationEarly(List<InoutRecord> eachList, Integer startSite,Integer endSite,boolean dayNight) throws ParseException {
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		InoutRecord inoutRecord2 = new InoutRecord();
		inoutRecord2 = eachList.get(endSite-1);
		boolean flag = false;
		if(notEmpty(inoutRecord2.getOutTime())) {
			if(dayNight) {//白班
				if(dft2.parse(inoutRecord2.getOutTime()).before(dft2.parse(inoutRecord2.getOutTime().substring(0, 10)+" 19:59:00"))) {
					//说明早退实锤
					long time1 = dft2.parse(inoutRecord2.getOutTime().substring(0, 10)+" 19:59:00").getTime();
					long time2 = dft2.parse(inoutRecord2.getOutTime()).getTime();
					long cumulative = (time1-time2)/(1000*60);
					inoutRecord2.setSpareField(cumulative+"min day early");
					flag = true;
				}
			}else {//夜班
				if(dft2.parse(inoutRecord2.getOutTime()).before(dft2.parse(inoutRecord2.getOutTime().substring(0, 10)+" 07:59:00"))) {
					//说明迟到了(若其为第二天凌晨来的，不算，应该有特殊原因)
					long time1 = dft2.parse(inoutRecord2.getOutTime().substring(0, 10)+" 07:59:00").getTime();
					long time2 = dft2.parse(inoutRecord2.getOutTime()).getTime();
					long cumulative = (time1-time2)/(1000*60);
					inoutRecord2.setSpareField(cumulative+"min night early");
					flag = true;
				}
			}
		}
		if(flag) {
			return inoutRecord2;
		}else {
			return null;
		}
		
	}

	/**
	 * 对于夜班的人，判断是否为同一天 逻辑： 1如果str1是当日下午14点以后23：59以前，而str2是次日的14：00以后
	 * 2如果str1是00：00以后，当日14点以前，str2大于当日的14：00 3如果str1 str2差值大于14小时
	 * 这3种情况都说明此条记录已经是新的工作日了
	 * 
	 * @param str1
	 * @param str2
	 * @return Boolean
	 * @throws ParseException
	 */
	public static Boolean N_is_sameday(String str1, String str2) throws ParseException {
		Boolean flag = true;
		SimpleDateFormat dft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String temp2 = str1.substring(0, 10) + " 23:59:59";
		String temp3 = str1.substring(0, 10) + " 14:00:00";
		String temp4 = str1.substring(0, 10) + " 00:00:00";
		Calendar cld = Calendar.getInstance();
		cld.setTime(dft.parse(temp3));
		cld.add(Calendar.DATE, 1);
		long date5 = cld.getTime().getTime();// 次日14点

		long date1 = dft.parse(str1).getTime();//
		long date2 = dft.parse(str2).getTime();//
		long date4 = dft.parse(temp2).getTime();// 当日24点
		long date6 = dft.parse(temp4).getTime();// 当日0点
		long date8 = dft.parse(temp3).getTime();// 当日14点
		if ((date1 > date8 && date1 < date4 && date2 > date5) || (date1 > date6 && date1 < date8 && date2 > date8)
				|| (date2 - date1) > 14 * 60 * 60 * 1000) {
			flag = false;
		}
		return flag;
	}

	/**
	 * 夜班早退计算 （最难理解的一个方法，难理解在于它的应用场景）
	 * 
	 * @param str1
	 * @return
	 * @throws ParseException
	 */
	public static List<Integer> N_leave(String str1) throws ParseException {
		List<Integer> list = new ArrayList<Integer>();
		Integer flag = 0;
		Integer minutes = 0;
		SimpleDateFormat dft = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String temp2 = str1.substring(0, 10) + " 23:59:59";
		String temp3 = str1.substring(0, 10) + " 14:00:00";
		String temp5 = str1.substring(0, 10) + " 07:59:00";

		long date1 = dft.parse(str1).getTime();//
		long date4 = dft.parse(temp2).getTime();// 当日24点
		long date3 = dft.parse(temp5).getTime();// 当日8点
		long date8 = dft.parse(temp3).getTime();// 当日14点
		if (date1 > date8 && date1 < date4) {
			flag = 1;
			Calendar cld = Calendar.getInstance();
			cld.setTime(dft.parse(temp5));
			cld.add(Calendar.DATE, 1);
			long date5 = cld.getTime().getTime();// 次日8点
			minutes = (int) ((date5 - date1) / (1000 * 60));
		} else {
			if (date3 > date1) {
				flag = 1;
				minutes = (int) ((date3 - date1) / (1000 * 60));
			}
		}
		list.add(flag);
		list.add(minutes);
		return list;
	}

	@Override
	public List<InoutRecord> queryReportLater(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		/**
		 * 迟到
		 */
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		String day = "";
		int later = 0;// 默认0没有迟到
		int minutes1 = 0;// 迟到时间
		int re = 0;// 上一条记录无效，上来就是出 默认0有效
		String cardNo = "";
		List<Integer> record = new ArrayList<Integer>();

		// 夜班：inTim.substring(0, 10) 重新考量
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		int size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date1 = new Date();
			if (4 != state) {
				date1 = dft2.parse(inTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim;
				} else {
					day = outTim;
				}
				// 判断是否迟到 并记录
				if (4 != state) {
					String str3 = inTim.substring(0, 10) + " 19:56:00";
					Date date3 = dft2.parse(str3);
					if (date1.compareTo(date3) > 0) {
						later = 1;
						minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
					}
				} else {
					re = 1;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim;
					} else {
						day_ = outTim;
					}
					if (N_is_sameday(day, day_)) {// 说明是同一天
						if (1 == re) {// 只有同一个人，同一天，才来做这种判断 因为sql所有记录都是按人排列的
							if (4 != state) {
								String str3 = inTim.substring(0, 10) + " 19:56:00";
								Date date3 = dft2.parse(str3);
								if (date1.compareTo(date3) > 0) {
									later = 1;
									minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
								}
							} else {
								re = 1;
							}
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (1 == later) {//
							InoutRecord inoutRecord_ = new InoutRecord();
							inoutRecord_ = inoutRecordList.get(record.get(0));
							inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
							inoutRecordList2.add(inoutRecord_);
						}
						record.clear();
						minutes1 = 0;
						later = 0;
						re = 0;
						if (4 != state) {
							String str3 = inTim.substring(0, 10) + " 19:56:00";
							Date date3 = dft2.parse(str3);
							if (date1.compareTo(date3) > 0) {
								later = 1;
								minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
							}
						} else {
							re = 1;
						}
					}
					day = day_;
				} else {// 换人了，相当于第一次开始
					if (1 == later) {//
						InoutRecord inoutRecord_ = new InoutRecord();
						inoutRecord_ = inoutRecordList.get(record.get(0));
						inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
						inoutRecordList2.add(inoutRecord_);
					}
					record.clear();
					minutes1 = 0;
					later = 0;
					re = 0;

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim;
					} else {
						day = outTim;
					}
					// 判断是否迟到 并记录
					if (4 != state) {
						String str3 = inTim.substring(0, 10) + " 19:56:00";
						Date date3 = dft2.parse(str3);
						if (date1.compareTo(date3) > 0) {
							later = 1;
							minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
						}
					} else {
						re = 1;
					}
				}
			}
			record.add(i);
		}
		if (1 == later) {// 循环结束 收尾工作
			InoutRecord inoutRecord_ = new InoutRecord();
			inoutRecord_ = inoutRecordList.get(record.get(0));
			inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
			inoutRecordList2.add(inoutRecord_);
		}
		record.clear();
		minutes1 = 0;
		later = 0;
		re = 0;
		cardNo = "";
		day = "";

		// 白班：
		String sql3 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql3 += "and b.dept=?\r\n";
		} else {
			sql3 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql3 += "and 'all'=?\r\n";
		} else {
			sql3 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql3 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql3 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date1 = new Date();
			if (4 != state) {
				date1 = dft2.parse(inTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim.substring(0, 10);
				} else {
					day = outTim.substring(0, 10);
				}
				// 判断是否迟到 并记录
				if (4 != state) {
					String str3 = inTim.substring(0, 10) + " 07:56:00";
					Date date3 = dft2.parse(str3);
					if (date1.compareTo(date3) > 0) {
						later = 1;
						minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
					}
				} else {
					re = 1;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim.substring(0, 10);
					} else {
						day_ = outTim.substring(0, 10);
					}
					// day_ 跟day天的次日的14点相比
					if (day.equals(day_)) {// 说明是同一天
						if (1 == re) {// 同一个人，同一天，做这种判断 因为sql所有记录都是按人排列的
							if (4 != state) {
								String str3 = inTim.substring(0, 10) + " 07:56:00";
								Date date3 = dft2.parse(str3);
								if (date1.compareTo(date3) > 0) {
									later = 1;
									minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
								}
							} else {
								re = 1;
							}
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (1 == later) {//
							InoutRecord inoutRecord_ = new InoutRecord();
							inoutRecord_ = inoutRecordList.get(record.get(0));
							inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
							inoutRecordList2.add(inoutRecord_);
						}
						record.clear();
						minutes1 = 0;
						later = 0;
						re = 0;
						if (4 != state) {
							String str3 = inTim.substring(0, 10) + " 07:56:00";
							Date date3 = dft2.parse(str3);
							if (date1.compareTo(date3) > 0) {
								later = 1;
								minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
							}
						} else {
							re = 1;
						}
					}
					day = day_;
				} else {// 换人了，相当于第一次开始
					if (1 == later) {//
						InoutRecord inoutRecord_ = new InoutRecord();
						inoutRecord_ = inoutRecordList.get(record.get(0));
						inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
						inoutRecordList2.add(inoutRecord_);
					}
					record.clear();
					minutes1 = 0;
					later = 0;
					re = 0;

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim.substring(0, 10);
					} else {
						day = outTim.substring(0, 10);
					}
					// 判断是否迟到 并记录
					if (4 != state) {
						String str3 = inTim.substring(0, 10) + " 07:56:00";
						Date date3 = dft2.parse(str3);
						if (date1.compareTo(date3) > 0) {
							later = 1;
							minutes1 = (int) ((date1.getTime() - date3.getTime()) / (1000 * 60));// 分钟
						}
					} else {
						re = 1;
					}
				}
			}
			record.add(i);
		}
		if (1 == later) {// 循环结束 收尾工作
			InoutRecord inoutRecord_ = new InoutRecord();
			inoutRecord_ = inoutRecordList.get(record.get(0));
			inoutRecord_.setSpareField(minutes1 + "min later");// 待确认
			inoutRecordList2.add(inoutRecord_);
		}
		record.clear();
		minutes1 = 0;
		later = 0;
		re = 0;
		cardNo = "";
		day = "";

		return inoutRecordList2;
	}
	
	@Override
	public List<InoutRecord> queryReportLater2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		timeFrom = timeFrom.replace('-', '/');
		timeTo = timeTo.replace('-', '/');
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		 
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 13:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 13:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB' OR c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		//inoutRecordList = new ArrayList<InoutRecord>();
		timeTo = nextDay(timeTo);
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		List<InoutRecord> tempList = new ArrayList<InoutRecord>();
		List<List<InoutRecord>> inoutRecordListList = new ArrayList<List<InoutRecord>>();
		int size = inoutRecordList.size();
		String carNo = "";
		for (int i=0; i<size; i++) {
			if(""!=carNo) {
				if(inoutRecordList.get(i).getCarNo().equals(carNo)) {
					tempList.add(inoutRecordList.get(i));
				} else {
					inoutRecordListList.add(tempList);
					tempList = new ArrayList<InoutRecord>(); 
					tempList.add(inoutRecordList.get(i));
					carNo = inoutRecordList.get(i).getCarNo();
				}
			} else {//第一次
				carNo = inoutRecordList.get(i).getCarNo();
				tempList.add(inoutRecordList.get(i));
			}
		}
		inoutRecordListList.add(tempList);//到此，inoutRecordListList中的每一项就是一个人的所有记录
		
		/**
		 * 总结出这个问题是：时间段内的统计应该这么来：
		 * 起始日期的白班开始，到截至日期的夜班结束（夜班结束的时间应该是截至日期的次日14/12点）
		 * 所以：
		 * 起始日期前一天的夜班出入记录要去掉
		 * 截止日期次日的白班/夜班的记录都要去掉（只留次日前一日的夜班！！）
		 * 这样，在此基础上再做统计，数据才会准确！
		 * 现在开始去掉头和尾的数据！！！！！！！
		 * 我的sql中有个or，是个麻烦的点
		 */
		
		List<List<Integer>> allSite = new ArrayList<List<Integer>>();
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			List<Integer> each26site = new ArrayList<Integer>();
			eachList = inoutRecordListList.get(i);
			boolean f2 = false;//截至日期的后一天上午有记录的
			int m1 = 0;
			int m2 = 0;
			for(int j=0;j<eachList.size();j++) {//对每一个人的记录进行处理
				//需要记录是按时间顺序排列的   待确认
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//if里边应该是一个比较值，试想早上下班回家，晚上来上班，就有问题
				if((notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)
						||(notEmpty(each.getOutTime())?(dft2.parse(each.getOutTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)) {
					m1 = j-1;
					break;
				}
			}
			each26site.add(m1);
			for(int j=0;j<eachList.size();j++) {
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false 不用如上一样处理
				if((notEmpty(each.getInTime())?(each.getInTime().substring(0, 10).equals(timeTo)):false)
						||(notEmpty(each.getOutTime())?(each.getOutTime().substring(0, 10).equals(timeTo)):false)) {
					f2 = true;
					m2 = j;
					break;
				}
			}
			if(f2) {
				each26site.add(m2);
			} else {
				each26site.add(-1);
			}
			allSite.add(each26site);//allSite.get(X).get(0)是头部、allSite.get(X).get(1)是尾部
		}
		//到此，allSite中已经包含了每一个人的所有记录中 可能需要去掉的起始日期之前的夜班和截至日期的次日的非前一日夜班 所在的节点位置
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			eachList = inoutRecordListList.get(i);
			int site2 = allSite.get(i).get(1).intValue();
			if(-1 != site2) {
				int lengthChange = eachList.size();
				if(((notEmpty(eachList.get(site2).getInTime())?(dft2.parse(eachList.get(site2).getInTime()).before(dft2.parse(timeTo+" 06:00:00"))):false)
						||(notEmpty(eachList.get(site2).getOutTime())?(dft2.parse(eachList.get(site2).getOutTime()).before(dft2.parse(timeTo+" 06:00:00"))):false))
						&&(true)) {
					/**
					 * (notEmpty(eachList.get(eachList.size()-1).getOutTime())?(dft2.parse(eachList.get(eachList.size()-1).getOutTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
								||(notEmpty(eachList.get(eachList.size()-1).getInTime())?(dft2.parse(eachList.get(eachList.size()-1).getInTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
					 */
					//表示的确是上一天的夜班，需要统计进去
				} else {
					//是次日的班了，不属于上一天的夜班
					for(int j=lengthChange-1;j>=site2;j--) {
						eachList.remove(j);//
					}
				}
			}
			//此时，eachList的尾巴已经可能被剪掉一截了，现在对其头部进行操作
			int site1 = allSite.get(i).get(0).intValue();
			if(-1 != site1) {
				if(((notEmpty(eachList.get(0).getInTime())?(dft2.parse(eachList.get(0).getInTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false)
						||(notEmpty(eachList.get(0).getOutTime())?(dft2.parse(eachList.get(0).getOutTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false))
						&&((notEmpty(eachList.get(site1).getOutTime())?(dft2.parse(eachList.get(site1).getOutTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false)
								||(notEmpty(eachList.get(site1).getInTime())?(dft2.parse(eachList.get(site1).getInTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false))) {
					//早6点以前有进，在10点以前回家了，则为25号的夜班
					for(int j=0;j<=site1;j++) {
						eachList.remove(0);
					}
				}
			}
			//至此，eachList的记录已经是干净的了（尾巴可能没干净,可能性几乎为0，没关系，后面有容错）
			//下面eachList是该员很多天的很多进出记录     还在大的循环体内
			Integer startSite = null;//包含
			Integer endSite = null;//不包含
			for(int j=0;j<eachList.size();j++) {
				InoutRecord inoutRecord = new InoutRecord();
				inoutRecord = eachList.get(j);
				/**
				 * 难点在于如何识别哪些记录是什么班别：
				 * 这样来：因为需要统计的员工只有两种、而且很单纯：白班和夜班
				 * 那么每次只需要抓取开始的日期  因为记录是干净的
				 * 如果第一条进入时间是白天06（夜班可能）点到16点   就可以确定是白班
				 * 如果第一条进入时间是19点到23.59.59   可确定是夜班   
				 * 
				 * 程序模拟大脑分析班别的过程！
				 */
				boolean dayNight = true;//true：白天    false：夜晚
				if(0==j) {
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							
							if (((notEmpty(inoutRecord2.getInTime())
									? (dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 00:00:01")))
									: false)
									|| (notEmpty(inoutRecord2.getOutTime())
											? (dft2.parse(inoutRecord2.getOutTime())
													.after(dft2.parse(thisDay + " 00:00:01")))
											: false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecord_ = calculationLater(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecord_) {
							inoutRecordList2.add(inoutRecord_);
						}
					}else {
						//夜班
						dayNight = false;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);

							if (((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 14:00:01"))): false)
									|| (notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay + " 14:00:01"))):false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecord_ = calculationLater(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecord_) {
							inoutRecordList2.add(inoutRecord_);
						}
					}
				}
				if(j==endSite) {//&&endSite!=eachList.size()不会
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecord_ = calculationLater(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecord_) {
							inoutRecordList2.add(inoutRecord_);
						}
					}else {
						//夜班
						dayNight = false;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecord_ = calculationLater(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecord_) {
							inoutRecordList2.add(inoutRecord_);
						}
					}
				}
			}
		}
		return inoutRecordList2;
	}

	@Override
	public List<InoutRecord> queryReportLeave(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		/**
		 * 早退
		 */
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		String day = "";
		int minutes2 = 0;// 早退时间
		Date date3 = new Date();
		String data3Str = "";
		String cardNo = "";
		List<Integer> record = new ArrayList<Integer>();

		// 夜班：
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		int size = inoutRecordList.size();
		List<Integer> listx = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date2 = new Date();
			if (!"".equals(outTim)) {
				date2 = dft2.parse(outTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天 节点是晚8点到次日早8点
				if (4 != state) {
					day = inTim;
				} else {
					day = outTim;
				}
				// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
				if (1 == state || 4 == state) {
					date3 = date2;
					data3Str = outTim;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim;
					} else {
						day_ = outTim;
					}
					if (N_is_sameday(day, day_)) {// 说明是同一天
						// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
						if (1 == state || 4 == state) {
							date3 = date2;
							data3Str = outTim;
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (!"".equals(data3Str)) {
							listx = N_leave(data3Str);
							// 技巧在于出的时间已经确定了，若20:00（14点）-23:59以前则必然早退用第二天的8点算；否则，若出（0点~14点）则计算得是否早退
							if (1 == listx.get(0)) {
								// 早退实锤：
								minutes2 = listx.get(1);// 分钟
								InoutRecord inoutRecord_ = new InoutRecord();
								inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
								inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
								String ot = inoutRecord_.getOutTime();
								if (null != ot && !"".equals(ot)) {
									inoutRecordList2.add(inoutRecord_);
								}
							}
						}
						listx = new ArrayList<Integer>();
						record.clear();
						minutes2 = 0;
						date3 = new Date();
						data3Str = "";

						if (1 == state || 4 == state) {
							date3 = date2;
							data3Str = outTim;
						}
					}
					day = day_;// 考虑 对夜班 跨天 合不合适
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (!"".equals(data3Str)) {
						listx = N_leave(data3Str);
						// 技巧在于出的时间已经确定了，若20:00（14点）-23:59以前则必然早退用第二天的8点算；否则，若出（0点~14点）则计算得是否早退
						if (1 == listx.get(0)) {
							// 早退实锤：
							minutes2 = listx.get(1);// 分钟
							InoutRecord inoutRecord_ = new InoutRecord();
							inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
							inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
							String ot = inoutRecord_.getOutTime();
							if (null != ot && !"".equals(ot)) {
								inoutRecordList2.add(inoutRecord_);
							}
						}
					}
					listx = new ArrayList<Integer>();
					record.clear();
					minutes2 = 0;
					date3 = new Date();
					data3Str = "";

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim;
					} else {
						day = outTim;
					}
					// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
					if (1 == state || 4 == state) {
						date3 = date2;
						data3Str = outTim;
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作date3.toLocaleString().substring(0, 10).replace("-", "/")+"
		// 08:00:00"
		if (!"".equals(data3Str)) {
			listx = N_leave(data3Str);
			// 技巧在于出的时间已经确定了，若20:00（14点）-23:59以前则必然早退用第二天的8点算；否则，若出（0点~14点）则计算得是否早退
			if (1 == listx.get(0)) {
				// 早退实锤：
				minutes2 = listx.get(1);// 分钟
				InoutRecord inoutRecord_ = new InoutRecord();
				inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
				inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
				String ot = inoutRecord_.getOutTime();
				if (null != ot && !"".equals(ot)) {
					inoutRecordList2.add(inoutRecord_);
				}
			}
		}
		listx = new ArrayList<Integer>();
		record.clear();
		minutes2 = 0;
		date3 = new Date();
		cardNo = "";
		day = "";
		data3Str = "";

		// 白班：
		List<InoutRecord> inoutRecordList3 = new ArrayList<InoutRecord>();
		String sql3 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql3 += "and b.dept=?\r\n";
		} else {
			sql3 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql3 += "and 'all'=?\r\n";
		} else {
			sql3 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql3 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql3 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			// 避免取时间出错
			Date date2 = new Date();
			if (!"".equals(outTim)) {
				date2 = dft2.parse(outTim);
			}
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim.substring(0, 10);
				} else {
					day = outTim.substring(0, 10);
				}
				// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
				if (1 == state || 4 == state) {
					date3 = date2;
					data3Str = outTim;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim.substring(0, 10);
					} else {
						day_ = outTim.substring(0, 10);
					}
					// day_ 跟day天的次日的14点相比
					if (day.equals(day_)) {// 说明是同一天
						// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
						if (1 == state || 4 == state) {
							date3 = date2;
						}
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (!"".equals(data3Str)) {
							String str4 = data3Str.substring(0, 10) + " 19:59:00";
							Date date4 = dft2.parse(str4);
							if (date4.compareTo(date3) > 0) {
								// 早退实锤：
								minutes2 = (int) ((date4.getTime() - date3.getTime()) / (1000 * 60));// 分钟
								InoutRecord inoutRecord_ = new InoutRecord();
								inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
								inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
								String ot = inoutRecord_.getOutTime();
								if (null != ot && !"".equals(ot)) {
									inoutRecordList3.add(inoutRecord_);
								}
							}
						}
						record.clear();
						minutes2 = 0;
						date3 = new Date();
						data3Str = "";

						if (1 == state || 4 == state) {
							date3 = date2;
							data3Str = outTim;
						}
					}
					day = day_;
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (!"".equals(data3Str)) {
						String str4 = data3Str.substring(0, 10) + " 19:59:00";
						Date date4 = dft2.parse(str4);
						if (date4.compareTo(date3) > 0) {
							// 早退实锤：
							minutes2 = (int) ((date4.getTime() - date3.getTime()) / (1000 * 60));// 分钟
							InoutRecord inoutRecord_ = new InoutRecord();
							inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
							inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
							String ot = inoutRecord_.getOutTime();
							if (null != ot && !"".equals(ot)) {
								inoutRecordList3.add(inoutRecord_);
							}
						}
					}
					record.clear();
					minutes2 = 0;
					date3 = new Date();
					data3Str = "";

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim.substring(0, 10);
					} else {
						day = outTim.substring(0, 10);
					}
					// 记录“出”的时间：只针对状态为1和4的（证据确凿的，不冤枉好人） 某一天的最后一次记录正好是出的并且是早退
					if (1 == state || 4 == state) {
						date3 = date2;
						data3Str = outTim;
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作
		if (!"".equals(data3Str)) {
			Date date433 = dft2.parse(data3Str.substring(0, 10) + " 19:59:00");
			if (date433.compareTo(date3) > 0) {
				// 早退实锤：
				minutes2 = (int) ((date433.getTime() - date3.getTime()) / (1000 * 60));// 分钟
				InoutRecord inoutRecord_ = new InoutRecord();
				inoutRecord_ = inoutRecordList.get(record.get(record.size() - 1));
				inoutRecord_.setSpareField(minutes2 + "min early");// 待确认
				String ot = inoutRecord_.getOutTime();
				if (null != ot && !"".equals(ot)) {
					inoutRecordList3.add(inoutRecord_);
				}
			}
		}
		record.clear();
		minutes2 = 0;
		date3 = new Date();
		cardNo = "";
		day = "";
		data3Str = "";

		//inoutRecordList3 及下面的两个循环，是用以从结果集的侧面解决一天内存在两条早退记录的情况 只针对白班
		for(int z=1;z<inoutRecordList3.size();z++) {
			if(inoutRecordList3.get(z).getCarNo().equals(inoutRecordList3.get(z-1).getCarNo())
					&&inoutRecordList3.get(z).getOutTime().substring(0, 10).equals(inoutRecordList3.get(z-1).getOutTime().substring(0, 10))) {
				inoutRecordList3.remove(z-1);
			}
		}
		for(int r=0;r<inoutRecordList3.size();r++) {
			inoutRecordList2.add(inoutRecordList3.get(r));
		}
		
		return inoutRecordList2;
	}
	
	@Override
	public List<InoutRecord> queryReportLeave2(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {

		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		timeFrom = timeFrom.replace('-', '/');
		timeTo = timeTo.replace('-', '/');
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		 
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classtype\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 13:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 13:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB' OR c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		//inoutRecordList = new ArrayList<InoutRecord>();
		timeTo = nextDay(timeTo);
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		List<InoutRecord> tempList = new ArrayList<InoutRecord>();
		List<List<InoutRecord>> inoutRecordListList = new ArrayList<List<InoutRecord>>();
		int size = inoutRecordList.size();
		String carNo = "";
		for (int i=0; i<size; i++) {
			if(""!=carNo) {
				if(inoutRecordList.get(i).getCarNo().equals(carNo)) {
					tempList.add(inoutRecordList.get(i));
				} else {
					inoutRecordListList.add(tempList);
					tempList = new ArrayList<InoutRecord>(); 
					tempList.add(inoutRecordList.get(i));
					carNo = inoutRecordList.get(i).getCarNo();
				}
			} else {//第一次
				carNo = inoutRecordList.get(i).getCarNo();
				tempList.add(inoutRecordList.get(i));
			}
		}
		inoutRecordListList.add(tempList);//到此，inoutRecordListList中的每一项就是一个人的所有记录
		
		/**
		 * 总结出这个问题是：时间段内的统计应该这么来：
		 * 起始日期的白班开始，到截至日期的夜班结束（夜班结束的时间应该是截至日期的次日14/12点）
		 * 所以：
		 * 起始日期前一天的夜班出入记录要去掉
		 * 截止日期次日的白班/夜班的记录都要去掉（只留次日前一日的夜班！！）
		 * 这样，在此基础上再做统计，数据才会准确！
		 * 现在开始去掉头和尾的数据！！！！！！！
		 * 我的sql中有个or，是个麻烦的点
		 */
		
		List<List<Integer>> allSite = new ArrayList<List<Integer>>();
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			List<Integer> each26site = new ArrayList<Integer>();
			eachList = inoutRecordListList.get(i);
			boolean f2 = false;//截至日期的后一天上午有记录的
			int m1 = 0;
			int m2 = 0;
			for(int j=0;j<eachList.size();j++) {//对每一个人的记录进行处理
				//需要记录是按时间顺序排列的   待确认
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//if里边应该是一个比较值，试想早上下班回家，晚上来上班，就有问题
				if((notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)
						||(notEmpty(each.getOutTime())?(dft2.parse(each.getOutTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false)) {
					m1 = j-1;
					break;
				}
			}
			each26site.add(m1);
			for(int j=0;j<eachList.size();j++) {
				InoutRecord each = new InoutRecord();
				each = eachList.get(j);
				//notEmpty(each.getInTime())?(dft2.parse(each.getInTime()).after(dft2.parse(timeFrom+" 14:00:00"))):false 不用如上一样处理
				if((notEmpty(each.getInTime())?(each.getInTime().substring(0, 10).equals(timeTo)):false)
						||(notEmpty(each.getOutTime())?(each.getOutTime().substring(0, 10).equals(timeTo)):false)) {
					f2 = true;
					m2 = j;
					break;
				}
			}
			if(f2) {
				each26site.add(m2);
			} else {
				each26site.add(-1);
			}
			allSite.add(each26site);//allSite.get(X).get(0)是头部、allSite.get(X).get(1)是尾部
		}
		//到此，allSite中已经包含了每一个人的所有记录中 可能需要去掉的起始日期之前的夜班和截至日期的次日的非前一日夜班 所在的节点位置
		for(int i=0;i<inoutRecordListList.size();i++) {
			List<InoutRecord> eachList = new ArrayList<InoutRecord>();
			eachList = inoutRecordListList.get(i);
			int site2 = allSite.get(i).get(1).intValue();
			if(-1 != site2) {
				int lengthChange = eachList.size();
				if(((notEmpty(eachList.get(site2).getInTime())?(dft2.parse(eachList.get(site2).getInTime()).before(dft2.parse(timeTo+" 06:00:00"))):false)
						||(notEmpty(eachList.get(site2).getOutTime())?(dft2.parse(eachList.get(site2).getOutTime()).before(dft2.parse(timeTo+" 06:00:00"))):false))
						&&(true)) {
					/**
					 * (notEmpty(eachList.get(eachList.size()-1).getOutTime())?(dft2.parse(eachList.get(eachList.size()-1).getOutTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
								||(notEmpty(eachList.get(eachList.size()-1).getInTime())?(dft2.parse(eachList.get(eachList.size()-1).getInTime()).before(dft2.parse(timeTo+" 10:00:00"))):false)
					 */
					//表示的确是上一天的夜班，需要统计进去
				} else {
					//是次日的班了，不属于上一天的夜班
					for(int j=lengthChange-1;j>=site2;j--) {
						eachList.remove(j);//
					}
				}
			}
			//此时，eachList的尾巴已经可能被剪掉一截了，现在对其头部进行操作
			int site1 = allSite.get(i).get(0).intValue();
			if(-1 != site1) {
				if(((notEmpty(eachList.get(0).getInTime())?(dft2.parse(eachList.get(0).getInTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false)
						||(notEmpty(eachList.get(0).getOutTime())?(dft2.parse(eachList.get(0).getOutTime()).before(dft2.parse(timeFrom+" 06:00:00"))):false))
						&&((notEmpty(eachList.get(site1).getOutTime())?(dft2.parse(eachList.get(site1).getOutTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false)
								||(notEmpty(eachList.get(site1).getInTime())?(dft2.parse(eachList.get(site1).getInTime()).before(dft2.parse(timeFrom+" 10:00:00"))):false))) {
					//早6点以前有进，在10点以前回家了，则为25号的夜班
					for(int j=0;j<=site1;j++) {
						eachList.remove(0);
					}
				}
			}
			//至此，eachList的记录已经是干净的了（尾巴可能没干净,可能性几乎为0，没关系，后面有容错）
			//下面eachList是该员很多天的很多进出记录     还在大的循环体内
			Integer startSite = null;//包含
			Integer endSite = null;//不包含
			for(int j=0;j<eachList.size();j++) {
				InoutRecord inoutRecord = new InoutRecord();
				inoutRecord = eachList.get(j);
				/**
				 * 难点在于如何识别哪些记录是什么班别：
				 * 这样来：因为需要统计的员工只有两种、而且很单纯：白班和夜班
				 * 那么每次只需要抓取开始的日期  因为记录是干净的
				 * 如果第一条进入时间是白天06（夜班可能）点到16点   就可以确定是白班
				 * 如果第一条进入时间是19点到23.59.59   可确定是夜班   
				 * 
				 * 程序模拟大脑分析班别的过程！
				 */
				boolean dayNight = true;//true：白天    false：夜晚
				if(0==j) {
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							
							if (((notEmpty(inoutRecord2.getInTime())
									? (dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 00:00:01")))
									: false)
									|| (notEmpty(inoutRecord2.getOutTime())
											? (dft2.parse(inoutRecord2.getOutTime())
													.after(dft2.parse(thisDay + " 00:00:01")))
											: false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecordX = calculationEarly(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecordX) {
							inoutRecordList2.add(inoutRecordX);
						}
					}else {
						//夜班
						dayNight = false;
						startSite = 0;//包含
						endSite = null;//不包含
						boolean yijinggetstart = false;//只会取符合条件的第一个值
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);

							if (((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay + " 14:00:01"))): false)
									|| (notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay + " 14:00:01"))):false))
									&& !yijinggetstart) {
								startSite = k;
								yijinggetstart = true;
							}

							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecordX = calculationEarly(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecordX) {
							inoutRecordList2.add(inoutRecordX);
						}
					}
				}
				if(j==endSite) {//&&endSite!=eachList.size()不会
					String thisDay = notEmpty(inoutRecord.getInTime())?(inoutRecord.getInTime().substring(0, 10)):(inoutRecord.getOutTime().substring(0, 10));
					if((notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).after(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 06:00:00"))):false
							||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).after(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 06:00:00"))):false)
							&&(notEmpty(inoutRecord.getInTime())?(dft2.parse(inoutRecord.getInTime()).before(dft2.parse(inoutRecord.getInTime().substring(0, 10)+" 16:00:00"))):false
									||notEmpty(inoutRecord.getOutTime())?(dft2.parse(inoutRecord.getOutTime()).before(dft2.parse(inoutRecord.getOutTime().substring(0, 10)+" 16:00:00"))):false)) {
						//说明是白班
						dayNight = true;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(thisDay+" 23:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecordX = calculationEarly(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecordX) {
							inoutRecordList2.add(inoutRecordX);
						}
					}else {
						//夜班
						dayNight = false;
						startSite = endSite;//包含
						endSite = null;//不包含
						for(int k=0;k<eachList.size();k++) {
							InoutRecord inoutRecord2 = new InoutRecord();
							inoutRecord2 = eachList.get(k);
							if((notEmpty(inoutRecord2.getInTime())?(dft2.parse(inoutRecord2.getInTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)
									||(notEmpty(inoutRecord2.getOutTime())?(dft2.parse(inoutRecord2.getOutTime()).after(dft2.parse(nextDay(thisDay)+" 13:59:59"))):false)) {
								endSite = k;
								break;
							}
						}
						if(null==endSite) {
							endSite = eachList.size();
						}
						InoutRecord inoutRecordX = calculationEarly(eachList, startSite, endSite, dayNight);
						if(null!=inoutRecordX) {
							inoutRecordList2.add(inoutRecordX);
						}
					}
				}
			}
		}
		return inoutRecordList2;
	}

	@Override
	public List<InoutRecord> queryReport45m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		// 夜班：
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		int size = inoutRecordList.size();
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		String day = "";
		String cardNo = "";
		List<Integer> record = new ArrayList<Integer>();
		Calendar cld = Calendar.getInstance();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天 节点是晚8点到次日早8点
				if (4 != state) {
					day = inTim;
				} else {
					day = outTim;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim;
					} else {
						day_ = outTim;
					}
					if (N_is_sameday(day, day_)) {// 说明是同一天
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (record.size() > 1) {
							InoutRecord inoutRecord0 = new InoutRecord();
							InoutRecord inoutRecord_ = new InoutRecord();
							for (int j = 0; j < record.size(); j++) {
								if (0 == j) {
									inoutRecord0 = inoutRecordList.get(record.get(j));
								} else {
									inoutRecord_ = inoutRecordList.get(record.get(j));
									if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
											&& null != inoutRecord_.getInTime()
											&& !"".equals(inoutRecord_.getInTime())) {
										long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
										long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
										long time2 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 22:45:00")
												.getTime();
										cld.setTime(
												dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00"));
										cld.add(Calendar.DATE, 1);
										long time3 = cld.getTime().getTime();// 次日1点
										long time5 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 04:45:00")
												.getTime();
										long time6 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 07:00:00")
												.getTime();
										// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
										int eatTime = (int) ((time4 - time1) / (1000 * 60));
										if (time2 < time1 && time4 < time3 && eatTime > 46
												|| time5 < time1 && time4 < time6 && eatTime > 46) {
											inoutRecord0.setSpareField(eatTime + "min");
											inoutRecord_.setSpareField(eatTime + "min");
											InoutRecord inoutRecordTemp = inoutRecord0;
											inoutRecordTemp.setInTime(inoutRecord_.getInTime());
											inoutRecordList2.add(inoutRecordTemp);
//											inoutRecordList2.add(inoutRecord0);
//											inoutRecordList2.add(inoutRecord_);
										}
									}
									inoutRecord0 = inoutRecord_;
								}
							}
						}
						record.clear();
						day = day_;//
					}
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (record.size() > 1) {
						InoutRecord inoutRecord0 = new InoutRecord();
						InoutRecord inoutRecord_ = new InoutRecord();
						for (int j = 0; j < record.size(); j++) {
							if (0 == j) {
								inoutRecord0 = inoutRecordList.get(record.get(j));
							} else {
								inoutRecord_ = inoutRecordList.get(record.get(j));
								if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
										&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
									long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
									long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
									long time2 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 22:45:00")
											.getTime();
									cld.setTime(dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00"));
									cld.add(Calendar.DATE, 1);
									long time3 = cld.getTime().getTime();// 次日1点
									long time5 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 04:45:00")
											.getTime();
									long time6 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 07:00:00")
											.getTime();
									// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
									int eatTime = (int) ((time4 - time1) / (1000 * 60));
									if (time2 < time1 && time4 < time3 && eatTime > 46
											|| time5 < time1 && time4 < time6 && eatTime > 46) {
										inoutRecord0.setSpareField(eatTime + "min");
										inoutRecord_.setSpareField(eatTime + "min");
										InoutRecord inoutRecordTemp = inoutRecord0;
										inoutRecordTemp.setInTime(inoutRecord_.getInTime());
										inoutRecordList2.add(inoutRecordTemp);
//										inoutRecordList2.add(inoutRecord0);
//										inoutRecordList2.add(inoutRecord_);
									}
								}
								inoutRecord0 = inoutRecord_;
							}
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim;
					} else {
						day = outTim;
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作
		if (record.size() > 1) {
			InoutRecord inoutRecord0 = new InoutRecord();
			InoutRecord inoutRecord_ = new InoutRecord();
			for (int j = 0; j < record.size(); j++) {
				if (0 == j) {
					inoutRecord0 = inoutRecordList.get(record.get(j));
				} else {
					inoutRecord_ = inoutRecordList.get(record.get(j));
					if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
							&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
						long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
						long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
						long time2 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 22:45:00").getTime();
						cld.setTime(dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00"));
						cld.add(Calendar.DATE, 1);
						long time3 = cld.getTime().getTime();// 次日1点
						long time5 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 04:45:00").getTime();
						long time6 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 07:00:00").getTime();
						// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
						int eatTime = (int) ((time4 - time1) / (1000 * 60));
						if (time2 < time1 && time4 < time3 && eatTime > 46
								|| time5 < time1 && time4 < time6 && eatTime > 46) {
							inoutRecord0.setSpareField(eatTime + "min");
							inoutRecord_.setSpareField(eatTime + "min");
							InoutRecord inoutRecordTemp = inoutRecord0;
							inoutRecordTemp.setInTime(inoutRecord_.getInTime());
							inoutRecordList2.add(inoutRecordTemp);
//							inoutRecordList2.add(inoutRecord0);
//							inoutRecordList2.add(inoutRecord_);
						}
					}
					inoutRecord0 = inoutRecord_;
				}
			}
		}
		record.clear();
		cardNo = "";
		day = "";

		// 白班：
		String sql3 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql3 += "and b.dept=?\r\n";
		} else {
			sql3 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql3 += "and 'all'=?\r\n";
		} else {
			sql3 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql3 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql3 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim.substring(0, 10);
				} else {
					day = outTim.substring(0, 10);
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim.substring(0, 10);
					} else {
						day_ = outTim.substring(0, 10);
					}
					// day_ 跟day天的次日的14点相比
					if (day.equals(day_)) {// 说明是同一天
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (record.size() > 1) {
							InoutRecord inoutRecord0 = new InoutRecord();
							InoutRecord inoutRecord_ = new InoutRecord();
							for (int j = 0; j < record.size(); j++) {
								if (0 == j) {
									inoutRecord0 = inoutRecordList.get(record.get(j));
								} else {
									inoutRecord_ = inoutRecordList.get(record.get(j));
									if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
											&& null != inoutRecord_.getInTime()
											&& !"".equals(inoutRecord_.getInTime())) {
										long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
										long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
										long time2 = dft2.parse(day + " 10:45:00").getTime();
										long time3 = dft2.parse(day + " 13:00:00").getTime();
										long time5 = dft2.parse(day + " 16:45:00").getTime();
										long time6 = dft2.parse(day + " 19:00:00").getTime();
										int eatTime = (int) ((time4 - time1) / (1000 * 60));
										// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
										if (time2 < time1 && time4 < time3 && eatTime > 46
												|| time5 < time1 && time4 < time6 && eatTime > 46) {
											inoutRecord0.setSpareField(eatTime + "min");
											inoutRecord_.setSpareField(eatTime + "min");
											InoutRecord inoutRecordTemp = inoutRecord0;
											inoutRecordTemp.setInTime(inoutRecord_.getInTime());
											inoutRecordList2.add(inoutRecordTemp);
//											inoutRecordList2.add(inoutRecord0);
//											inoutRecordList2.add(inoutRecord_);
										}
									}
									inoutRecord0 = inoutRecord_;
								}
							}
						}
						record.clear();
						day = day_;
					}
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (record.size() > 1) {
						InoutRecord inoutRecord0 = new InoutRecord();
						InoutRecord inoutRecord_ = new InoutRecord();
						for (int j = 0; j < record.size(); j++) {
							if (0 == j) {
								inoutRecord0 = inoutRecordList.get(record.get(j));
							} else {
								inoutRecord_ = inoutRecordList.get(record.get(j));
								if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
										&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
									long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
									long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
									long time2 = dft2.parse(day + " 10:45:00").getTime();
									long time3 = dft2.parse(day + " 13:00:00").getTime();
									long time5 = dft2.parse(day + " 16:45:00").getTime();
									long time6 = dft2.parse(day + " 19:00:00").getTime();
									int eatTime = (int) ((time4 - time1) / (1000 * 60));
									// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
									if (time2 < time1 && time4 < time3 && eatTime > 46
											|| time5 < time1 && time4 < time6 && eatTime > 46) {
										inoutRecord0.setSpareField(eatTime + "min");
										inoutRecord_.setSpareField(eatTime + "min");
										InoutRecord inoutRecordTemp = inoutRecord0;
										inoutRecordTemp.setInTime(inoutRecord_.getInTime());
										inoutRecordList2.add(inoutRecordTemp);
//										inoutRecordList2.add(inoutRecord0);
//										inoutRecordList2.add(inoutRecord_);
									}
								}
								inoutRecord0 = inoutRecord_;
							}
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim.substring(0, 10);
					} else {
						day = outTim.substring(0, 10);
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作
		if (record.size() > 1) {
			InoutRecord inoutRecord0 = new InoutRecord();
			InoutRecord inoutRecord_ = new InoutRecord();
			for (int j = 0; j < record.size(); j++) {
				if (0 == j) {
					inoutRecord0 = inoutRecordList.get(record.get(j));
				} else {
					inoutRecord_ = inoutRecordList.get(record.get(j));
					if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
							&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
						long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
						long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
						long time2 = dft2.parse(day + " 10:45:00").getTime();
						long time3 = dft2.parse(day + " 13:00:00").getTime();
						long time5 = dft2.parse(day + " 16:45:00").getTime();
						long time6 = dft2.parse(day + " 19:00:00").getTime();
						int eatTime = (int) ((time4 - time1) / (1000 * 60));
						// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
						if (time2 < time1 && time4 < time3 && eatTime > 46
								|| time5 < time1 && time4 < time6 && eatTime > 46) {
							inoutRecord0.setSpareField(eatTime + "min");
							inoutRecord_.setSpareField(eatTime + "min");
							InoutRecord inoutRecordTemp = inoutRecord0;
							inoutRecordTemp.setInTime(inoutRecord_.getInTime());
							inoutRecordList2.add(inoutRecordTemp);
//							inoutRecordList2.add(inoutRecord0);
//							inoutRecordList2.add(inoutRecord_);
						}
					}
					inoutRecord0 = inoutRecord_;
				}
			}
		}
		record.clear();
		cardNo = "";
		day = "";

		return inoutRecordList2;
	}

	@Override
	public List<InoutRecord> queryReport30m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) throws SQLException, ParseException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
		if (null == parcelRange) {
			parcelRange = "all";
		}
		// 夜班：
		String sql2 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='NA' OR c.classType='NB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql2 += "and b.dept=?\r\n";
		} else {
			sql2 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql2 += "and 'all'=?\r\n";
		} else {
			sql2 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql2 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql2 += "ORDER BY a.carNo ASC,a.id_R ASC ";

		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql2, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		int size = inoutRecordList.size();
		SimpleDateFormat dft2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		List<InoutRecord> inoutRecordList2 = new ArrayList<InoutRecord>();
		String day = "";
		String cardNo = "";
		List<Integer> record = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天 节点是晚8点到次日早8点
				if (4 != state) {
					day = inTim;
				} else {
					day = outTim;
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim;
					} else {
						day_ = outTim;
					}
					if (N_is_sameday(day, day_)) {// 说明是同一天
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (record.size() > 1) {
							InoutRecord inoutRecord0 = new InoutRecord();
							InoutRecord inoutRecord_ = new InoutRecord();
							for (int j = 0; j < record.size(); j++) {
								if (0 == j) {
									inoutRecord0 = inoutRecordList.get(record.get(j));
								} else {
									inoutRecord_ = inoutRecordList.get(record.get(j));
									if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
											&& null != inoutRecord_.getInTime()
											&& !"".equals(inoutRecord_.getInTime())) {
										long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
										long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
										long time2 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 20:00:00")
												.getTime();
										long time3 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 23:00:00")
												.getTime();
										long time5 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00")
												.getTime();
										long time6 = dft2
												.parse(inoutRecord0.getOutTime().substring(0, 10) + " 05:00:00")
												.getTime();
										// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
										int eatTime = (int) ((time4 - time1) / (1000 * 60));
										if (time2 < time1 && time4 < time3 && eatTime > 31
												|| time5 < time1 && time4 < time6 && eatTime > 31) {
											inoutRecord0.setSpareField(eatTime + "min");
											inoutRecord_.setSpareField(eatTime + "min");
											InoutRecord inoutRecordTemp = inoutRecord0;
											inoutRecordTemp.setInTime(inoutRecord_.getInTime());
											inoutRecordList2.add(inoutRecordTemp);
											// inoutRecordList2.add(inoutRecord0);
											// inoutRecordList2.add(inoutRecord_);
										}
									}
									inoutRecord0 = inoutRecord_;
								}
							}
						}
						record.clear();
						day = day_;//
					}
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (record.size() > 1) {
						InoutRecord inoutRecord0 = new InoutRecord();
						InoutRecord inoutRecord_ = new InoutRecord();
						for (int j = 0; j < record.size(); j++) {
							if (0 == j) {
								inoutRecord0 = inoutRecordList.get(record.get(j));
							} else {
								inoutRecord_ = inoutRecordList.get(record.get(j));
								if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
										&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
									long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
									long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
									long time2 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 20:00:00")
											.getTime();
									long time3 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 23:00:00")
											.getTime();
									long time5 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00")
											.getTime();
									long time6 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 05:00:00")
											.getTime();
									// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
									int eatTime = (int) ((time4 - time1) / (1000 * 60));
									if (time2 < time1 && time4 < time3 && eatTime > 31
											|| time5 < time1 && time4 < time6 && eatTime > 31) {
										inoutRecord0.setSpareField(eatTime + "min");
										inoutRecord_.setSpareField(eatTime + "min");
										InoutRecord inoutRecordTemp = inoutRecord0;
										inoutRecordTemp.setInTime(inoutRecord_.getInTime());
										inoutRecordList2.add(inoutRecordTemp);
//										inoutRecordList2.add(inoutRecord0);
//										inoutRecordList2.add(inoutRecord_);
									}
								}
								inoutRecord0 = inoutRecord_;
							}
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim;
					} else {
						day = outTim;
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作
		if (record.size() > 1) {
			InoutRecord inoutRecord0 = new InoutRecord();
			InoutRecord inoutRecord_ = new InoutRecord();
			for (int j = 0; j < record.size(); j++) {
				if (0 == j) {
					inoutRecord0 = inoutRecordList.get(record.get(j));
				} else {
					inoutRecord_ = inoutRecordList.get(record.get(j));
					if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
							&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
						long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
						long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
						long time2 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 20:00:00").getTime();
						long time3 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 23:00:00").getTime();
						long time5 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 01:00:00").getTime();
						long time6 = dft2.parse(inoutRecord0.getOutTime().substring(0, 10) + " 05:00:00").getTime();
						// 判断ta是去吃饭：出和进的时间点在晚10：45-次日早1：00之间 4：45-7：00 相差45m （夜班吃饭时间）
						int eatTime = (int) ((time4 - time1) / (1000 * 60));
						if (time2 < time1 && time4 < time3 && eatTime > 31
								|| time5 < time1 && time4 < time6 && eatTime > 31) {
							inoutRecord0.setSpareField(eatTime + "min");
							inoutRecord_.setSpareField(eatTime + "min");
							InoutRecord inoutRecordTemp = inoutRecord0;
							inoutRecordTemp.setInTime(inoutRecord_.getInTime());
							inoutRecordList2.add(inoutRecordTemp);
//							inoutRecordList2.add(inoutRecord0);
//							inoutRecordList2.add(inoutRecord_);
						}
					}
					inoutRecord0 = inoutRecord_;
				}
			}
		}
		record.clear();
		cardNo = "";
		day = "";

		// 白班：
		String sql3 = "select a.id_R,a.adrass,a.carNo,a.inTime,a.outTime,a.state,a.SpareField,b.jobNo,b.empName,b.dept,b.parcel,c.classType\r\n"
				+ "from inoutrecord a,correspondingrelation b,classtype c\r\n"
				+ "where (date_format(a.inTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?\r\n"
				+ "				,'%Y-%c-%d 00:00:00') AND date_format(?,'%Y-%c-%d 23:59:59') \r\n"
				+ "or date_format(a.outTime,'%Y-%c-%d %H:%i:%s') BETWEEN date_format(?,'%Y-%c-%d 00:00:00')\r\n"
				+ "				 AND date_format(?,'%Y-%c-%d 23:59:59') )\r\n" + "and a.carNo=b.carNo\r\n"
				+ "and b.jobNo=c.jobNo\r\n" + "and c.yearMonth=?\r\n"
				+ "and (c.classType='DA' OR c.classType='DB')\r\n";
		if (!"all fab".equals(deptRange)) {
			sql3 += "and b.dept=?\r\n";
		} else {
			sql3 += "and 'all fab'=?\r\n";
		}
		if ("all".equals(parcelRange)) {
			sql3 += "and 'all'=?\r\n";
		} else {
			sql3 += "and b.parcel=?\r\n";
		}
		if (null != jobNo && !"".equals(jobNo)) {
			sql3 += "and b.jobNo like \"%\"?\"%\" \r\n";
		}
		sql3 += "ORDER BY a.carNo ASC,a.id_R ASC ";
		inoutRecordList = new ArrayList<InoutRecord>();
		if (null != jobNo && !"".equals(jobNo)) {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange, jobNo);
		} else {
			inoutRecordList = qr.query(sql3, new BeanListHandler<InoutRecord>(InoutRecord.class), timeFrom, timeTo,
					timeFrom, timeTo, yearMonth, deptRange, parcelRange);
		}
		size = inoutRecordList.size();
		for (int i = 0; i < size; i++) {
			int state = inoutRecordList.get(i).getState();
			String inTim = inoutRecordList.get(i).getInTime();
			String outTim = inoutRecordList.get(i).getOutTime();
			if (0 == i) {// 循环为第一次
				cardNo = inoutRecordList.get(i).getCarNo();
				// 用于判断是否为同一天
				if (4 != state) {
					day = inTim.substring(0, 10);
				} else {
					day = outTim.substring(0, 10);
				}
			} else {// 循环为第二次以上
					// 判断是否为同一个人的记录
				if (inoutRecordList.get(i).getCarNo().equals(cardNo)) {
					// 用于判断是否为同一天
					String day_ = "";
					if (4 != state) {
						day_ = inTim.substring(0, 10);
					} else {
						day_ = outTim.substring(0, 10);
					}
					// day_ 跟day天的次日的14点相比
					if (day.equals(day_)) {// 说明是同一天
					} else {// 不是同一天 先把之前的处理了 再开始新的周期
						if (record.size() > 1) {
							InoutRecord inoutRecord0 = new InoutRecord();
							InoutRecord inoutRecord_ = new InoutRecord();
							for (int j = 0; j < record.size(); j++) {
								if (0 == j) {
									inoutRecord0 = inoutRecordList.get(record.get(j));
								} else {
									inoutRecord_ = inoutRecordList.get(record.get(j));
									if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
											&& null != inoutRecord_.getInTime()
											&& !"".equals(inoutRecord_.getInTime())) {
										long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
										long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
										long time2 = dft2.parse(day + " 08:00:00").getTime();
										long time3 = dft2.parse(day + " 11:00:00").getTime();
										long time5 = dft2.parse(day + " 13:00:00").getTime();
										long time6 = dft2.parse(day + " 17:00:00").getTime();
										int eatTime = (int) ((time4 - time1) / (1000 * 60));
										// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
										if (time2 < time1 && time4 < time3 && eatTime > 31
												|| time5 < time1 && time4 < time6 && eatTime > 31) {
											inoutRecord0.setSpareField(eatTime + "min");
											inoutRecord_.setSpareField(eatTime + "min");
											InoutRecord inoutRecordTemp = inoutRecord0;
											inoutRecordTemp.setInTime(inoutRecord_.getInTime());
											inoutRecordList2.add(inoutRecordTemp);
//											inoutRecordList2.add(inoutRecord0);
//											inoutRecordList2.add(inoutRecord_);
										}
									}
									inoutRecord0 = inoutRecord_;
								}
							}
						}
						record.clear();
						day = day_;
					}
				} else {// 换人了 先把之前的处理了 再开始新的周期
					if (record.size() > 1) {
						InoutRecord inoutRecord0 = new InoutRecord();
						InoutRecord inoutRecord_ = new InoutRecord();
						for (int j = 0; j < record.size(); j++) {
							if (0 == j) {
								inoutRecord0 = inoutRecordList.get(record.get(j));
							} else {
								inoutRecord_ = inoutRecordList.get(record.get(j));
								if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
										&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
									long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
									long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
									long time2 = dft2.parse(day + " 08:00:00").getTime();
									long time3 = dft2.parse(day + " 11:00:00").getTime();
									long time5 = dft2.parse(day + " 13:00:00").getTime();
									long time6 = dft2.parse(day + " 17:00:00").getTime();
									int eatTime = (int) ((time4 - time1) / (1000 * 60));
									// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
									if (time2 < time1 && time4 < time3 && eatTime > 31
											|| time5 < time1 && time4 < time6 && eatTime > 31) {
										inoutRecord0.setSpareField(eatTime + "min");
										inoutRecord_.setSpareField(eatTime + "min");
										InoutRecord inoutRecordTemp = inoutRecord0;
										inoutRecordTemp.setInTime(inoutRecord_.getInTime());
										inoutRecordList2.add(inoutRecordTemp);
//										inoutRecordList2.add(inoutRecord0);
//										inoutRecordList2.add(inoutRecord_);
									}
								}
								inoutRecord0 = inoutRecord_;
							}
						}
					}
					record.clear();

					cardNo = inoutRecordList.get(i).getCarNo();
					// 用于判断是否为同一天
					if (4 != state) {
						day = inTim.substring(0, 10);
					} else {
						day = outTim.substring(0, 10);
					}
				}
			}
			record.add(i);
		}
		// 循环结束 收尾工作
		if (record.size() > 1) {
			InoutRecord inoutRecord0 = new InoutRecord();
			InoutRecord inoutRecord_ = new InoutRecord();
			for (int j = 0; j < record.size(); j++) {
				if (0 == j) {
					inoutRecord0 = inoutRecordList.get(record.get(j));
				} else {
					inoutRecord_ = inoutRecordList.get(record.get(j));
					if (null != inoutRecord0.getOutTime() && !"".equals(inoutRecord0.getOutTime())
							&& null != inoutRecord_.getInTime() && !"".equals(inoutRecord_.getInTime())) {
						long time1 = dft2.parse(inoutRecord0.getOutTime()).getTime();
						long time4 = dft2.parse(inoutRecord_.getInTime()).getTime();
						long time2 = dft2.parse(day + " 08:00:00").getTime();
						long time3 = dft2.parse(day + " 11:00:00").getTime();
						long time5 = dft2.parse(day + " 13:00:00").getTime();
						long time6 = dft2.parse(day + " 17:00:00").getTime();
						int eatTime = (int) ((time4 - time1) / (1000 * 60));
						// 判断ta是去吃饭：出和进的时间点在10:45:00-13:00:00 之间 16:45:00-19:00:00 相差45m
						if (time2 < time1 && time4 < time3 && eatTime > 31
								|| time5 < time1 && time4 < time6 && eatTime > 31) {
							inoutRecord0.setSpareField(eatTime + "min");
							inoutRecord_.setSpareField(eatTime + "min");
							InoutRecord inoutRecordTemp = inoutRecord0;
							inoutRecordTemp.setInTime(inoutRecord_.getInTime());
							inoutRecordList2.add(inoutRecordTemp);
//							inoutRecordList2.add(inoutRecord0);
//							inoutRecordList2.add(inoutRecord_);
						}
					}
					inoutRecord0 = inoutRecord_;
				}
			}
		}
		record.clear();
		cardNo = "";
		day = "";

		return inoutRecordList2;
	}

	@Override
	public List<Correspondingrelation> queryComboBoxAllDept() throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<Correspondingrelation> deptList = new ArrayList<Correspondingrelation>();
		String sql = "select distinct dept from correspondingrelation where dept<>'' order by dept";
		deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class));
		return deptList;
	}

	@Override
	public List<Correspondingrelation> queryComboBoxParcelDept(String dept) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<Correspondingrelation> deptList = new ArrayList<Correspondingrelation>();
		String sql = "select distinct parcel from correspondingrelation where dept=? and parcel<>'' order by parcel";
		deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), dept);
		return deptList;
	}

	@Override
	public List<Correspondingrelation> getDataEmpInfo(String empDept, String empParcel, String time8, String time7,
			String time6, String time1, String time2, String time4, String time5) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<Correspondingrelation> deptList = new ArrayList<Correspondingrelation>();
		String sql = "select a.carNo,a.jobNo,a.dept,a.empName,a.parcel,a.spare\r\n"
				+ ",(select c.classType from classType c where c.jobNo=a.jobNo and c.YEARMONTH=?) previousClassType3\r\n"
				+ ",(select d.classType from classType d where d.jobNo=a.jobNo and d.YEARMONTH=?) previousClassType2\r\n"
				+ ",(select e.classType from classType e where e.jobNo=a.jobNo and e.YEARMONTH=?) previousClassType1\r\n"
				+ ",(select f.classType from classType f where f.jobNo=a.jobNo and f.YEARMONTH=?) currentClassType\r\n"
				+ ",(select g.classType from classType g where g.jobNo=a.jobNo and g.YEARMONTH=?) nextClassType1\r\n"
				+ ",(select h.classType from classType h where h.jobNo=a.jobNo and h.YEARMONTH=?) nextClassType2\r\n"
				+ ",(select i.classType from classType i where i.jobNo=a.jobNo and i.YEARMONTH=?) nextClassType3\r\n"
				+ "from correspondingrelation a\r\n" + "where 1=1 ";
		if (!"all fab".equals(empDept)) {
			sql += "and a.dept =? \r\n";
			if (!"all".equals(empParcel)) {
				sql += "and a.parcel =? \r\n";
				deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), time8,
						time7, time6, time1, time2, time4, time5, empDept, empParcel);
			} else {
				deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), time8,
						time7, time6, time1, time2, time4, time5, empDept);
			}
		} else {
			deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), time8,
					time7, time6, time1, time2, time4, time5);
		}
		return deptList;
	}

	@Override
	public List<Correspondingrelation> getDataEmpInfo2(String empDept, String empParcel, String time1, String jobNoJTF)
			throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<Correspondingrelation> deptList = new ArrayList<Correspondingrelation>();
		String sql = "select a.carNo,a.jobNo,a.dept,a.empName,a.parcel,a.spare,f.classType currentClassType\r\n"
				+ "from correspondingrelation a left join classType f on a.jobNo=f.jobNo and f.YEARMONTH=?\r\n"
				+ "where 1=1 \r\n";
		if (!"".equals(jobNoJTF)) {
			sql += "and a.jobNo like \"%\"?\"%\" \r\n";
			if (!"all fab".equals(empDept)) {
				sql += "and a.dept =? \r\n";
				if (!"all".equals(empParcel)) {
					sql += "and a.parcel =? \r\n";
					deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class),
							time1, jobNoJTF, empDept, empParcel);
				} else {
					deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class),
							time1, jobNoJTF, empDept);
				}
			} else {
				deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), time1,
						jobNoJTF);
			}
		} else {
			if (!"all fab".equals(empDept)) {
				sql += "and a.dept =? \r\n";
				if (!"all".equals(empParcel)) {
					sql += "and a.parcel =? \r\n";
					deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class),
							time1, empDept, empParcel);
				} else {
					deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class),
							time1, empDept);
				}
			} else {
				deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class),
						time1);
			}
		}
		return deptList;
	}

	@Override
	public int updateEmpParcel(String jobNo, String aValue) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		String sql = "update correspondingrelation set parcel=? where jobNo=?";
		return qr.execute(sql, aValue, jobNo);
	}

	@Override
	public int updateEmpClassType(String jobNo, String aValue, String time3) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		String sql = "update ClassType set classtype=? where jobno=? and yearMonth=?";
		return qr.execute(sql, aValue, jobNo, time3);
	}

	@Override
	public int insertEmpClassType(String jobNo, String aValue, String time3) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		String sql0 = "update ClassType set classtype=? where jobno=? and yearMonth=?";
		int xx = qr.execute(sql0, aValue, jobNo, time3);
		int yy = 0;
		if (xx < 1) {
			String sql = "insert into ClassType (jobNo,yearMonth,classType,state) VALUES (?,?,?,0)";
			yy = qr.execute(sql, jobNo, time3, aValue);
		}

		return (xx < 1) ? yy : xx;
	}

	@Override
	public int queryEmpClassTypeCount(String jobNo, String time3) throws SQLException {
		QueryRunner qr = BaseUtils.getQueryRunner();
		List<Correspondingrelation> deptList = new ArrayList<Correspondingrelation>();
		String sql = "select COUNT(*) total from ClassType where jobNo=? and yearMonth=?";
		deptList = qr.query(sql, new BeanListHandler<Correspondingrelation>(Correspondingrelation.class), jobNo, time3);
		// 实在没有好的办法
		int xx = deptList.get(0).getTotal();
		return xx;
	}

	@SuppressWarnings("static-access")
	@Override
	public String updateInsertClassType() throws SQLException {
		// 先预定：这个方法是每月26号执行
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
		// Date date = new Date();
		// String yearMonth = dft.format(date);//当月
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		String monthStr = "";// 当月
		if (month < 10) {
			monthStr = year + "-0" + month;
		} else {
			monthStr = year + "-" + month;
		}
		calendar.add(calendar.MONTH, +1);
		String yearMonth2 = dft.format(calendar.getTime());// 下月

		QueryRunner qr = BaseUtils.getQueryRunner();
		List<ClassType> list = new ArrayList<ClassType>();
		List<ClassType> list2 = new ArrayList<ClassType>();
		String sql = "select * from classtype where classType<>'NOR' and yearMonth=?";
		list = qr.query(sql, new BeanListHandler<ClassType>(ClassType.class), monthStr);
		ClassType classType = null;
		for (int i = 0; i < list.size(); i++) {
			classType = new ClassType();
			classType = list.get(i);
			if (month % 2 == 0) {// 当月是双数月份 则下月需要换班
				String type = classType.getClassType();
				if (type.equals("NA")) {
					classType.setClassType("DA");
				} else if (type.equals("NB")) {
					classType.setClassType("DB");
				} else if (type.equals("DA")) {
					classType.setClassType("NA");
				} else if (type.equals("DB")) {
					classType.setClassType("NB");
				}
			}
			classType.setYearMonth(yearMonth2);
			list2.add(classType);
		}
		int xx = 0;
		int yy = 0;
		for (int i = 0; i < list2.size(); i++) {
			classType = new ClassType();
			classType = list2.get(i);
			String sql2 = "update classtype set classType=? where jobNo=? and yearMonth=?";
			int count = qr.execute(sql2, classType.getClassType(), classType.getJobNo(), classType.getYearMonth());
			xx = xx + count;
			if (0 == count) {
				String sql3 = "insert into classtype (jobNo,yearMonth,classtype) VALUES (?,?,?)";
				qr.execute(sql3, classType.getJobNo(), classType.getYearMonth(), classType.getClassType());
				yy++;
			}
		}
		return "更新:" + xx + ",新增:" + yy;
	}

	@Override
	public List<String> insertCurrentClassTypeInfo(List<Correspondingrelation> fromExcelList, String time1)
			throws SQLException {

		List<String> listStr = new ArrayList<String>();
		QueryRunner qr = BaseUtils.getQueryRunner();
		Correspondingrelation fromExcel;
		int updateNo = 0;
		int newCount = 0;
		int countt = 0;
		Object[][] params = new Object[fromExcelList.size()][];
		for (int i = 0; i < params.length; i++) {
			fromExcel = fromExcelList.get(i);
			params[i] = new Object[] { fromExcel.getParcel(), fromExcel.getCarNo() };
		}
		String sql = "update CorrespondingRelation set parcel=?  where carNo=?";
		int[] x = qr.batch(sql, params);
		for (int xs : x) {
			updateNo = updateNo + xs;
		}

		for (int i = 0; i < fromExcelList.size(); i++) {
			fromExcel = new Correspondingrelation();
			fromExcel = fromExcelList.get(i);
			String sql4 = "update classType set classType=? where jobNo=? and YEARMONTH=?";
			countt = qr.execute(sql4, fromExcel.getCurrentClassType(), fromExcel.getJobNo(), time1);// 暂时
			if (0 == countt) {
				String sql3 = "insert into classType (jobNo,YEARMONTH,classType) values (?,?,?)";
				newCount = newCount + qr.execute(sql3, fromExcel.getJobNo(), time1, fromExcel.getCurrentClassType());
			}
		}
		listStr.add(newCount + "");
		listStr.add(countt + "");
		return listStr;
	}

}
