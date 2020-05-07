package test.domain;

public class ClassType {

	private String id_ct;
	private String jobNo;
	private String spare;
	private String yearMonth;
	private String classType;
	private String state;
	
	public String getId_ct() {
		return id_ct;
	}
	public void setId_ct(String id_ct) {
		this.id_ct = id_ct;
	}
	public String getJobNo() {
		return jobNo;
	}
	public void setJobNo(String jobNo) {
		this.jobNo = jobNo;
	}
	public String getSpare() {
		return spare;
	}
	public void setSpare(String spare) {
		this.spare = spare;
	}
	public String getYearMonth() {
		return yearMonth;
	}
	public void setYearMonth(String yearMonth) {
		this.yearMonth = yearMonth;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return "ClassType [id_ct=" + id_ct + ", jobNo=" + jobNo + ", spare=" + spare + ", yearMonth=" + yearMonth
				+ ", classType=" + classType + ", state=" + state + "]";
	}
	
}
