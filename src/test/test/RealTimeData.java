package test.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import com.eltima.components.ui.DatePicker;

import test.dao.UserDao;
import test.dao.impl.UserDaoImpl;
import test.domain.AlarmInfo;
import test.domain.Correspondingrelation;
import test.domain.InoutRecord;
import test.util.ExportExcel;
import test.util.GenderEditor;
import test.util.ImportExcel;
import test.util.ReportTimeTableModel10h;
import test.util.ReportTimeTableModel10hNor;
import test.util.ReportTimeTableModel30m;
import test.util.ReportTimeTableModel45m;
import test.util.ReportTimeTableModelLater;
import test.util.ReportTimeTableModelLeave;
import test.util.UserTableModelAlarm;
import test.util.UserTableModelInoutRecord;
import test.util.UserTableModelSet;

@SuppressWarnings("serial")
public class RealTimeData extends JFrame {

	TableModel userTableModel;
	TableModel userTableModel2;
	JScrollPane scrollpane;
	JScrollPane scrollpane2;
	JTable table;
	JTable table2;
	Timer timer;
	Timer timer2;

	JTabbedPane jtbp; // 定义选项卡
	JPanel jp1, jp2, jp21, jp22, jp221, jp23, jp24, jp25, jp252, jp20, jp26, jp3, jp4, jp30, jp31, jp32; // 定义面板
	JTable table3, table4;
	JScrollPane scrollpane3;
	JScrollPane scrollpane4;
	TableModel reportTimeTableModel10h;
	TableModel reportTimeTableModel10hNor;
	TableModel reportTimeTableModelLater;
	TableModel reportTimeTableModelLeave;
	TableModel reportTimeTableModel45m;
	TableModel reportTimeTableModel30m;
	// TableModel userTableModelInOut;
	TableModel userTableModelInoutRecord;
	TableModel userTableModel10h;
	TableModel userTableModel45m;
	TableModel userTableModel30m;
	TableModel userTableModelLater;
	TableModel userTableModelSet;
	final DatePicker datepick11 = getDatePicker();
	final DatePicker datepick12 = getDatePicker();
	@SuppressWarnings("rawtypes")
	JComboBox comboBox;
	@SuppressWarnings("rawtypes")
	JComboBox comboBox2;
	@SuppressWarnings("rawtypes")
	JComboBox comboBox3;
	@SuppressWarnings("rawtypes")
	JComboBox comboBox4;
	JTextField jtfname2;
	List<InoutRecord> inoutRecordListReport;
	JButton buttonQueryInfo;// 全局 方便调用点击

	List<AlarmInfo> alarmInfoList2;
	List<AlarmInfo> alarmInfoList;
	private UserDao userDao = new UserDaoImpl();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RealTimeData(String strr) {
		super(strr);

		jtbp = new JTabbedPane();// 创建选项卡
		jp1 = new JPanel();
		jp2 = new JPanel();
		jp4 = new JPanel();

		jp21 = new JPanel();
		jp22 = new JPanel();
		jp221 = new JPanel();
		jp23 = new JPanel();
		jp24 = new JPanel();
		jp25 = new JPanel();
		jp252 = new JPanel();
		jp20 = new JPanel();
		jp26 = new JPanel();
		jp3 = new JPanel();

		final DatePicker datepick = getDatePicker();
		final DatePicker datepick2 = getDatePicker();

		userTableModel = new UserTableModelAlarm();
		userTableModel2 = new UserTableModelAlarm();
		((UserTableModelAlarm) userTableModel).setUsers(getData());
		((UserTableModelAlarm) userTableModel2).setUsers(getData2());

		table2 = new JTable(userTableModel2);// 进出
		table2.setRowHeight(30);
		table2.getColumnModel().getColumn(0).setPreferredWidth(20);
		table2.getColumnModel().getColumn(1).setPreferredWidth(60);
		table2.getColumnModel().getColumn(2).setPreferredWidth(45);
		table2.getColumnModel().getColumn(3).setPreferredWidth(30);
		table2.getColumnModel().getColumn(4).setPreferredWidth(90);
		table2.getColumnModel().getColumn(5).setPreferredWidth(40);
		table2.getColumnModel().getColumn(6).setPreferredWidth(20);
		table2.getColumnModel().getColumn(7).setPreferredWidth(30);
		table2.getColumnModel().getColumn(8).setPreferredWidth(110);
		table2.setPreferredScrollableViewportSize(new Dimension(650, 500));

		table = new JTable(userTableModel);// 汇总
		table.setRowHeight(30);
		table.getColumnModel().getColumn(0).setPreferredWidth(20);// 序号
		table.getColumnModel().getColumn(1).setPreferredWidth(60);// 卡号
		table.getColumnModel().getColumn(2).setPreferredWidth(45);// 工号
		table.getColumnModel().getColumn(3).setPreferredWidth(30);// 姓名
		table.getColumnModel().getColumn(4).setPreferredWidth(90);// 部门
		table.getColumnModel().getColumn(5).setPreferredWidth(40);// 分部
		table.getColumnModel().getColumn(6).setPreferredWidth(20);// 进出
		table.getColumnModel().getColumn(7).setPreferredWidth(30);// 通道
		table.getColumnModel().getColumn(8).setPreferredWidth(110);// 时间
		table.setPreferredScrollableViewportSize(new Dimension(650, 500));

		// 按钮点击事件监听
		JButton button = new JButton("导出进出记录");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String dateFrom = datepick.getText();
				String dateTo = datepick2.getText();
				try {
					Date date3 = new Date();
					Date date1 = sdf.parse(dateFrom);
					Date date2 = sdf.parse(dateTo);
					int res = date1.compareTo(date2);
					if (res > 0) {
						JOptionPane.showMessageDialog(null, "请正确选择时间:起始时间不能大于截止时间");
					} else if (date1.compareTo(date3) > 0) {
						JOptionPane.showMessageDialog(null, "请正确选择时间:起始时间不能大于当前日期");
					} else {
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportInOutExcel(datepick.getText(), datepick2.getText());
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		});

		// 按钮点击事件监听
		JButton button2 = new JButton("导出Fab内人员名单");
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportExcel exportSummaryExcel = new ExportExcel();
				exportSummaryExcel.exportSummaryExcel();
			}
		});

		// 导入员工资料 按钮
		JButton button3 = new JButton("导入员工资料");
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//密码校验
				/*
				 * String mimakuang = JOptionPane.showInputDialog("请输入密码");
				 * JOptionPane.showMessageDialog(null, "您输入的密码是："+mimakuang);
				 */
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请勿频繁导入数据", "提示", JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					try {
						dealExcel();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "导入员工资料失败，请联系管理员");
					}
				}
			}
		});

		scrollpane = new JScrollPane(table);// huizong
		scrollpane2 = new JScrollPane(table2);// jinchu

		timer = new Timer(1000, new ActionListener() {// 进出记录
			public void actionPerformed(ActionEvent evt) {
				((UserTableModelAlarm) userTableModel).setUsers(getData());
				((UserTableModelAlarm) userTableModel2).setUsers(getData2());
				table.validate();
				table.updateUI();
				table2.validate();
				table2.updateUI();
			}
		});
		timer.start();

		jp1.setBackground(new Color(169, 169, 169));// new Color(169,169,169) new Color(121212)
		jp1.add(new JLabel("--------------------------------------------"
				+ "------------------------>>>>>>实时进出记录<<<<<<------------------------------------------"
				+ "------------------------------------------------------------->>>>>>Fab内实时人员<<<<<<-------------"
				+ "-------------------------------------------------------"));
		jp1.add(scrollpane2);// 进出记录
		jp1.add(scrollpane, BorderLayout.EAST);// 汇总
		jp1.add(datepick, BorderLayout.SOUTH);//
		jp1.add(datepick2, BorderLayout.SOUTH);//
		jp1.add(button, BorderLayout.SOUTH);//
		jp1.add(button2, BorderLayout.SOUTH);//
		jp1.add(button3, BorderLayout.SOUTH);//

		jp2.setBackground(new Color(169, 169, 169));
		jp2.setLayout(new BorderLayout());
		jp20.add("起始时间", datepick11);
		jp20.add("截止时间", datepick12);

		List<String> deptList = new ArrayList<String>();
		deptList = getComboBoxAllDept();
		comboBox = new JComboBox();
		comboBox2 = new JComboBox();
		comboBox.addItem("all fab");
		for (String deptStr : deptList) {
			comboBox.addItem(deptStr);
		}
		comboBox.setName("统计范围");
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int index = comboBox.getSelectedIndex();
				comboBox2.removeAllItems();
				comboBox2.addItem("all");
				if (0 != index) {
					String content = comboBox.getSelectedItem().toString();
					List<String> parcelList = new ArrayList<String>();
					parcelList = getComboBoxParcelDept(content);
					for (String parcelStr : parcelList) {
						comboBox2.addItem(parcelStr);
					}
				}
			}
		});
		jp20.add(comboBox);

		comboBox2.setName("分部");
		jp20.add(comboBox2);
		jtfname2 = new JTextField(9);
		jp20.add(jtfname2);
		jp2.add(jp20, BorderLayout.NORTH);

		// 选项卡二 按钮查询人员进出信息
		JButton buttonInOut = new JButton("查询人员进出信息");
		buttonInOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> listCondition = checkQueryCondition2();
				if (null != listCondition) {
					String timeFrom = listCondition.get(0);
					String timeTo = listCondition.get(1);
					String deptRange = listCondition.get(2);
					String parcelRange = listCondition.get(3);
					String yearMonth = classType_yearMonth(timeFrom);
					String jobNo = listCondition.get(4);
					List<InoutRecord> listInoutRecord = getDataReportInOut(timeFrom, timeTo, deptRange, parcelRange,
							yearMonth, jobNo);
					if (null != listInoutRecord && 0 != listInoutRecord.size()) {
					} else {
						listInoutRecord = new ArrayList<InoutRecord>();
					}
					((UserTableModelInoutRecord) userTableModelInoutRecord).setUsers(listInoutRecord);
					table3.setModel(userTableModelInoutRecord);
					table3.getColumnModel().getColumn(0).setPreferredWidth(20);
					table3.getColumnModel().getColumn(1).setPreferredWidth(100);
					table3.getColumnModel().getColumn(2).setPreferredWidth(100);
					table3.getColumnModel().getColumn(3).setPreferredWidth(60);
					table3.getColumnModel().getColumn(4).setPreferredWidth(160);
					table3.getColumnModel().getColumn(5).setPreferredWidth(50);
					table3.getColumnModel().getColumn(6).setPreferredWidth(25);
					table3.getColumnModel().getColumn(7).setPreferredWidth(150);
					table3.getColumnModel().getColumn(8).setPreferredWidth(150);
					table3.getColumnModel().getColumn(9).setPreferredWidth(50);
					table3.invalidate();
					table3.validate();
					table3.updateUI();
				}
			}
		});
		// 选项卡二 按钮导出人员进出信息
		JButton buttonInOut2 = new JButton("导出人员进出信息");
		buttonInOut2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> listCondition = checkQueryCondition2();
				if (null != listCondition) {
					String timeFrom = listCondition.get(0);
					String timeTo = listCondition.get(1);
					String deptRange = listCondition.get(2);
					String parcelRange = listCondition.get(3);
					String yearMonth = classType_yearMonth(timeFrom);
					String jobNo = listCondition.get(4);
					ExportExcel exportSummaryExcel = new ExportExcel();
					exportSummaryExcel.exportReportInOutExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
							jobNo);
				}
			}
		});
		jp21.add(buttonInOut);
		jp21.add(buttonInOut2);
		jp26.add(jp21);

		// 选项卡二 按钮查询FAB时数小于10小时
		JButton button10h = new JButton("查询FAB时数");
		button10h.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/**
				 * 这里边需要做的是：针对限定条件下已有记录的统计 限定条件：时间（请选择某月26日至次月25日） 前提：请要查询人员的班别已经配置
				 */
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						List<InoutRecord> list10h = getDataReport10h(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != list10h && 0 != list10h.size()) {
						} else {
							list10h = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModel10h) reportTimeTableModel10h).setUsers(list10h);
						table3.setModel(reportTimeTableModel10h);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(60);
						table3.getColumnModel().getColumn(11).setPreferredWidth(20);
						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出FAB时数小于10小时
		JButton button10h2 = new JButton("导出FAB时数");
		button10h2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReport10hExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp22.add(button10h);
		jp22.add(button10h2);
		jp26.add(jp22);
		
		// 选项卡二 按钮查询FAB时数 (常日班)
		JButton button10hNor = new JButton("查询FAB时数(常日班)");
		button10hNor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/**
				 * 这里边需要做的是：针对限定条件下已有记录的统计 限定条件：时间（请选择某月26日至次月25日） 前提：请要查询人员的班别已经配置
				 */
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						List<InoutRecord> list10h = getDataReport10hNor(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != list10h && 0 != list10h.size()) {
						} else {
							list10h = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModel10hNor) reportTimeTableModel10hNor).setUsers(list10h);
						table3.setModel(reportTimeTableModel10hNor);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(60);
						table3.getColumnModel().getColumn(11).setPreferredWidth(20);
						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出FAB时数  (常日班)
		JButton button10h2Nor = new JButton("导出FAB时数(常日班)");
		button10h2Nor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReport10hExcelNor(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp221.add(button10hNor);
		jp221.add(button10h2Nor);
		jp26.add(jp221);

		// 选项卡二 按钮查询迟到
		JButton buttonLater = new JButton("查询迟到");
		buttonLater.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						List<InoutRecord> listLater = getDataReportLater(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != listLater && 0 != listLater.size()) {
						} else {
							listLater = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModelLater) reportTimeTableModelLater).setUsers(listLater);
						table3.setModel(reportTimeTableModelLater);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(60);
						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出迟到
		JButton buttonLater2 = new JButton("导出迟到");
		buttonLater2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						// List<InoutRecord> listLater = getDataReportLater(timeFrom, timeTo,
						// deptRange,parcelRange,yearMonth);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReportLaterExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp25.add(buttonLater);
		jp25.add(buttonLater2);
		jp26.add(jp25);

		// 选项卡二 按钮查询早退
		JButton buttonLeave = new JButton("查询早退");
		buttonLeave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						List<InoutRecord> listLeave = getDataReportLeave(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != listLeave && 0 != listLeave.size()) {
						} else {
							listLeave = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModelLeave) reportTimeTableModelLeave).setUsers(listLeave);
						table3.setModel(reportTimeTableModelLeave);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(60);
						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出早退
		JButton buttonLeave2 = new JButton("导出早退");
		buttonLeave2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReportLeaveExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp252.add(buttonLeave);
		jp252.add(buttonLeave2);
		jp26.add(jp252);

		// 选项卡二 按钮查询吃饭时间大于45分钟
		JButton button45m = new JButton("查询吃饭时间大于45分钟");
		button45m.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						List<InoutRecord> list45m = getDataReport45m(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != list45m && 0 != list45m.size()) {
						} else {
							list45m = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModel45m) reportTimeTableModel45m).setUsers(list45m);
						table3.setModel(reportTimeTableModel45m);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(40);
						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出吃饭时间大于45分钟
		JButton button45m2 = new JButton("导出吃饭时间大于45分钟");
		button45m2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReport45mExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp23.add(button45m);
		jp23.add(button45m2);
		jp26.add(jp23);

		// 选项卡二 按钮查询外出时间大于30分钟
		JButton button30m = new JButton("查询外出时间大于30分钟");
		button30m.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String jobNo = listCondition.get(4);
						String yearMonth = classType_yearMonth(timeFrom);
						List<InoutRecord> list30m = getDataReport30m(timeFrom, timeTo, deptRange, parcelRange,
								yearMonth, jobNo);
						if (null != list30m && 0 != list30m.size()) {
						} else {
							list30m = new ArrayList<InoutRecord>();
						}
						((ReportTimeTableModel30m) reportTimeTableModel30m).setUsers(list30m);
						table3.setModel(reportTimeTableModel30m);
						table3.getColumnModel().getColumn(0).setPreferredWidth(20);
						table3.getColumnModel().getColumn(1).setPreferredWidth(80);
						table3.getColumnModel().getColumn(2).setPreferredWidth(80);
						table3.getColumnModel().getColumn(3).setPreferredWidth(60);
						table3.getColumnModel().getColumn(4).setPreferredWidth(140);
						table3.getColumnModel().getColumn(5).setPreferredWidth(40);
						table3.getColumnModel().getColumn(6).setPreferredWidth(25);
						table3.getColumnModel().getColumn(7).setPreferredWidth(40);
						table3.getColumnModel().getColumn(8).setPreferredWidth(100);
						table3.getColumnModel().getColumn(9).setPreferredWidth(100);
						table3.getColumnModel().getColumn(10).setPreferredWidth(50);

						table3.invalidate();
						table3.validate();
						table3.updateUI();// scrollpane3 jp3 jp2
					}
				}
			}
		});
		// 选项卡二 按钮导出外出时间大于30分钟
		JButton button30m2 = new JButton("导出外出时间大于30分钟");
		button30m2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int isDelete = JOptionPane.showConfirmDialog(null, "请确保该时间段内/所需查询员工的所有班别在系统中已经配置", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					List<String> listCondition = checkQueryCondition2();
					if (null != listCondition) {
						String timeFrom = listCondition.get(0);
						String timeTo = listCondition.get(1);
						String deptRange = listCondition.get(2);
						String parcelRange = listCondition.get(3);
						String yearMonth = classType_yearMonth(timeFrom);
						String jobNo = listCondition.get(4);
						ExportExcel exportSummaryExcel = new ExportExcel();
						exportSummaryExcel.exportReport30mExcel(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
								jobNo);
					}
				}
			}
		});
		jp24.add(button30m);
		jp24.add(button30m2);
		jp26.add(jp24);

		jp2.add(jp26, BorderLayout.CENTER);// NORTH WEST

		userTableModelInoutRecord = new UserTableModelInoutRecord();
		reportTimeTableModel10h = new ReportTimeTableModel10h();
		reportTimeTableModel10hNor = new ReportTimeTableModel10hNor();
		reportTimeTableModelLater = new ReportTimeTableModelLater();
		reportTimeTableModelLeave = new ReportTimeTableModelLeave();
		reportTimeTableModel45m = new ReportTimeTableModel45m();
		reportTimeTableModel30m = new ReportTimeTableModel30m();
		table3 = new JTable();
		table3.setModel(userTableModelInoutRecord);// 初始设定
		table3.setRowHeight(30);
		table3.setPreferredScrollableViewportSize(new Dimension(1000, 530));

		scrollpane3 = new JScrollPane(table3);
		jp3.add(scrollpane3);
		jp2.add(jp3, BorderLayout.SOUTH);

		jtbp.add("实时面板", jp1);// 创建面板
		jtbp.add("统计面板", jp2);
		jtbp.add("分部/班别", jp4);
		jtbp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (2 == ((JTabbedPane) e.getSource()).getSelectedIndex()) {
					// 意义：不在初始化的时候去加载这个页面，避免卡顿 这些事放到方法中去做
					loadEditPane();
				}
			}
		});
		this.add(jtbp);// 添加选项卡窗格到容器
	}

	/**
	 * 导入excel
	 * 
	 * @throws IOException
	 */
	private void dealExcel() throws IOException {
		JFileChooser chooser = new JFileChooser();
		// 当前系统
		chooser.showOpenDialog(this);
		chooser.setVisible(true);
		File file = chooser.getSelectedFile();
		if (null != file) {
			String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if (!"xlsx".equals(fileType) && !"xls".equals(fileType)) {
				JOptionPane.showMessageDialog(null, "请导入excel文件");
			} else {
				ImportExcel importExcel = new ImportExcel();
				try {
					List<String> xx = importExcel.frameImportExcel(file);
					if (null == xx) {
						JOptionPane.showMessageDialog(null, "excel文件有问题，请重新制作");
					} else {
						JOptionPane.showMessageDialog(null, "已成功录入" + xx.get(0) + "条记录," + xx.get(1) + "条数据作更新");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 当月班别导入
	 * 
	 * @throws IOException
	 */
	private void dealExcel2() throws IOException {
		JFileChooser chooser = new JFileChooser();
		// 当前系统
		chooser.showOpenDialog(this);
		chooser.setVisible(true);
		File file = chooser.getSelectedFile();
		if (null != file) {
			String fileType = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if (!"xlsx".equals(fileType) && !"xls".equals(fileType)) {
				JOptionPane.showMessageDialog(null, "请导入excel文件");
			} else {
				ImportExcel importExcel = new ImportExcel();
				try {
					List<String> xx = importExcel.f_classTypeImportExcel(file);
					if (null == xx) {
						JOptionPane.showMessageDialog(null, "excel文件有问题，请重新制作");
					} else {
						if (xx.get(0).equals("error0")) {
							JOptionPane.showMessageDialog(null, "第" + xx.get(1) + "条数据班别信息有误");
						} else {
							JOptionPane.showMessageDialog(null, "已成功插入:" + xx.get(0));
							// 点击按钮 刷新table
							buttonQueryInfo.doClick();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 时间控件
	 * 
	 * @return
	 */
	private static DatePicker getDatePicker() {
		final DatePicker datepick;
		// 格式
		String DefaultFormat = "yyyy-MM-dd";
		// 当前时间
		Date date = new Date();
		// 字体
		Font font = new Font("Times New Roman", Font.BOLD, 14);
		Dimension dimension = new Dimension(100, 24);// 177
		// 构造方法（初始时间，时间显示格式，字体，控件大小）
		datepick = new DatePicker(date, DefaultFormat, font, dimension);
		datepick.setLocation(137, 83);// 设置起始位置
		// 设置国家
		datepick.setLocale(Locale.CHINA);
		// 设置时钟面板不可见
		datepick.setTimePanleVisible(false);
		return datepick;
	}

	/**
	 * 取所有汇总信息
	 * 
	 * @return
	 */
	public List<AlarmInfo> getData() {
		try {
			alarmInfoList = userDao.querySummaryAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return alarmInfoList;
	}

	/**
	 * 二十条 进出记录
	 * 
	 * @return
	 */
	public List<AlarmInfo> getData2() {
		try {
			alarmInfoList2 = userDao.queryInOutRecorder();
		} catch (SQLException e1) {
			System.out.println("查 二十条 进出记录 失败");
			e1.printStackTrace();
		}
		return alarmInfoList2;
	}

	/**
	 * 选项卡二 查询进出记录
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @return
	 */
	public List<InoutRecord> getDataReportInOut(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReportInOutNew(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
					jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询进出记录 失败");
			e1.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 构造班别的月份
	 * 
	 * @param timeFrom
	 * @return
	 */
	public String classType_yearMonth(String timeFrom) {
		String yearMonth = "";
		String str = timeFrom.substring(8, 9);
		if ("0".equals(str)) {
			str = timeFrom.substring(9, 10);
		} else {
			str = timeFrom.substring(8, 10);
		}
		if (Integer.parseInt(str) < 26) {
			yearMonth = timeFrom.substring(0, 7);
		} else {
			// 次月
			yearMonth = getPreMonth(timeFrom);
		}
		return yearMonth;
	}

	/**
	 * 选项卡二 查询Fab时数小于10小时
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @return
	 */
	public List<InoutRecord> getDataReport10h(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReport10h2(timeFrom, timeTo, deptRange, parcelRange, yearMonth, jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询10h 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询10h 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}
	
	/***
	 * 新需求，统计常日班 fab时长
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @param jobNo
	 * @return
	 */
	public List<InoutRecord> getDataReport10hNor(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReport10h2Nor(timeFrom, timeTo, deptRange, parcelRange, yearMonth, jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询10h (常日班) 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询10h (常日班) 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 选项卡二 查询迟到人员信息
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @return
	 */
	public List<InoutRecord> getDataReportLater(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReportLater2(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
					jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询迟到人员信息 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询迟到人员信息 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 选项卡二 查询早退人员信息
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @return
	 */
	public List<InoutRecord> getDataReportLeave(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReportLeave2(timeFrom, timeTo, deptRange, parcelRange, yearMonth,
					jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询早退人员信息 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询早退人员信息 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 选项卡二 查询用餐时间超过45分 绳之以法
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @return
	 */
	public List<InoutRecord> getDataReport45m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReport45m(timeFrom, timeTo, deptRange, parcelRange, yearMonth, jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询吃饭时间超过45min人员信息 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询吃饭时间超过45min人员信息 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 选项卡二 查询用餐时间超过30分 绳之以法
	 * 
	 * @param timeFrom
	 * @param timeTo
	 * @param deptRange
	 * @param parcelRange
	 * @param yearMonth
	 * @return
	 */
	public List<InoutRecord> getDataReport30m(String timeFrom, String timeTo, String deptRange, String parcelRange,
			String yearMonth, String jobNo) {
		try {
			inoutRecordListReport = userDao.queryReport30m(timeFrom, timeTo, deptRange, parcelRange, yearMonth, jobNo);
		} catch (SQLException e1) {
			System.out.println("选项卡二 查询外出时间超过30min人员信息 失败");
			e1.printStackTrace();
		} catch (ParseException e) {
			System.out.println("选项卡二 查询外出时间超过30min人员信息 失败2");
			e.printStackTrace();
		}
		return inoutRecordListReport;
	}

	/**
	 * 限制查询的条件
	 * 
	 * @return
	 */
	private List<String> checkQueryCondition2() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateFrom = datepick11.getText();
		String dateTo = datepick12.getText();
		Date date3 = new Date();
		Date date1;
		try {
			date1 = sdf.parse(dateFrom);
			Date date2 = sdf.parse(dateTo);
			int res = date1.compareTo(date2);
			if (res > 0) {
				JOptionPane.showMessageDialog(null, "请正确选择时间:起始时间不能大于截止时间");
				return null;
			} else if (date1.compareTo(date3) > 0) {
				JOptionPane.showMessageDialog(null, "请正确选择时间:起始时间不能大于当前日期");
				return null;
			}
			// 如果起始时间小于26号 那么截止时间也需要小于该月的26号
			// 如果起始时间大于25号 那么截止时间需要小于次月的26号
			String str = dateFrom.substring(8, 10);
			String str2 = str.substring(0, 1);
			if ("0".equals(str2) || (!"0".equals(str2) && Integer.parseInt(str) < 26)) {
				String str3 = dateFrom.substring(0, 8) + "25";
				Date date4 = sdf.parse(str3);
				Date date5 = sdf.parse(dateTo);
				int res2 = date5.compareTo(date4);
				if (res2 > 0) {
					JOptionPane.showMessageDialog(null, "截止时间请勿超过当月25日");
					return null;
				}
			}
			String str4 = dateFrom.substring(0, 8) + "25";
			Date date6 = sdf.parse(str4);
			int res3 = date1.compareTo(date6);
			if (res3 > 0) {
				String str5 = getPreMonth(dateFrom) + "-25";
				Date date7 = sdf.parse(str5);
				int res4 = date2.compareTo(date7);
				if (res4 > 0) {
					JOptionPane.showMessageDialog(null, "截止时间请勿超过次月25日");
					return null;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<String> listCondition = new ArrayList<String>();
		listCondition.add(dateFrom);
		listCondition.add(dateTo);
		listCondition.add((String) comboBox.getSelectedItem());
		listCondition.add((String) comboBox2.getSelectedItem());
		listCondition.add(fullWidth2halfWidth(jtfname2.getText().trim()));
		return listCondition;
	}
	
	/**
	 * 查询所有的部门下拉选项
	 * 
	 * @return
	 */
	public List<String> getComboBoxAllDept() {
		List<String> deptList = new ArrayList<String>();
		List<Correspondingrelation> correspondingrelationList = new ArrayList<Correspondingrelation>();
		try {
			correspondingrelationList = userDao.queryComboBoxAllDept();
			for (Correspondingrelation correspondingrelation : correspondingrelationList) {
				deptList.add(correspondingrelation.getDept());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return deptList;
	}

	/**
	 * 查询分部下拉框
	 * 
	 * @return
	 */
	public List<String> getComboBoxParcelDept(String dept) {
		List<String> parcelList = new ArrayList<String>();
		List<Correspondingrelation> correspondingrelationList = new ArrayList<Correspondingrelation>();
		try {
			correspondingrelationList = userDao.queryComboBoxParcelDept(dept);
			for (Correspondingrelation correspondingrelation : correspondingrelationList) {
				parcelList.add(correspondingrelation.getParcel());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return parcelList;
	}

	/**
	 * 得到下月字符串
	 * 
	 * @param repeatDate
	 * @return
	 */
	public static String getPreMonth(String repeatDate) {
		String lastMonth = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
		int year = Integer.parseInt(repeatDate.substring(0, 4));
		String monthsString = repeatDate.substring(5, 7);
		int month;
		if ("0".equals(monthsString.substring(0, 1))) {
			month = Integer.parseInt(monthsString.substring(1, 2));
		} else {
			month = Integer.parseInt(monthsString.substring(0, 2));
		}
		cal.set(year, month, Calendar.DATE);
		lastMonth = dft.format(cal.getTime());
		return lastMonth;
	}

	/**
	 * 加载 分部/班别 选项卡
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadEditPane() {
		jp4.removeAll();
		jp30 = new JPanel();
		jp31 = new JPanel();
		jp32 = new JPanel();
		table4 = new JTable();
		userTableModelSet = new UserTableModelSet();

		List<String> deptList2 = new ArrayList<String>();
		deptList2 = getComboBoxAllDept();
		comboBox3 = new JComboBox();
		comboBox4 = new JComboBox();
		JTextField jtfname = new JTextField(9);
		jtfname.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					buttonQueryInfo.doClick();
				}
			}
		});
		// jtfname.setHorizontalAlignment(JTextField.LEFT);
		// jtfname.setBounds(140, 90, 120, 30);
		comboBox3.addItem("all fab");
		for (String deptStr : deptList2) {
			comboBox3.addItem(deptStr);
		}
		comboBox3.setName("查询范围");
		comboBox3.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int index = comboBox3.getSelectedIndex();
				comboBox4.removeAllItems();
				comboBox4.addItem("all");
				if (0 != index) {
					String content = comboBox3.getSelectedItem().toString();
					List<String> parcelList = new ArrayList<String>();
					parcelList = getComboBoxParcelDept(content);
					for (String parcelStr : parcelList) {
						comboBox4.addItem(parcelStr);
					}
				}
			}
		});

		// 按钮查询人员信息 设置
		buttonQueryInfo = new JButton("查询");
		buttonQueryInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableModel userTableModelSet = new UserTableModelSet();
				String empDept = (String) comboBox3.getSelectedItem();
				String empParcel = (String) comboBox4.getSelectedItem();
				String jobNoJTF = jtfname.getText().trim();
				jobNoJTF = fullWidth2halfWidth(jobNoJTF);
				if (null != empParcel && !("".equals(empParcel))) {
				} else {
					empParcel = "all";
				}
				// JOptionPane.showMessageDialog(null,
				// "empDept:"+empDept+",empParcel:"+empParcel+",jobNoJTF:"+jobNoJTF);
				List<Correspondingrelation> CorrespondList = getDataEmpInfo(empDept, empParcel, jobNoJTF);
				((UserTableModelSet) userTableModelSet).setUsers(CorrespondList);
				table4.setModel(userTableModelSet);
				table4.getColumnModel().getColumn(0).setPreferredWidth(20);
				table4.getColumnModel().getColumn(1).setPreferredWidth(100);
				table4.getColumnModel().getColumn(2).setPreferredWidth(100);
				table4.getColumnModel().getColumn(3).setPreferredWidth(60);
				table4.getColumnModel().getColumn(4).setPreferredWidth(160);
				table4.getColumnModel().getColumn(5).setPreferredWidth(60);// 分部
				table4.getColumnModel().getColumn(6).setPreferredWidth(50);
				table4.invalidate();
				table4.validate();
				table4.updateUI();//

				table4.getColumnModel().getColumn(6).setCellEditor(new GenderEditor());
			}
		});

		jp30.add(comboBox3);
		comboBox4.setName("分部");
		jp30.add(comboBox4);

		jp30.add(jtfname);
		jp30.add(buttonQueryInfo);
		// 按钮导出
		JButton buttonEmpInfo = new JButton("导出");
		buttonEmpInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String empDept = (String) comboBox3.getSelectedItem();
				String empParcel = (String) comboBox4.getSelectedItem();
				String jobNoJTF = jtfname.getText().trim();
				jobNoJTF = fullWidth2halfWidth(jobNoJTF);
				if (null != empParcel && !("".equals(empParcel))) {
				} else {
					empParcel = "all";
				}

				ExportExcel exportSummaryExcel = new ExportExcel();
				exportSummaryExcel.exportEmpInfo(empDept, empParcel, jobNoJTF);
			}
		});
		jp30.add(buttonEmpInfo);
		// 导入员工当月班别
		JButton buttonSetEmpClassInfo = new JButton("导入");
		buttonSetEmpClassInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 添加确认提示框，会返回一个整数
				int isDelete = JOptionPane.showConfirmDialog(null, "你需要导入当月的员工班别信息？", "提示",
						JOptionPane.YES_NO_CANCEL_OPTION);
				// 如果这个整数等于JOptionPane.YES_OPTION，则说明你点击的是“确定”按钮，则允许继续操作，否则结束
				if (isDelete == JOptionPane.YES_OPTION) {
					try {
						dealExcel2();
					} catch (IOException e1) {
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "导入当月的员工班别信息失败，请联系管理员");
					}
				}
			}
		});
		jp30.add(buttonSetEmpClassInfo);
		jp32.add(jp30);
		jp4.add(jp32, BorderLayout.NORTH);
		table4.setModel(userTableModelSet);
		table4.setRowHeight(30);
		table4.setPreferredScrollableViewportSize(new Dimension(1200, 560));

		scrollpane4 = new JScrollPane(table4);
		jp31.add(scrollpane4);
		jp4.add(jp31, BorderLayout.SOUTH);// SOUTH CENTER NORTH EAST WEST
	}

	/**
	 * 分部、班别 table加载 变更后
	 * 
	 * @param empDept
	 * @param empParcel
	 * @return
	 */
	@SuppressWarnings("static-access")
	protected List<Correspondingrelation> getDataEmpInfo(String empDept, String empParcel, String jobNoJTF) {
		Date date = new Date();
		@SuppressWarnings("deprecation")
		int today = date.getDate();
		String time1 = "";// 当月
		Calendar cal = Calendar.getInstance();
		cal.add(cal.MONTH, +1);
		SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
		String time3 = dft.format(cal.getTime());
		if (today < 26) {// 每月26号后 算作下一月的班别
			time1 = dft.format(date);// 当月
		} else {
			time1 = time3;// 当月
		}
		List<Correspondingrelation> correspondingrelationList = new ArrayList<Correspondingrelation>();
		try {
			correspondingrelationList = userDao.getDataEmpInfo2(empDept, empParcel, time1, jobNoJTF);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return correspondingrelationList;
	}

	/**
	 * 全角字符串转换半角字符串
	 * 
	 * @param fullWidthStr 非空的全角字符串
	 * @return 半角字符串
	 */
	private static String fullWidth2halfWidth(String fullWidthStr) {
		if (null == fullWidthStr || fullWidthStr.length() <= 0) {
			return "";
		}
		char[] charArray = fullWidthStr.toCharArray();
		// 对全角字符转换的char数组遍历
		for (int i = 0; i < charArray.length; ++i) {
			int charIntValue = (int) charArray[i];
			// 如果符合转换关系,将对应下标之间减掉偏移量65248;如果是空格的话,直接做转换
			if (charIntValue >= 65281 && charIntValue <= 65374) {
				charArray[i] = (char) (charIntValue - 65248);
			} else if (charIntValue == 12288) {
				charArray[i] = (char) 32;
			}
		}
		return new String(charArray);
	}

	public static void main(String[] g) {
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.getImage("logo.jpg");
		RealTimeData rFrame = new RealTimeData("CQAOS门禁数据统计系统");
		rFrame.setIconImage(img);
		rFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rFrame.setSize(new Dimension(1400, 750));
		rFrame.setResizable(false);// 设置大小不可变
		rFrame.setVisible(true);
	}
}
