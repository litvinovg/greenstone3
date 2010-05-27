package org.greenstone.admin.guiext;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import javax.crypto.spec.SecretKeySpec;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import java.util.Properties;
import java.util.Arrays;
import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.w3c.dom.Element;

import org.greenstone.util.Configuration;
import org.greenstone.admin.LoggedMessageArea;

public class PropertiesStep extends Step
{
    OptionList[] _optionLists = null;
    JTable[] _tables = null;
    OptionList[] _modifiedOptionLists = null;
    
    HashMap _optionListTableMap = new HashMap();

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

    public JTable getTableFromOptionList(OptionList list)
    {
	return ((JTable)_optionListTableMap.get(list));
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

		CustomTableModel tableModel = new CustomTableModel(new String[]{"Setting", "Value", "Check"}, options);
		PropertyTable propertiesTable = new PropertyTable(tableModel, currentList);
		TableColumn column = propertiesTable.getColumnModel().getColumn(2);
		column.setPreferredWidth(32);
		column.setMinWidth(32);
		column.setMaxWidth(32);
		column.setResizable(false);

		_optionListTableMap.put(currentList, propertiesTable);

		//The line below is necessary as the default grid colour on mac is white, which makes the lines invisible.
		propertiesTable.setGridColor(Color.BLACK);

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

    public class BrowseButtonListener implements ActionListener
    {
	Option _option = null;
	JTextField _path = null;
	JFileChooser _browser = null;
	
	public BrowseButtonListener(Option option, JTextField path)
	{
	    _option = option;
	    _path = path;
	    _browser = new JFileChooser();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    if(e.getActionCommand().equals("edit")){
		if(_option.getType().equalsIgnoreCase("folderbrowse")){
		    _browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		int returnValue = _browser.showOpenDialog(null);

		if(returnValue == JFileChooser.APPROVE_OPTION){
		    _option.setValue(_browser.getSelectedFile().getAbsolutePath());
		    _path.setText(_option.getValue());
		}
	    }
	}
    }

    public class PathBoxListener implements DocumentListener
    {
	Option _option = null;
	JTextField _path = null;

	public PathBoxListener(Option option, JTextField path)
	{
	    _option = option;
	    _path = path;
	}
	
	public void changedUpdate(DocumentEvent e)
	{
	    _option.setValue(_path.getText());
	}
	
	public void insertUpdate(DocumentEvent e)
	{
	    _option.setValue(_path.getText());
	}

	public void removeUpdate(DocumentEvent e)
	{
	    _option.setValue(_path.getText());
	}
    }

    public class PasswordOKButtonListener implements ActionListener
    {
	JPasswordField _password = null;
	JPasswordField _confirm = null;
	Option _option = null;
	JFrame _passwordFrame = null;

	public PasswordOKButtonListener(JPasswordField password, JPasswordField confirm, Option option, JFrame passwordFrame)
	{
	    _password = password;
	    _confirm = confirm;
	    _option = option;
	    _passwordFrame = passwordFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
	    if(Arrays.equals(_password.getPassword(), _confirm.getPassword())){
		try{
		    _option.setValue(new String(encrypt(_password.getPassword())));
		}
		catch(Exception ex){
		    System.err.println("Error encrypting password");
		}
		_passwordFrame.dispose();
	    }
	    else{
		JOptionPane.showMessageDialog(null, "The passwords you entered do not match.");
		_password.setText("");
		_confirm.setText("");
	    }
	}
    }  

    public static char[] encrypt(char[] value) throws Exception
    {
	byte[] salt = {'T', 'p'};
	int count = 20;

	KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecretKey secretKey = keygen.generateKey();
        byte[] raw = secretKey.getEncoded();
        SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

	Cipher cipher = Cipher.getInstance("AES");
	cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
	
	byte[] encryptedPassword = cipher.doFinal((new String(value)).getBytes("UTF-8"));

	return new String(encryptedPassword).toCharArray();
    }

    public class PasswordCancelButtonListener implements ActionListener
    {
	JFrame _passwordFrame = new JFrame();

	public PasswordCancelButtonListener(JFrame passwordFrame)
	{
	    _passwordFrame = passwordFrame;
	}

	public void actionPerformed(ActionEvent e)
	{
	    _passwordFrame.dispose();
	}
    }

    public class PasswordFieldListener implements ActionListener
    {
	JPasswordField _password = new JPasswordField(10);
	JPasswordField _confirm = new JPasswordField(10);
	JFrame _passwordFrame = new JFrame("Enter password");

	public PasswordFieldListener(Option option)
	{
	    JButton okButton = new JButton("Change");
	    JButton cancelButton = new JButton("Cancel");
	    
	    okButton.addActionListener(new PasswordOKButtonListener(_password, _confirm, option, _passwordFrame));
	    cancelButton.addActionListener(new PasswordCancelButtonListener(_passwordFrame));

	    _passwordFrame.getContentPane().setLayout(new GridLayout(3,2));
	    _passwordFrame.getContentPane().add(new JLabel("Password:"));
	    _passwordFrame.getContentPane().add(_password);
	    _passwordFrame.getContentPane().add(new JLabel("Confirm:"));
	    _passwordFrame.getContentPane().add(_confirm);
	    _passwordFrame.getContentPane().add(okButton);
	    _passwordFrame.getContentPane().add(cancelButton);
	    _passwordFrame.pack();
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    _password.setText("");
	    _confirm.setText("");
	    _passwordFrame.setLocationRelativeTo(null);
	    _passwordFrame.setVisible(true);
	}
    }

    public class PasswordEditor extends AbstractCellEditor implements TableCellEditor
    {
	JButton _onClickButton = new JButton("Change Password?");

	public PasswordEditor(Option option)
	{
	    _onClickButton.addActionListener(new PasswordFieldListener(option));
	}

	public Object getCellEditorValue()
	{
	    return "";
	}

	public Component getTableCellEditorComponent(JTable table, Object path, boolean isSelected, int row, int column)
	{
	    return _onClickButton;
	}
    }

    public class BrowseEditor extends AbstractCellEditor implements TableCellEditor
    {
	JPanel _browserPanel = null;
	JTextField _path = null;
	JButton _browserButton = null;
	Option _option = null;
	String _type = null;

	public BrowseEditor(Option option, String type)
	{
	    _option = option;
	    _type = type;
	    
	    _path = new JTextField(_option.getValue());
	    _path.getDocument().addDocumentListener(new PathBoxListener(_option, _path));

	    _browserButton = new JButton("Browse");
	    _browserButton.setActionCommand("edit");
	    _browserButton.addActionListener(new BrowseButtonListener(_option, _path));
	    _browserButton.setBorderPainted(false);

	    _browserPanel = new JPanel();
	    _browserPanel.setLayout(new BorderLayout());
	    _browserPanel.add(_path, BorderLayout.CENTER);
	    _browserPanel.add(_browserButton, BorderLayout.EAST);
	}

	public Object getCellEditorValue()
	{
	    _path.setText(_option.getValue());
	    return _option.getValue();
	}

	public Component getTableCellEditorComponent(JTable table, Object path, boolean isSelected, int row, int column)
	{
	    return _browserPanel;
	}
    }

    public class PropertyTable extends JTable 
    {
	OptionList _properties = null;
	TableCellEditor[] _editors = null;

	public PropertyTable(TableModel tm, OptionList properties)
	{
	    super(tm);
	    _properties = properties;
	    
	    Option[] options = _properties.getOptions();
	    _editors = new TableCellEditor[options.length];

	    for(int i = 0; i < options.length; i++){
		Option currentOption = options[i];

		if(currentOption.getType().equals("filebrowse")){
		    _editors[i] = new BrowseEditor(currentOption, "file");
		}
		else if(currentOption.getType().equals("folderbrowse")){
		    _editors[i] = new BrowseEditor(currentOption, "folder");
		}
		else if(currentOption.getType().equals("password")){
		    _editors[i] = new PasswordEditor(currentOption);
		}
		else{
		    _editors[i] = null;
		}
	    }
	}

	public TableCellEditor getCellEditor(int row, int column)
	{
	    if(column == 1 && row < _editors.length && _editors[row] != null){
		return _editors[row];
	    }
	    return super.getCellEditor(row, column);
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
	private String[] _columnNames = null;
    	private Option[] _data = null;
	private ImageIcon[] _images = null;
	
    	public CustomTableModel(String[] columnNames, Option[] data){
	    _columnNames = columnNames;
	    _data = data;
	    
	    _images = new ImageIcon[_data.length];
	    for(int i = 0; i < _data.length; i++){
		_images[i] = _data[i].getImage();
	    }
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
	    if(col == 0){
		return _data[row].getName();
	    }
	    else if(col == 1){
		if(_data[row].getType().equals("password") && !_data[row].getValue().equals("")){
		    return "Password Set";
		}
		else if(_data[row].getType().equals("password") && _data[row].getValue().equals("")){
		    return "No Password Set";
		}
		
		return _data[row].getValue();
	    }
	    else if(col == 2){
		return _data[row].getImage();
	    }
            else{
		return null;
	    }
        }


        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

	public boolean isCellEditable(int row, int col) {
	    if (col == 1) {
                return true;
            } else {
                return false;
            }
        }
	
        public void setValueAt(Object value, int row, int col) {
	    Option o = _data[row];
	    if(isCellEditable(row, col) && !o.getType().equals("password")){
		o.setValue((String)value);
		
		if(o.isCheckable()){
		    _images[row] = o.getImage();
		}
	    }
	    fireTableDataChanged();
        }
    }
}