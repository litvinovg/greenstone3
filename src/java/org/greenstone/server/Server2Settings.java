package org.greenstone.server;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;

import org.greenstone.server.BaseServerSettings;
import org.greenstone.util.ScriptReadWrite;

public class Server2Settings extends BaseServerSettings
{
    protected JComboBox prefix_combobox;
    protected JCheckBox allowConnections;
    protected JRadioButton[] hostRadioButtons = new JRadioButton[4];

    // 0 to 3: 0 is resolve (hostname) from local IP, 1 is local IP address, 2 is localhost, 3 is 127.0.0.1
    protected int address_resolution_method = 2;
    protected int externalaccess = 0;

    public Server2Settings(BaseServer server) 
    {
	super(server);	
    }

    protected JPanel createServletPanel()
    {
	JPanel server2panel = new JPanel();
	server2panel.setLayout(new BorderLayout());

	boolean allowCons = false;
	String externalAccess = server.config_properties.getProperty("externalaccess").trim();
	if(externalAccess != null && externalAccess.equals("1")) {
	    this.externalaccess = 1;
	    allowCons = true;
	}
	allowConnections = new JCheckBox(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".ExternalAccess"), allowCons);
	allowConnections.setBackground(bg_color);
	
	JPanel connect_panel = new JPanel(new GridLayout(4, 1));
	connect_panel.setBackground(bg_color);
	connect_panel.setBorder(BorderFactory.createTitledBorder(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".AddressResolutionMethod")));

	hostRadioButtons = new JRadioButton[4];
	hostRadioButtons[0] = new JRadioButton(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".ResolveIP"));
	hostRadioButtons[1] = new JRadioButton(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".LocalIP"));
	hostRadioButtons[2] = new JRadioButton(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".AlwaysUse")+" localhost");
	hostRadioButtons[3] = new JRadioButton(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".AlwaysUse")+" 127.0.0.1");

	ButtonGroup hostGroup = new ButtonGroup();
	for(int i = 0; i < hostRadioButtons.length; i ++) {
	    connect_panel.add(hostRadioButtons[i]);
	    hostGroup.add(hostRadioButtons[i]);
	    hostRadioButtons[i].setBackground(bg_color);
	}	

	String addressResolutionMethod = server.config_properties.getProperty("address_resolution_method").trim();
	if(addressResolutionMethod != null) {
	    this.address_resolution_method = Integer.parseInt(addressResolutionMethod);
	}
	hostRadioButtons[address_resolution_method].setSelected(true);

	JPanel comb_panel = new JPanel(new BorderLayout());	
	comb_panel.add(allowConnections, BorderLayout.NORTH);
	comb_panel.add(connect_panel, BorderLayout.CENTER);
	return comb_panel;
    }

    public boolean[] onSave()
    {
	// superclass detects changes to port and autoenter
	// handle changes to address_resolution_method and externalAccess/allowConnections here

	boolean hasChanged = false;
	boolean requireRestart = false;
	
	for(int i = 0; i < hostRadioButtons.length; i++) {
	    if(hostRadioButtons[i].isSelected() && address_resolution_method != i) {
		address_resolution_method = i;
		hasChanged = true;
		requireRestart = true;
		server.reconfigRequired();
	    }
	}

	int oldExternalAccess = externalaccess;
	externalaccess = allowConnections.isSelected() ? 1 : 0;
	if (oldExternalAccess != externalaccess) {
	    hasChanged = true;
	    requireRestart = true;
	    server.reconfigRequired();
	}

	boolean[] returnValues = { hasChanged, requireRestart };
	return returnValues;
    }

    public void save(ScriptReadWrite scriptReadWrite, ArrayList newFileLines) 
    {
	// write only 1 or 0 (and not true or false) for Server2
	boolean auto_enter = autoEnter.isSelected();
	if(autoStart != auto_enter) {
	    String newAutoEnter = auto_enter ? "1" : "0";
	    newFileLines = scriptReadWrite.queryReplace(newFileLines, BaseServer.Property.AUTOSTART, newAutoEnter);
	}

	// external access - onSave() would have updated this value
	newFileLines = scriptReadWrite.queryReplace(newFileLines, "externalaccess", Integer.toString(externalaccess));
	
	// work out the host (default is address_resolution_method 2: localhost)
	String hostIP = "127.0.0.1";
	InetAddress inetAddress = null;
	try {
	    inetAddress = InetAddress.getLocalHost();
	    hostIP = inetAddress.getHostAddress(); // used for all cases unless an Exception is thrown here
	} catch(UnknownHostException e) {
	    logger.error(e);
	    logger.info("Server2.java reload(): Defaulting host URL to localhost");
	    hostIP = "127.0.0.1";
	    address_resolution_method = 2;	    
	}

	newFileLines = scriptReadWrite.replaceOrAddLine(newFileLines, "hostIP", hostIP, true);

	// address resolution method - onSave() would have updated
	// this value (or the UnknownHostException above might have)
	newFileLines = scriptReadWrite.queryReplace(newFileLines, "address_resolution_method", Integer.toString(address_resolution_method));
	
    }    
}
