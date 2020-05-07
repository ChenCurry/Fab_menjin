package test.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;
import test.domain.Correspondingrelation;

@SuppressWarnings("serial")
public class UserTableModelSet extends AbstractTableModel {
	private UserDao userDao = new UserDaoImpl();
	String[] columnNames = { "序号", "卡号", "工号", "姓名", "部门", "分部", "当前班别"};//增加5列
	// 保存一个User的列表
	private List<Correspondingrelation> users = new ArrayList<Correspondingrelation>();

	// 设置User列表, 同时通知JTabel数据对象更改, 重绘界面
	public void setUsers(List<Correspondingrelation> users) {
		this.users = users;
		this.fireTableDataChanged();// 同时通知JTabel数据对象更改, 重绘界面
	}

	@Override
	public int getColumnCount() {
		return 7;
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
			return users.get(rowIndex).getEmpName();// getEmpName getDept
		}
		case (4): {
			return users.get(rowIndex).getDept();//
		}
		case (5): {
			return users.get(rowIndex).getParcel();//
		}
		case (6): {
			return users.get(rowIndex).getCurrentClassType();//
		}
		default:
			return "sssssss";//
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// 这个函数式设置每个单元格的编辑属性的
		if (5 == columnIndex || 6 == columnIndex) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// 通过rowIndex，获取工号
		String jobNo = this.users.get(rowIndex).getJobNo();
		try {
			if (5 == columnIndex) {// 双击第6列 操作 离开后  配置班别信息
				String value = (aValue+"").trim();
				value = fullWidth2halfWidth(value);
				if(!"".equals(value)) {
					int upCount = userDao.updateEmpParcel(jobNo, value);
					this.users.get(rowIndex).setParcel(value);
					if(upCount>0) {
						JOptionPane.showMessageDialog(null, "成功更新"+jobNo+"的分部");
					}
				}
			}else {
				String value = "";
				Integer val = new Integer((int) aValue);
				switch(val) {
				case 0:
					value = "NOR";
					break;
				case 1:
					value = "NA";
					break;
				case 2:
					value = "NB";
					break;
				case 3:
					value = "DA";
					break;
				case 4:
					value = "DB";
					break;
				}
				Date date = new Date();
				@SuppressWarnings("deprecation")
				int today = date.getDate();
				String time1 = "";//当月
				Calendar cal = Calendar.getInstance();
				cal.add(cal.MONTH, +1);
				SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
				String time3 = dft.format(cal.getTime());
				int count=0;
				if(6 == columnIndex) {
					if (today < 26) {
						//更新或新增 当月
						time1 = dft.format(date);
					}else {
						//当月
						time1 = time3;
					}
					count = userDao.insertEmpClassType(jobNo, value, time1);
					this.users.get(rowIndex).setCurrentClassType(value);
				}
				if(count>0) {
					JOptionPane.showMessageDialog(null, "成功更新"+jobNo+"的班别");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.fireTableCellUpdated(rowIndex, columnIndex);
	}
	
	/**
        * 全角字符串转换半角字符串
	* 
	* @param fullWidthStr
	 *          非空的全角字符串
	* @return 半角字符串
	*/
	private static String fullWidth2halfWidth(String fullWidthStr) {
	   if (null == fullWidthStr || fullWidthStr.length() <= 0) {
	       return "";
	   }
	   char[] charArray = fullWidthStr.toCharArray();
	   //对全角字符转换的char数组遍历
	   for (int i = 0; i < charArray.length; ++i) {
	       int charIntValue = (int) charArray[i];
	       //如果符合转换关系,将对应下标之间减掉偏移量65248;如果是空格的话,直接做转换
	       if (charIntValue >= 65281 && charIntValue <= 65374) {
	           charArray[i] = (char) (charIntValue - 65248);
	       } else if (charIntValue == 12288) {
	           charArray[i] = (char) 32;
	       }
	   }
	   return new String(charArray);
	}

}
