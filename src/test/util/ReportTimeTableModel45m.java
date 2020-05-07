package test.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import test.domain.InoutRecord;

@SuppressWarnings("serial")
public class ReportTimeTableModel45m extends AbstractTableModel{
	String[] columnNames = { "序号", "卡号", "工号", "姓名", "部门","分部","班别", "通道", "出去时间","归来时间", "用餐分钟数"};
	// 保存一个列表
	private List<InoutRecord> inoutRecordList = new ArrayList<InoutRecord>();
	// 设置reportTimeList列表, 同时通知JTabel数据对象更改, 重绘界面
	public void setUsers(List<InoutRecord> list) {
		this.inoutRecordList = list;
		this.fireTableDataChanged();// 同时通知JTabel数据对象更改, 重绘界面
	}
	@Override
	public int getColumnCount() {
		return 11;
	}

	@Override
	public int getRowCount() {
		return inoutRecordList.size();
	}
	
	public String getColumnName(int col) {
		return columnNames[col].toString();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case (0): {
			return rowIndex + 1;
		}
		case (1): {
			return inoutRecordList.get(rowIndex).getCarNo();//
		}
		case (2): {
			return inoutRecordList.get(rowIndex).getJobNo();
		}
		case (3): {
			return inoutRecordList.get(rowIndex).getEmpName();//getEmpName getDept
		}
		case (4): {
			return inoutRecordList.get(rowIndex).getDept();//
		}
		case (5): {
			return inoutRecordList.get(rowIndex).getParcel();
		}
		case (6): {
			return inoutRecordList.get(rowIndex).getClassType();
		}
		case (7): {
			String access = inoutRecordList.get(rowIndex).getAdrass();
			if("192.168.1.154".equals(access)) {
				return "通道一";
			}else if("192.168.1.155".equals(access)) {
				return "通道二";
			}else if("192.168.1.156".equals(access)) {
				return "通道三";
			}
		}
		case (8): {
			return inoutRecordList.get(rowIndex).getOutTime();
		}
		case (9): {
			return inoutRecordList.get(rowIndex).getInTime();
		}
		case (10): {
			return inoutRecordList.get(rowIndex).getSpareField();
		}
		default:
			return "sssssss";// 
		}
	}

}
