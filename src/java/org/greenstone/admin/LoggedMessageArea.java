package org.greenstone.admin;

import java.io.File;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggedMessageArea extends JTextArea
{
    Logger _logger = null;
    boolean _loggingLoaded = false;

    public LoggedMessageArea(Class loggerClass)
    {
	if(!_loggingLoaded){
	    PropertyConfigurator.configure("log4j.properties");
	    _loggingLoaded = true;
	}
	_logger = Logger.getLogger(loggerClass);
    }

    public void append(String s)
    {
	super.append(s);
	_logger.info(s);
    }
}