package org.greenstone.server;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.greenstone.server.BaseServerControl;

/**
 * This class is to start or restart the Greenstone 3 library server
 * 
 */
public class Server3Control extends BaseServerControl 
{
    
    public Server3Control(BaseServer server,String frame_title) 
    {
	super(server,frame_title);
    }

    protected JMenuBar createMenu() 
    {
	JMenuItem iConf = new JMenuItem(BaseServer.dictionary.get("ServerControl.Menu.Settings"));
	iConf.setBackground(Color.white);
	iConf.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    BaseServerSettings serverSetting = new Server3Settings(server);
		}
	    });

	return createMenu(iConf);
    }
}
