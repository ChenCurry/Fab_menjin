package test.domain;

public class AlarmInfo {
	private String adrass;//地点
	private String carNo;//卡号
	private String dwCardReaderNo;//设备标识进出信息
	private String struTime;//报警时间
	private String empName;//姓名
	private String dept;//部门 组织
	private String parcel;//分部
	private String jobNo;//gonghao
	private String currentClassType;//当前班别
	private String nextClassType;//下月班别
	
	public AlarmInfo() {
	}
	public AlarmInfo(String adrass, String carNo, String dwCardReaderNo
			, String struTime, String empName, String dept,String jobNo) {
		this.adrass = adrass;
		this.carNo = carNo;
		this.dwCardReaderNo = dwCardReaderNo;
		this.struTime = struTime;
		this.empName = empName;
		this.dept = dept;
		this.jobNo = jobNo;
	}
	
	private String cardType;//卡类型
	private String dwMajor;//报警主类型 1报警 2异常 3操作  5事件
	private String dwMinor;//报警次类型 1报警 
	private String dwDoorNo;//
	private String dwIOTChannelNo;//IOT通道号
	private String sNetUser;//网络操作的用户名
	private String wInductiveEventType;//归纳事件类型，0-无效，客户端判断该值为非0值后，报警类型通过归纳事件类型区分，否则通过原有报警主次类型（dwMajor、dwMinor）区分
	
	private String fab10h;//统计在fab内呆的时间是否为10h
	
	/*
	 * private String byWhiteListNo;// private String byReportChannel;// private
	 * String byCardReaderKind;// private String dwVerifyNo;// private String
	 * dwAlarmInNo;// private String dwAlarmOutNo;// private String
	 * dwCaseSensorNo;// private String dwRs485No;// private String
	 * dwMultiCardGroupNo;// private String wAccessChannel;// private String
	 * byDeviceNo;// private String byDistractControlNo;// private String
	 * dwEmployeeNo;// private String wLocalControllerID;// private String
	 * byInternetAccess;// private String byType;//
	 */	
	
	
	public String getFab10h() {
		return fab10h;
	}
	public String getCurrentClassType() {
		return currentClassType;
	}
	public void setCurrentClassType(String currentClassType) {
		this.currentClassType = currentClassType;
	}
	public String getNextClassType() {
		return nextClassType;
	}
	public void setNextClassType(String nextClassType) {
		this.nextClassType = nextClassType;
	}
	public String getParcel() {
		return parcel;
	}
	public void setParcel(String parcel) {
		this.parcel = parcel;
	}
	public void setFab10h(String fab10h) {
		this.fab10h = fab10h;
	}
	public String getEmpName() {
		return empName;
	}
	public void setEmpName(String empName) {
		this.empName = empName;
	}
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getJobNo() {
		return jobNo;
	}
	public void setJobNo(String jobNo) {
		this.jobNo = jobNo;
	}
	public String getStruTime() {
		return struTime;
	}
	public void setStruTime(String struTime) {
		this.struTime = struTime;
	}
	public String getsNetUser() {
		return sNetUser;
	}
	public void setsNetUser(String sNetUser) {
		this.sNetUser = sNetUser;
	}
	public String getwInductiveEventType() {
		return wInductiveEventType;
	}
	public void setwInductiveEventType(String wInductiveEventType) {
		this.wInductiveEventType = wInductiveEventType;
	}
	public String getDwIOTChannelNo() {
		return dwIOTChannelNo;
	}
	public void setDwIOTChannelNo(String dwIOTChannelNo) {
		this.dwIOTChannelNo = dwIOTChannelNo;
	}

	public String getDwCardReaderNo() {
		return dwCardReaderNo;
	}
	public void setDwCardReaderNo(String dwCardReaderNo) {
		this.dwCardReaderNo = dwCardReaderNo;
	}
	public String getDwDoorNo() {
		return dwDoorNo;
	}
	public void setDwDoorNo(String dwDoorNo) {
		this.dwDoorNo = dwDoorNo;
	}

	public String getAdrass() {
		return adrass;
	}
	public void setAdrass(String adrass) {
		this.adrass = adrass;
	}
	public String getCarNo() {
		return carNo;
	}
	public void setCarNo(String carNo) {
		this.carNo = carNo;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getDwMajor() {
		return dwMajor;
	}
	public void setDwMajor(String dwMajor) {
		this.dwMajor = dwMajor;
	}
	public String getDwMinor() {
		return dwMinor;
	}
	public void setDwMinor(String dwMinor) {
		this.dwMinor = dwMinor;
	}
	@Override
	public String toString() {
		return "AlarmInfo [门=" + adrass + ", 卡号=" + carNo+ ", 读卡器编号(进出)=" + dwCardReaderNo+ ", 时间=" + struTime
				+ ", 卡类型=" + cardType + ", 报警主类型=" + dwMajor + ", 报警次类型=" + dwMinor 
				+ ", 门编号=" + dwDoorNo + ", IOT通道号=" + dwIOTChannelNo + ", 归纳事件类型=" + wInductiveEventType
				+ ", 网络用户名=" + sNetUser + "]";
	}
	
	
	
}
