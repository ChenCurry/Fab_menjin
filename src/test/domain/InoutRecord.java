package test.domain;

public class InoutRecord {

	private String adrass;
	private String carNo;
	private String inTime;
	private String outTime;
	private Integer state;
	private String spareField;
	
	private String empName;
	private String dept;
	private String parcel;
	private String jobNo;
	private String classType;
	private Integer inOutCount;
	

	
	public Integer getInOutCount() {
		return inOutCount;
	}
	public void setInOutCount(Integer inOutCount) {
		this.inOutCount = inOutCount;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
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
	public String getInTime() {
		return inTime;
	}
	public void setInTime(String inTime) {
		this.inTime = inTime;
	}
	public String getOutTime() {
		return outTime;
	}
	public void setOutTime(String outTime) {
		this.outTime = outTime;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
	}
	public String getSpareField() {
		return spareField;
	}
	public void setSpareField(String spareField) {
		this.spareField = spareField;
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
	public String getParcel() {
		return parcel;
	}
	public void setParcel(String parcel) {
		this.parcel = parcel;
	}
	public String getJobNo() {
		return jobNo;
	}
	public void setJobNo(String jobNo) {
		this.jobNo = jobNo;
	}
	@Override
	public String toString() {
		return "InoutRecord [adrass=" + adrass + ", carNo=" + carNo + ", inTime=" + inTime + ", outTime=" + outTime
				+ ", state=" + state + ", spareField=" + spareField + ", empName=" + empName + ", dept=" + dept
				+ ", parcel=" + parcel + ", jobNo=" + jobNo + ", classType=" + classType + ", inOutCount=" + inOutCount
				+ "]";
	}
	
	


	
}
