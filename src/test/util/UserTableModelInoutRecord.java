package test.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import test.domain.InoutRecord;

@SuppressWarnings("serial")
public class UserTableModelInoutRecord extends AbstractTableModel{
	String[] columnNames = { "序号", "卡号", "工号", "姓名", "部门","分部","班别", "进", "出", "通道" };
	// 保存一个User的列表
	private List<InoutRecord> users = new ArrayList<InoutRecord>();
	// 设置User列表, 同时通知JTabel数据对象更改, 重绘界面
	public void setUsers(List<InoutRecord> users) {
		this.users = users;
		this.fireTableDataChanged();// 同时通知JTabel数据对象更改, 重绘界面
	}
	@Override
	public int getColumnCount() {
		return 10;
	}

	@Override
	public int getRowCount() {
		return users.size();
	}
	
	@Override
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
			return users.get(rowIndex).getCarNo();//
		}
		case (2): {
			return users.get(rowIndex).getJobNo();
		}
		case (3): {
			return users.get(rowIndex).getEmpName();
		}
		case (4): {
			return users.get(rowIndex).getDept();//
		}
		case (5): {
			return users.get(rowIndex).getParcel();//
		}
		case (6): {
			return users.get(rowIndex).getClassType();//
		}
		case (7): {
			return users.get(rowIndex).getInTime();
		}
		case (8): {
			return users.get(rowIndex).getOutTime();
		}
		case (9): {
			String access = users.get(rowIndex).getAdrass();
			if("192.168.1.154".equals(access)) {
				return "通道一";
			}else if("192.168.1.155".equals(access)) {
				return "通道二";
			}else if("192.168.1.156".equals(access)) {
				return "通道三";
			}
		}
		default:
			return "sssssss";// 
		}
	}

}
