import com.table.*;

import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.io.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

public class ColumnTableModel_test {
    List<TableColumn> mockTable(int cols) {
        List list = new ArrayList();
        for(int i=0; i<cols; i++) list.add(mock(TableColumn.class));
        return list;
    }

    @Test
    public void testGetColumnCount() {
        ColumnTableModel ctm = new ColumnTableModel(mockTable(3));

        assertEquals(3, ctm.getColumnCount());
    }
    @Test
    public void testGetRowCount() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(0).getRowCount()).thenReturn(999);
        when(table.get(1).getRowCount()).thenReturn(99);
        when(table.get(2).getRowCount()).thenReturn(3200);

        assertEquals(99, ctm.getRowCount());
    }
    @Test
    public void testGetColumnName() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(0).getName()).thenReturn("Bob");

        assertEquals("Bob", ctm.getColumnName(0));
    }
    @Test
    public void testGetValue() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(2).getValueAt(3)).thenReturn("Value");

        assertEquals("Value", ctm.getValueAt(3,2));
    }
    @Test
    public void testGetColumnClass() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(0).getDataClass()).thenReturn(String.class);

        assertEquals(String.class, ctm.getColumnClass(0));
    }
    @Test
    public void testIsCellEditable() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(1).isRowEditable(0)).thenReturn(true);

        assertEquals(true, ctm.isCellEditable(0,1));
    }
    @Test
    public void testSetValue() {
        List<TableColumn> table = mockTable(3);
        ColumnTableModel ctm = new ColumnTableModel(table);

        when(table.get(1).getDataClass()).thenReturn(String.class);
        when(table.get(1).isRowEditable(0)).thenReturn(true);
        ctm.setValueAt("Value", 0, 1);

        verify(table.get(1)).setValueAt("Value", 0);
    }
}
