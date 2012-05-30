package org.greenstone.gsdl3;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.greenstone.util.GlobalProperties;

public class BaseGreenstoneServlet extends HttpServlet
{
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		GlobalProperties.loadGlobalProperties(config.getServletContext().getRealPath(""));
	}
}