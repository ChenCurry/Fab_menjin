package test.util;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import test.domain.AlarmInfo;

@SuppressWarnings("serial")
public class UserTableModelAlarm extends AbstractTableModel{
	String[] columnNames = { "序号", "卡号", "工号", "姓名", "部门","分部", "进出", "通道", "时间" };
	// 保存一个User的列表
	private List<AlarmInfo> users = new ArrayList<AlarmInfo>();
	// 设置User列表, 同时通知JTabel数据对象更改, 重绘界面
	public void setUsers(List<AlarmInfo> users) {
		this.users = users;
		this.fireTableDataChanged();// 同时通知JTabel数据对象更改, 重绘界面
	}
	@Override
	public int getColumnCount() {
		return 9;
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
			return users.get(rowIndex).getEmpName();//getEmpName getDept
		}
		case (4): {
			return users.get(rowIndex).getDept();//
		}
		case (5): {
			return users.get(rowIndex).getParcel();//
		}
		case (6): {
			String flag = users.get(rowIndex).getDwCardReaderNo();
			if("1".equals(flag))
				return "进";//
			else
				return "出";
		}
		case (7): {
			String access = users.get(rowIndex).getAdrass();
			if("192.168.1.154".equals(access)) {
				return "通道一";
			}else if("192.168.1.155".equals(access)) {
				return "通道二";
			}else if("192.168.1.156".equals(access)) {
				return "通道三";
			}
		}
		case (8): {
			return users.get(rowIndex).getStruTime();
		}
		default:
			return "sssssss";// 
		}
	}

}
