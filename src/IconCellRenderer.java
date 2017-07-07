import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by Chris on 2017-07-01.
 */
public class IconCellRenderer extends DefaultTableCellRenderer {

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        /*if(column == MyTableModel.IMAGE_COLUMN){
            String status = (String)value;
            Icon icon = StatusImageUtil.getStatusIcon(status);

            if(icon == null){
                label.setText(status);
            }else{
                label.setIcon(icon);
            }
        }*/
        return label;
    }
}
