package org.greenstone.server;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

import org.w3c.dom.*;

import org.apache.log4j.*;

import org.greenstone.util.ScriptReadWrite;

public abstract class BaseServerSettings extends JDialog implements ActionListener
{
    static Logger logger = Logger.getLogger(BaseServerSettings.class.getName());
    static final int DEFPORT = 8080;
    static final Color bg_color = Color.white;

    protected JCheckBox autoEnter;
    protected JCheckBox keepPortToggle;

    protected JSpinner portNumber_spinner = null;
    protected JTextField program_path_field = null;
    protected JButton browse_button = null; 
    protected JRadioButton default_browser_button = null ;
    protected JRadioButton other_browser_button = null;
    protected Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

    protected int portNum = DEFPORT;
    protected boolean autoStart = false;
    protected boolean keepPort = false;
    protected String browserPath = "";
    protected boolean useDefaultBrowser = true;

    protected JDialog self;
    protected BaseServer server;
     
    public BaseServerSettings(BaseServer server) 
    {
	super(server.getServerControl(), "", true);
	this.self = this;
        this.server = server;

        try {
	    this.portNum = Integer.parseInt(server.config_properties.getProperty(BaseServer.Property.WEB_PORT));
	}
        catch(Exception e){
	    logger.error(e);
	} 

        this.browserPath = server.config_properties.getProperty(BaseServer.Property.BROWSER_PATH);
        
        if (this.browserPath == null || this.browserPath.equals("")){
	    useDefaultBrowser = true;
            this.browserPath = "";
	}
	else{
	    useDefaultBrowser = false;
	}

	String auto_start_str = server.config_properties.getProperty(BaseServer.Property.AUTOSTART).trim();
	if (auto_start_str.equals("true") || auto_start_str.equals("1")) {
	    this.autoStart = true;
	} else {
	    this.autoStart = false;
	}

	String keep_port_str = server.config_properties.getProperty(BaseServer.Property.KEEPPORT, "false").trim();
	if (keep_port_str.equals("true") || keep_port_str.equals("1")) {
	    this.keepPort = true;
	} else {
	    this.keepPort = false;
	}


	setTitle(server.dictionary.get("ServerSettings.Title"));
	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

	JLabel port_label = new JLabel(BaseServer.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".Port"));

	portNumber_spinner = new JSpinner(new SpinnerNumberModel(portNum,1,65535,1));
	portNumber_spinner.setEditor(new JSpinner.NumberEditor(portNumber_spinner, "#####"));

	autoEnter = new JCheckBox(server.dictionary.get("ServerSettings.Auto_Start"));
	keepPortToggle = new JCheckBox(server.dictionary.get("ServerSettings.Keep_Port"));
	
	if (autoStart) {
	    autoEnter.setSelected(true);
	} else {
	    autoEnter.setSelected(false);
	}
	autoEnter.setBackground(bg_color);

	if(keepPort) {
	    keepPortToggle.setSelected(true);
	} else {
	    keepPortToggle.setSelected(false);
	}
	keepPortToggle.setBackground(bg_color);


	JButton save_button = new JButton(BaseServer.dictionary.get("ServerSettings.OK"));
	save_button.addActionListener(new SaveActionListener());

	JButton exit_button = new JButton(BaseServer.dictionary.get("ServerSettings.Cancel"));
	exit_button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    self.dispose();
		}
	    });

	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent we) { 
		    self.dispose();
		}
	    });

        JPanel port_panel = new JPanel();    
        port_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	port_panel.add(port_label);
	port_panel.add(portNumber_spinner);
	port_panel.setBackground(bg_color);

	JPanel top_panel = new JPanel(new GridLayout(3,1));
	top_panel.add(port_panel);
	top_panel.add(keepPortToggle);
	top_panel.add(autoEnter);

	JPanel comb_panel = createServletPanel();
	comb_panel.setBackground(bg_color);

	JPanel mid_panel = new JPanel();
	mid_panel.setLayout(new BorderLayout());
	mid_panel.add(top_panel, BorderLayout.NORTH);
	mid_panel.add(comb_panel, BorderLayout.CENTER);
	mid_panel.setBackground(bg_color);
	mid_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));


	JPanel browser_panel = new JPanel();
        browser_panel.setLayout(new GridLayout(4,1));
        default_browser_button = new JRadioButton(BaseServer.dictionary.get("ServerSettings.DefaultBrowser"));
        other_browser_button = new JRadioButton(BaseServer.dictionary.get("ServerSettings.OtherBrowser"));
	default_browser_button.addActionListener(this);
	other_browser_button.addActionListener(this);	

	ButtonGroup bg = new ButtonGroup();
        bg.add(default_browser_button);
        bg.add(other_browser_button);
        default_browser_button.setBorder(BorderFactory.createEmptyBorder(5,10,5,0));
        default_browser_button.setBackground(bg_color);     
        other_browser_button.setBorder(BorderFactory.createEmptyBorder(5,10,5,0)); 
        other_browser_button.setBackground(bg_color);
        JPanel browse_program_panel = new JPanel();     
        browse_program_panel.setLayout(new BorderLayout());
        program_path_field = new JTextField(this.browserPath);
        browse_button = new JButton(BaseServer.dictionary.get("ServerSettings.Browse"));
	browse_program_panel.add(program_path_field,BorderLayout.CENTER);
        browse_program_panel.add(browse_button,BorderLayout.EAST);
        browse_program_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,0));
	browse_program_panel.setBackground(bg_color);
        browser_panel.add(new JLabel(BaseServer.dictionary.get("ServerSettings.ChooseBrowser")));
        browser_panel.add(default_browser_button);
	browser_panel.add(other_browser_button);
        browser_panel.add(browse_program_panel);
	browser_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        browser_panel.setBackground(bg_color);
	if (useDefaultBrowser){
	    default_browser_button.setSelected(true);
	    program_path_field.setEnabled(false);
	    browse_button.setEnabled(false); 
	}
	else{
	    other_browser_button.setSelected(true);
	}
	browse_button.addActionListener(this);
        
        JPanel down_panel = new JPanel();
	down_panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
	down_panel.add(save_button);
	down_panel.add(exit_button);
	down_panel.setBackground(bg_color);
	down_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

        getContentPane().add(mid_panel, BorderLayout.NORTH);
        getContentPane().add(browser_panel, BorderLayout.CENTER);
	getContentPane().add(down_panel, BorderLayout.SOUTH);
	getContentPane().setBackground(bg_color);

	pack();
	setLocation((screen.width - getWidth()) / 2,
		    (screen.height - getHeight()) / 2);
	setVisible(true);
    }


    protected boolean[] onSave() {
	boolean[] returnValues = { false, false }; // hasChanged, requireRestart
	return returnValues;
    }
    protected void save(ScriptReadWrite scriptReadWrite, ArrayList newFileLines) {}
    protected abstract JPanel createServletPanel();


    public void actionPerformed(ActionEvent ev) {
	if(ev.getSource() == browse_button) {
	    JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(self);        
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File program_path = chooser.getSelectedFile();
		program_path_field.setText(program_path.getPath());
	   } 
	} else { // one of the two Choose Browser radiobuttons
	    if (default_browser_button.isSelected()){
		program_path_field.setEnabled(false);
		browse_button.setEnabled(false);
	    }
	    else{
		program_path_field.setEnabled(true);
		browse_button.setEnabled(true);
	    }
	}       
    }
    

    private class SaveActionListener
	implements ActionListener {
	
	public void actionPerformed(ActionEvent ev) {
	    // save everything to config_properties if things have changed
	    boolean has_changed = false;
            boolean require_restart = false;

	    if (portNum != ((Integer)portNumber_spinner.getValue()).intValue()) {
		has_changed = true;
                require_restart = true;
                server.reconfigRequired();
                portNum = ((Integer)portNumber_spinner.getValue()).intValue();
             	logger.info("port changed, new port is "+portNumber_spinner.getValue());
	    }
	    if (autoStart != autoEnter.isSelected()) {
            	has_changed = true;
	    }
	    if (keepPort != keepPortToggle.isSelected()) {
            	has_changed = true;
	    }


	    // call subclass' onSave method, which may indicate (further) changes,
	    // and which may or may not require a restart
	    boolean[] returnValues = onSave();
	    has_changed = has_changed || returnValues[0];
	    require_restart = require_restart || returnValues[1];

            //changed to use other browser
            if (useDefaultBrowser && other_browser_button.isSelected()){
		browserPath = program_path_field.getText();
		has_changed = true;
	    } 
	    //the browser path has been changed 
	    if (!useDefaultBrowser && !browserPath.equals(program_path_field.getText())){
		browserPath = program_path_field.getText();
		has_changed = true;
	    }
	    
            //changed to use the default browser
	    if (default_browser_button.isSelected() && !useDefaultBrowser){
		browserPath = "";
		has_changed = true;
	    }
             

	    if (has_changed) {
		ArrayList oldFileLines = null;
		ArrayList newFileLines = null;
		
		ScriptReadWrite scriptReadWrite = new ScriptReadWrite();
		oldFileLines = scriptReadWrite.readInFile(BaseServer.config_properties_file);
		
		newFileLines = scriptReadWrite.queryReplace(oldFileLines, BaseServer.Property.WEB_PORT, portNum+"");
		
		// call the subclass' save() method to save custom elements
		save(scriptReadWrite, newFileLines);
		
		String osName = System.getProperty("os.name");
                if (osName.startsWith("Windows")){
		    browserPath = browserPath.replaceAll("\\\\","/");
		}
		newFileLines = scriptReadWrite.replaceOrAddLine(newFileLines, BaseServer.Property.BROWSER_PATH, browserPath, true);

		scriptReadWrite.writeOutFile(BaseServer.config_properties_file, newFileLines);

		server.reloadConfigProperties();
		server.reload(); // work out the URL again in case it has changed
		if (require_restart){
		    JOptionPane.showMessageDialog(null,server.dictionary.get("ServerSettings.SettingChanged"),"Info", JOptionPane.INFORMATION_MESSAGE);
		    if(autoStart) {
			server.autoStart();
			server.getServerControl().updateControl();
		    }
		}
	    } 

	    self.dispose();
	}
    }

}
