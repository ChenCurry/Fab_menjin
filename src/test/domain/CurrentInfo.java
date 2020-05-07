package test.domain;

public class CurrentInfo {
	private String adrass;//地点
	private String carNo;//卡号
	private String dwCardReaderNo;//设备标识进出信息
	private String struTime;//报警时间
	private String cardType;//卡类型
	private String dwMajor;//报警主类型 1报警 2异常 3操作  5事件
	private String dwMinor;//报警次类型 1报警 
	private String dwDoorNo;//
	private String dwIOTChannelNo;//IOT通道号
	private String jobNo;//gonghao
	private String dept;//部门 组织
	private String empName;//姓名
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
	public String getDwCardReaderNo() {
		return dwCardReaderNo;
	}
	public void setDwCardReaderNo(String dwCardReaderNo) {
		this.dwCardReaderNo = dwCardReaderNo;
	}
	public String getStruTime() {
		return struTime;
	}
	public void setStruTime(String struTime) {
		this.struTime = struTime;
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
	public String getDwDoorNo() {
		return dwDoorNo;
	}
	public void setDwDoorNo(String dwDoorNo) {
		this.dwDoorNo = dwDoorNo;
	}
	public String getDwIOTChannelNo() {
		return dwIOTChannelNo;
	}
	public void setDwIOTChannelNo(String dwIOTChannelNo) {
		this.dwIOTChannelNo = dwIOTChannelNo;
	}
	public String getJobNo() {
		return jobNo;
	}
	public void setJobNo(String jobNo) {
		this.jobNo = jobNo;
	}
	public String getDept() {
		return dept;
	}
	public void setDept(String dept) {
		this.dept = dept;
	}
	public String getEmpName() {
		return empName;
	}
	public void setEmpName(String empName) {
		this.empName = empName;
	}
	@Override
	public String toString() {
		return "CurrentInfo [adrass=" + adrass + ", carNo=" + carNo + ", dwCardReaderNo=" + dwCardReaderNo
				+ ", struTime=" + struTime + ", cardType=" + cardType + ", dwMajor=" + dwMajor + ", dwMinor=" + dwMinor
				+ ", dwDoorNo=" + dwDoorNo + ", dwIOTChannelNo=" + dwIOTChannelNo + ", jobNo=" + jobNo + ", dept="
				+ dept + ", empName=" + empName + "]";
	}
	
	
	
}
