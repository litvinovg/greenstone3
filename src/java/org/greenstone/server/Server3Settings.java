package org.greenstone.server;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;

import org.w3c.dom.*;

import org.greenstone.util.ScriptReadWrite;

import org.greenstone.util.GlobalProperties;
import org.greenstone.gsdl3.util.GSXML;
import org.greenstone.gsdl3.util.XMLConverter;

import org.greenstone.server.BaseServerSettings;

public class Server3Settings extends BaseServerSettings
{
    protected String servletDefault = null;
    protected JComboBox servlet_combobox;
    protected HashMap url_mappings = null;

    public Server3Settings(BaseServer server) 
    {
	super(server);
    }

    protected JPanel createServletPanel()
    {
	JLabel servlet_label = new JLabel(server.dictionary.get(BaseServer.Property.SERVER_SETTINGS+".URL"));

	this.servletDefault = server.config_properties.getProperty(BaseServer.Property.DEFAULT_SERVLET).replaceAll("/","");
	
	servlet_combobox = new JComboBox();
	servlet_combobox.setMaximumRowCount(5);
	servlet_combobox.setBackground(bg_color);

	File web_xml = new File(GlobalProperties.getProperty(BaseServer.Property.GSDL_HOME) + File.separator + "WEB-INF" + File.separator + "web.xml");
	XMLConverter converter = new XMLConverter();
	Document web_config = converter.getDOM(web_xml);
	if (web_config == null) {
	    logger.error("web.xml is null! "+web_xml.getAbsolutePath());
	    return null;
	}

	NodeList servlet_mappings = web_config.getElementsByTagName("servlet-mapping");
	// make a little map class
	url_mappings = new HashMap();
	for (int i = 0; i < servlet_mappings.getLength(); i++) {
	    Element map = (Element) servlet_mappings.item(i);
	    Element servlet_name_elem = (Element) GSXML.getChildByTagName(map, "servlet-name");
	    String name = GSXML.getNodeText(servlet_name_elem);
	    Element url_pattern_elem = (Element) GSXML.getChildByTagName(map, "url-pattern");
	    String pattern = GSXML.getNodeText(url_pattern_elem);
	    // Ignore the Axis servlets
	    if (!(name.equals("AxisServlet"))) {
		servlet_combobox.addItem(name.trim());
		url_mappings.put(name, pattern);
	    }

	    if (pattern.replaceAll("/","").equals(servletDefault)) {
		servlet_combobox.setSelectedItem(name);
	    }
	}


        JPanel comb_panel = new JPanel();
	comb_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
	comb_panel.add(servlet_label);
	comb_panel.add(servlet_combobox);

	return comb_panel;
    }

    public boolean[] onSave()
    {
	boolean hasChanged = false;
	boolean requireRestart = false;
	if (!servletDefault.equals((String)url_mappings.get(servlet_combobox.getSelectedItem()))) {
	    hasChanged = true;
	    requireRestart = true;
	}
	boolean[] returnValues = { hasChanged, requireRestart };
	return returnValues;
    }

    public void save(ScriptReadWrite scriptReadWrite, ArrayList newFileLines) 
    {
	String newAutoEnter = (new Boolean(autoEnter.isSelected())).toString();
	newFileLines = scriptReadWrite.queryReplace(newFileLines, BaseServer.Property.AUTOSTART, newAutoEnter);

	String newKeepPort = (new Boolean(keepPortToggle.isSelected())).toString();
	newFileLines = scriptReadWrite.queryReplace(newFileLines, BaseServer.Property.KEEPPORT, newKeepPort);

	String newServletDef = (String) servlet_combobox.getSelectedItem();
	newFileLines = scriptReadWrite.queryReplace(newFileLines,BaseServer.Property.DEFAULT_SERVLET, (String) url_mappings.get(newServletDef));	
    }

}
