package test.domain;

public class Correspondingrelation {

	private String carNo;//卡号
	private String jobNo;//工号
	private String dept;//部门 组织
	private String empName;//姓名
	private String parcel;//分部
	private String state;//状态
	private String spare;//备用字段
	private String previousClassType3;//
	private String previousClassType2;//
	private String previousClassType1;//
	private String currentClassType;//当前班别
	private String nextClassType;//下月班别   这个不用了
	private String nextClassType1;//
	private String nextClassType2;//
	private String nextClassType3;//
	private Integer total;//条数
	
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
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
	public String getCarNo() {
		return carNo;
	}
	public void setCarNo(String carNo) {
		this.carNo = carNo;
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
	public String getParcel() {
		return parcel;
	}
	public void setParcel(String parcel) {
		this.parcel = parcel;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getSpare() {
		return spare;
	}
	public void setSpare(String spare) {
		this.spare = spare;
	}
	public String getPreviousClassType3() {
		return previousClassType3;
	}
	public void setPreviousClassType3(String previousClassType3) {
		this.previousClassType3 = previousClassType3;
	}
	public String getPreviousClassType2() {
		return previousClassType2;
	}
	public void setPreviousClassType2(String previousClassType2) {
		this.previousClassType2 = previousClassType2;
	}
	public String getPreviousClassType1() {
		return previousClassType1;
	}
	public void setPreviousClassType1(String previousClassType1) {
		this.previousClassType1 = previousClassType1;
	}
	public String getNextClassType1() {
		return nextClassType1;
	}
	public void setNextClassType1(String nextClassType1) {
		this.nextClassType1 = nextClassType1;
	}
	public String getNextClassType2() {
		return nextClassType2;
	}
	public void setNextClassType2(String nextClassType2) {
		this.nextClassType2 = nextClassType2;
	}
	public String getNextClassType3() {
		return nextClassType3;
	}
	public void setNextClassType3(String nextClassType3) {
		this.nextClassType3 = nextClassType3;
	}
	@Override
	public String toString() {
		return "Correspondingrelation [carNo=" + carNo + ", jobNo=" + jobNo + ", dept=" + dept + ", empName=" + empName
				+ ", parcel=" + parcel + ", state=" + state + ", spare=" + spare + ", previousClassType3="
				+ previousClassType3 + ", previousClassType2=" + previousClassType2 + ", previousClassType1="
				+ previousClassType1 + ", currentClassType=" + currentClassType + ", nextClassType=" + nextClassType
				+ ", nextClassType1=" + nextClassType1 + ", nextClassType2=" + nextClassType2 + ", nextClassType3="
				+ nextClassType3 + ", total=" + total + "]";
	}
}
