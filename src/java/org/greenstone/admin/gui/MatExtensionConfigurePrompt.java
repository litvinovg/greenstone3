package org.greenstone.admin.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import org.greenstone.admin.Configuration;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class MatExtensionConfigurePrompt extends JFrame {

    JPanel main_pane;
    JScrollPane table_pane;
    JTable configure_table;
    
    JButton save_button;
    JButton cancel_button;
    JPanel control_pane;
    JPanel button_pane;
	
    JLabel prompt_title;
    JPanel label_pane;
    
    String heading;
    String destination;

    static final String Setting = "setting";
    static final String Value = "value";
    static final String Property = "property";
    
    Object[][] data;
    String title;
    
    public MatExtensionConfigurePrompt(Object[][] o, String directory, String extensionName, String titleString){

	String[] columnNames = {"Setting","Value"};

	title = titleString;
	data = o;
	destination = directory;
	
	main_pane = new JPanel();
	main_pane.setBorder(BorderFactory.createLoweredBevelBorder());
	main_pane.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	main_pane.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	
	configure_table = new JTable(new MyTableModel(columnNames, data)){		
		public Component prepareRenderer (TableCellRenderer renderer, int rowIndex, int colIndex){
		    Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
		    if (colIndex == 0) {
			c.setBackground(Configuration.getColor("coloring.table_noneditable_background"));
		    } else {
			c.setBackground(Configuration.getColor("coloring.table_editable_background"));
		    }
		    
		    return c;
		}};
	configure_table.setIntercellSpacing(new Dimension(ExtPane.left_padding,0));
	table_pane = new JScrollPane(configure_table);
	table_pane.setVisible(true);
	
	save_button = new JButton("Save");
	save_button.addActionListener(new Save_button_Adapter(this));
	
	cancel_button= new JButton("Cancel");
	cancel_button.addActionListener(new Cancel_button_Adapter(this));
	button_pane = new JPanel();
	button_pane.setLayout(new GridLayout(1,2));
	button_pane.add(save_button);
	button_pane.add(cancel_button);
	
	control_pane = new JPanel();
	control_pane.setLayout (new BorderLayout());
	control_pane.setBorder(BorderFactory.createEmptyBorder(05,10,5,10));
	control_pane.add (button_pane, BorderLayout.CENTER);
	
	prompt_title = new JLabel();
	prompt_title.setOpaque(true);
	prompt_title.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	prompt_title.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	prompt_title.setText(extensionName);
	prompt_title.setBorder(BorderFactory.createEmptyBorder(0, ExtPane.left_padding, 0, 0));
    }
    
    
    class MyTableModel extends AbstractTableModel {
	
    	private String [] columnNames;
    	private Object[][] data;
	
    	public MyTableModel(String[] s , Object[][]o){
	    columnNames = s;
	    data = o;
    	}
	
        public int getColumnCount() {
            return columnNames.length;
        }
	
        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }


        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

	public boolean isCellEditable(int row, int col) {
	    if (col < 1) {
                return false;
            } else {
                return true;
            }
        }
	
        public void setValueAt(Object value, int row, int col) {
        	if(isCellEditable(row, col)){
		    data[row][col] = value;
        	}
        }
    }
    
    public void display(){
	
	main_pane.setLayout(new BorderLayout());
	main_pane.add(prompt_title, BorderLayout.NORTH);
	main_pane.add(table_pane, BorderLayout.CENTER);
	main_pane.add(control_pane, BorderLayout.SOUTH);
		
	this.getContentPane().add(main_pane);
	//this.setPreferredSize(new Dimension(500,300));
	this.setSize(new Dimension(500,300));
	this.setVisible(true);
	this.setTitle(title);
	this.pack();
	this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	
    }

    public void cancel_button_actionPerformed(ActionEvent actionevent){
    	
	this.dispose();

    }
    
    public void save_button_actionPerformed(ActionEvent actionevent){
    	
	this.saveConfiguration();
    	this.dispose();
    
    }
   
    
    private void saveConfiguration(){
    	
    	File propertyFile = new File(destination);
	boolean fileExists = propertyFile.exists();
	Writer writer = null;
 	
	if(fileExists){
	    try{
		propertyFile.delete();
		propertyFile.createNewFile();
		writer = new BufferedWriter(new FileWriter(propertyFile));
 	    	
		for(int i = 0; i< data.length; i++){
		    String setting = (String)data[i][0];
		    String value = (String) data[i][1];
		    writer.write(setting + " = " + value+"\n\n");
		}
		
		writer.close();
		}catch (IOException ex){
		ex.printStackTrace();
	    }
	}
	else{
	    String errMsg ="These settings cannot be saved.";
	    JOptionPane.showMessageDialog(new JFrame(),errMsg);
	}
    }
}

class Cancel_button_Adapter implements ActionListener {
    
    private MatExtensionConfigurePrompt adaptee;
    
    Cancel_button_Adapter(MatExtensionConfigurePrompt adaptee) {
        this.adaptee = adaptee;
    }
    
    public void actionPerformed(ActionEvent e) {
        adaptee.cancel_button_actionPerformed(e);
    }
}

class Save_button_Adapter implements ActionListener {
    
    private MatExtensionConfigurePrompt adaptee;
    
    Save_button_Adapter(MatExtensionConfigurePrompt adaptee) {
        this.adaptee = adaptee;
    }
    
    public void actionPerformed(ActionEvent e) {
        adaptee.save_button_actionPerformed(e);
    }
}


