package test.util;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * https://blog.csdn.net/cannel_2020/article/details/7269073
 * @author yu.chen
 *
 */
@SuppressWarnings({ "serial", "rawtypes" })
public class GenderEditor extends JComboBox implements TableCellEditor {
	// EventListenerList:保存EventListener 列表的类。
	private EventListenerList listenerList = new EventListenerList();
	// ChangeEvent用于通知感兴趣的参与者事件源中的状态已发生更改。
	private ChangeEvent changeEvent = new ChangeEvent(this);

	@SuppressWarnings("unchecked")
	public GenderEditor() {
		super();
		addItem("NOR");
		addItem("NA");
		addItem("NB");
		addItem("DA");
		addItem("DB");
	}
	
	/**
	 * 为一个单元格初始化编辑时，getTableCellEditorComponent被调用
	 */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if ("NOR".equals(value)) {
			setSelectedIndex(0);
		} else if ("NA".equals(value)) {
			setSelectedIndex(1);
		} else if ("NB".equals(value)) {
			setSelectedIndex(2);
		} else if ("DA".equals(value)) {
			setSelectedIndex(3);
		} else if ("DB".equals(value)) {
			setSelectedIndex(4);
		}
		return this;
	}

	/**
	 * 编辑其中一个单元格，再点击另一个单元格时，调用。-------------！！！！！
	 */
	public boolean stopCellEditing() {
		fireEditingStopped();// 请求终止编辑操作从JTable获得
		return true;
	}

	/**
	 * 通知编辑器已经结束编辑
	 */
	private void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				// 之所以是i+1，是因为一个为CellEditorListener.class（Class对象），
				// 接着的是一个CellEditorListener的实例
				listener = (CellEditorListener) listeners[i + 1];
				// 让changeEvent去通知编辑器已经结束编辑
				// 在editingStopped方法中，JTable调用getCellEditorValue()取回单元格的值，
				// 并且把这个值传递给TableValues(TableModel)的setValueAt()
				listener.editingStopped(changeEvent);
			}
		}
	}
	
	/**
	 * 返回值传递给TableValue（TableModel）中的setValueAt()方法
	 */
	public Object getCellEditorValue() {
		return new Integer(getSelectedIndex());
	}

	/**
	 * 询问编辑器它是否可以使用 anEvent 开始进行编辑。
	 */
	public boolean isCellEditable(EventObject anEvent) {
		return true;
	}

	/**
	 * 如果应该选择正编辑的单元格，则返回true，否则返回 false。
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	public void cancelCellEditing() {
		
	}
	
	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}

	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}
}
