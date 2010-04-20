package org.greenstone.admin.guiext;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.w3c.dom.Element;

import org.greenstone.admin.Configuration;
import org.greenstone.admin.LoggedMessageArea;

public class PropertiesStep extends Step
{
    OptionList[] _optionLists = null;
    JTable[] _tables = null;
    OptionList[] _modifiedOptionLists = null;

    public PropertiesStep(Element propertiesStepElement, SequenceList parent)
    {
	super(propertiesStepElement, parent);
	
	if(propertiesStepElement != null){
	    ExtensionInformation info = _parent.getParent();

	    Element[] optionListElements = ExtXMLHelper.getMultipleChildElements(propertiesStepElement, ExtXMLHelper.OPTION_LIST, true);

	    if(optionListElements != null){
		
		_optionLists = new OptionList[optionListElements.length];
		_modifiedOptionLists = new OptionList[optionListElements.length];

		for(int i = 0; i < optionListElements.length; i++){
		    _optionLists[i] = new OptionList(optionListElements[i], this, false);
		    _modifiedOptionLists[i] = new OptionList(optionListElements[i], this, true);
		}
	    }
	    else{
		System.err.println("This properties <" + ExtXMLHelper.STEP + "> element has no <" + ExtXMLHelper.OPTION_LIST + "> elements");
	    }
	}
	else{
	    System.err.println("This properties <" + ExtXMLHelper.STEP + "> element is null" );
	}
	
	_button.addActionListener(new PropertiesButtonListener());
    }

    public void setPropertiesToDefaults()
    {
	for(int i = 0; i < _optionLists.length; i++){
	    Option[] _modifiedOptions = _modifiedOptionLists[i].getOptions();
	    Option[] _defaultOptions = _optionLists[i].getOptions();
	    
	    for(int j = 0; j < _defaultOptions.length; j++){
		_modifiedOptions[j].setValue(_defaultOptions[j].getValue());
	    }
	}
    }

    public class PropertiesButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    ExtensionInformation extension = _parent.getParent();
	    
	    String fileStem = extension.getFileStem();
	    
	    JPanel mainPanel = new JPanel();
	    mainPanel.setBorder(BorderFactory.createLoweredBevelBorder());
	    mainPanel.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	    mainPanel.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	    mainPanel.setLayout(new BorderLayout());
	    
	    JPanel tablePanel = new JPanel(new GridLayout(_optionLists.length, 1));
	    
	    if(_tables == null){
		_tables = new JTable[_optionLists.length];
	    }
	    for(int i = 0; i < _optionLists.length; i++){
		OptionList currentList = _modifiedOptionLists[i];
	    
		JLabel singleTableLabel = new JLabel(currentList.getLabel()); 
		singleTableLabel.setHorizontalAlignment(SwingConstants.CENTER);
		singleTableLabel.setFont(new Font("Arial", Font.BOLD, 13));

		JPanel singleTablePanel = new JPanel();
		singleTablePanel.setLayout(new BorderLayout());
		singleTablePanel.add(singleTableLabel, BorderLayout.NORTH);
		
		Option[] options = currentList.getOptions();		

		JTable propertiesTable = new JTable(new CustomTableModel(new String[]{"Setting", "Value"}, options));
		
		if(_tables[i] == null){
		    _tables[i] = propertiesTable;
		}

		//The line below is a workaround for a bug in the JTable class that does not store properties if focus is given to another component.
		propertiesTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		singleTablePanel.add(propertiesTable, BorderLayout.CENTER);
		
		tablePanel.add(singleTablePanel);
	    }

	    JButton defaultsButton = new JButton("Restore default settings");
	    defaultsButton.addActionListener(new DefaultSettingsButtonListener());
	    
	    JButton saveButton = new JButton("Save");
	    saveButton.addActionListener(new SaveButtonListener());
	    
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new GridLayout(1, 2));
	    buttonPanel.add(saveButton);
	    buttonPanel.add(defaultsButton);
	    
	    mainPanel.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
	    mainPanel.setBackground(Configuration.getColor("coloring.workspace_selection_background"));
	    mainPanel.setForeground(Configuration.getColor("coloring.workspace_selection_foreground"));
	    mainPanel.add(tablePanel, BorderLayout.CENTER);
	    mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	    
	    _parent.getParent().changeExtPane(mainPanel);
	}
    }
    
    public class SaveButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    for(int i = 0; i < _optionLists.length; i++){
		OptionList currentOptionList = _modifiedOptionLists[i];
		Option[] currentOptions = currentOptionList.getOptions();

		Properties newProperties = new Properties();
		File propertiesFile = null;
		if(currentOptionList.getFilename().equals("")){
		    propertiesFile = new File(_parent.getParent().getExtensionDirectory() + System.getProperty("file.separator") + "build.properties");
		}
		else{
		    propertiesFile = new File(_parent.getParent().getExtensionDirectory() + System.getProperty("file.separator") + currentOptionList.getFilename());
		}

		if(propertiesFile.exists()){
		    try{
			newProperties.load(new FileInputStream(propertiesFile));
		    }
		    catch(Exception ex){
			System.err.println("Could not load properties file before saving to it, existing values will be removed. Possible reasons for this include: \n - Invalid properties file format\n - File is write protected");
		    }
		}

		for(int j = 0; j < currentOptions.length; j++){
		    Option currentOption = currentOptions[j];
		    if(currentOptionList.getId().equals("")){
			newProperties.setProperty(currentOption.getId(), currentOption.getValue());
		    }
		    else{
			newProperties.setProperty(currentOptionList.getId() + "." + currentOption.getId(), currentOption.getValue());
		    }
		}

		try{
		    if(currentOptionList.getFilename().equals("")){
			newProperties.store(new FileOutputStream(_parent.getParent().getExtensionDirectory() + System.getProperty("file.separator") + "build.properties"), null);
		    }
		    else{
			newProperties.store(new FileOutputStream(_parent.getParent().getExtensionDirectory() + System.getProperty("file.separator") + currentOptionList.getFilename()), null);
		    }
		}
		catch(Exception ex){
		    System.err.println("Could not save to properties file.");
		}
	    }

	    _parent.registerStepCompletion(PropertiesStep.this);
	}
    }

    public class DefaultSettingsButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    for(int i = 0; i < _tables.length; i++){
		JTable currentTable = _tables[i]; 
		Option[] currentDefaultOptions = _optionLists[i].getOptions();

		for(int j = 0; j < currentDefaultOptions.length; j++){
		    Option currentOption = currentDefaultOptions[j];
		    currentTable.setValueAt(currentOption.getValue(), j, 1);
		}
		
		currentTable.revalidate();
		currentTable.repaint();
	    }
	}
    }

    public class CustomTableModel extends AbstractTableModel
    {
	private String[] _columnNames;
    	private Option[] _data;
	
    	public CustomTableModel(String[] columnNames, Option[] data){
	    _columnNames = columnNames;
	    _data = data;
    	}
	
        public int getColumnCount() {
            return _columnNames.length;
        }
	
        public int getRowCount() {
            return _data.length;
        }

        public String getColumnName(int col) {
            return _columnNames[col];
        }

        public Object getValueAt(int row, int col) {
	    Option o = _data[row];
	    if(col == 0){
		return o.getName();
	    }
	    else if(col == 1){
		return o.getValue();
	    }
            else{
		return null;
	    }
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
		_data[row].setValue((String)value);
	    }
	    fireTableDataChanged();
        }
    }
}