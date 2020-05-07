package test.domain;

public class FromExcel {
	private String carNo;//卡号
	private String jobNo;//工号
	private String dept;//部门
	private String empName;//姓名
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
	@Override
	public String toString() {
		return "FromExcel [carNo=" + carNo + ", jobNo=" + jobNo + ", dept=" + dept + ", empName=" + empName + "]";
	}
	
}
