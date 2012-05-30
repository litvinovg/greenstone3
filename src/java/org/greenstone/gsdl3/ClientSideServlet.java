package org.greenstone.gsdl3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.greenstone.gsdl3.util.Dictionary;
import org.greenstone.gsdl3.util.XSLTUtil;

public class ClientSideServlet extends BaseGreenstoneServlet
{
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String query_string = request.getQueryString();
		PrintWriter w = response.getWriter();

		if (query_string == null)
			displayParamError(response);

		String[] parts = StringUtils.split(query_string, "&");

		String[] keys = null; // Array of keys to look up
		String lang = "";
		String interface_name = "";
		String command = "";
		String params = "";

		for (String part : parts)
		{

			String[] nameval = StringUtils.split(part, '=');

			if (nameval.length != 2)
			{
				displayParamError(response);
				return;
			}

			String name = nameval[0];
			String value = nameval[1];

			if (name.equals("k"))
				keys = StringUtils.split(value, ",");
			else if (name.equals("l"))
				lang = value;
			else if (name.equals("i"))
				interface_name = value;
			else if (name.equals("c"))
				// Alternative commands
				command = value;
			else if (name.equals("p"))
				params = value;
		}

		if (keys == null && command.equals("") && params.equals(""))
		{
			// Not satisfiable
			displayParamError(response);
			return;
		}

		String bundle = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>\r\n<textBundle>\r\n";

		if (!command.equals("") && !params.equals(""))
		{

			// Alternative commands other than dictionary lookup
			String[] parameters = StringUtils.split(params, "|");
			String result = null;

			if (command.equals("getNumberedItem") && parameters.length == 2)
			{
				result = XSLTUtil.getNumberedItem(parameters[0], Integer.parseInt(parameters[1]));
			}
			else if (command.equals("exists") && parameters.length == 2)
			{
				result = XSLTUtil.exists(parameters[0], parameters[1]) ? "true" : "false";
			}
			else if (command.equals("isImage") && parameters.length == 1)
			{
				result = (XSLTUtil.isImage(parameters[0])) ? "true" : "false";
			}

			if (result != null)
			{
				bundle += "	<method name=\"" + command + "\" parameters=\"" + params + "\" return=\"" + result + "\" />\r\n";
			}
			else
			{
				displayParamError(response);
				return;
			}
		}
		else
		{

			for (String key : keys)
			{

				String original_key = key;
				String[] theParts = StringUtils.split(key, "|");
				String[] args = null;

				if (theParts.length > 1)
					args = StringUtils.split(theParts[1], ";");

				key = theParts[0];

				// Straight from XSLTUtils.java (src), with some modifications
				if (key.equals("null"))
				{
					bundle += "	<item key=\"" + key + "\" value=\"\" />\r\n";
					continue;
				}

				Dictionary dict = new Dictionary("interface_" + interface_name, lang);
				String result = dict.get(key, args);

				if (result == null)
				{
					String sep_interface_dir = interface_name + File.separatorChar + lang + File.separatorChar + "interface";
					dict = new Dictionary(sep_interface_dir, lang);
					result = dict.get(key, args);
				}

				if (result == null && !interface_name.equals("default"))
				{ // not found, try the default interface
					dict = new Dictionary("interface_default", lang);
					result = dict.get(key, args);
				}

				if (result == null)
				{ // not found
					result = "_" + original_key + "_";
				}

				bundle += "	<item key=\"" + encode(original_key) + "\" value=\"" + encode(result) + "\" />\r\n";
			}
		}

		bundle += "</textBundle>";

		response.setContentType("text/xml");

		w.print(bundle);
		w.close();
	}

	private String encode(String s)
	{
		s = StringUtils.replace(s, "&", "&amp;");
		s = StringUtils.replace(s, "<", "&lt;");
		s = StringUtils.replace(s, ">", "&gt;");
		return s;
	}

	private void displayParamError(HttpServletResponse response)
	{

		try
		{
			PrintWriter w = response.getWriter();
			w.write("Invalid parameters supplied! Need key (k=), interface name (i=) and language (l=). If you specified a method call, it must be on the list of supported method calls, and you must specify a command (c=) and parameters (p=).");
			w.close();
		}
		catch (Exception ex)
		{ /* Should log here */
		}

	}

}
