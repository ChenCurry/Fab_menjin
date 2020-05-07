package test.util;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings({ "rawtypes", "serial" })
public class GenderRenderer extends JComboBox implements TableCellRenderer {
	@SuppressWarnings("unchecked")
	public GenderRenderer() {
		super();
		addItem("NOR");
		addItem("NA");
		addItem("NB");
		addItem("DA");
		addItem("DB");
		addItem("未设定");
//		addItem("");
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		if (isSelected) {
			setForeground(table.getForeground());
			super.setBackground(table.getBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		if ("NOR".equals(value)) {//数字代表上边addItem("")的项
			setSelectedIndex(0);
		} else if ("NA".equals(value)) {
			setSelectedIndex(1);
		} else if ("NB".equals(value)) {
			setSelectedIndex(2);
		} else if ("DA".equals(value)) {
			setSelectedIndex(3);
		} else if ("DB".equals(value)) {
			setSelectedIndex(4);
		} else if (null==value||"".equals(value)) {
			setSelectedIndex(5);
		}
		return this;
	}
}
