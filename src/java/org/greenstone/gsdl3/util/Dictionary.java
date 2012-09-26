/*
 *    Dictionary.java
 *    Copyright (C) 2005 New Zealand Digital Library, http://www.nzdl.org
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.greenstone.gsdl3.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.Enumeration;

import org.apache.log4j.*;

public class Dictionary
{

	static Logger logger = Logger.getLogger(org.greenstone.gsdl3.util.Dictionary.class.getName());

	/** The locale of this dictionary. */
	protected Locale locale = null;

	/** The resource that has been loaded */
	protected String resource = null;

	/**
	 * The ResourceBundle which contains the raw key-value mappings. Loaded from
	 * a file named "resource_locale.properties
	 */
	private ResourceBundle raw = null;

	/**
	 * Constructs the Dictionary class by first creating a locale from the
	 * specified language, (or using the default locale if this didn't work),
	 * then loading a resource bundle based on this locale.
	 */
	public Dictionary(String resource, String lang)
	{
		// Initialize.

		this.locale = new Locale(lang);
		this.resource = resource;
		if (this.locale == null)
		{
			this.locale = Locale.getDefault();
		}
		try
		{
			this.raw = ResourceBundle.getBundle(this.resource, this.locale);
		}
		catch (Exception e)
		{
			//logger.debug("Dictionary: couldn't locate a resource bundle for "+resource);
		}
	}

	public Dictionary(String resource, Locale locale)
	{
		this.locale = locale;
		this.resource = resource;
		try
		{
			this.raw = ResourceBundle.getBundle(this.resource, this.locale);
		}
		catch (Exception e)
		{
			//logger.debug("Dictionary: couldn't locate a resource bundle for "+resource);
		}
	}

	/**
	 * Constructs the Dictionary class by first creating a locale from the
	 * specified language, (or using the default locale if this didn't work),
	 * then loading a resource bundle based on this locale. A classloader is
	 * specified which can be used to find the resource.
	 */
	public Dictionary(String resource, String lang, ClassLoader loader)
	{
		// Initialize.
		this.locale = new Locale(lang);
		this.resource = resource;
		if (this.locale == null)
		{
			this.locale = Locale.getDefault();
		}
		// try the specified class loader
		try
		{
			this.raw = ResourceBundle.getBundle(this.resource, this.locale, loader);
			return;
		}
		catch (Exception e)
		{
		}
		;

		try
		{
			this.raw = ResourceBundle.getBundle(this.resource, this.locale);
		}
		catch (Exception ex)
		{
			//logger.debug("Dictionary: couldn't locate a resource bundle for "+resource);
		}
	}

	public Enumeration getKeys()
	{
		if (this.raw != null)
		{
			return this.raw.getKeys();
		}
		return null;
	}

	/**
	 * Overloaded to call get with both a key and an empty argument array.
	 * 
	 * @param key
	 *            A <strong>String</strong> which is mapped to a initial String
	 *            within the ResourceBundle.
	 * @return A <strong>String</strong> which has been referenced by the key
	 *         String and that contains no argument fields.
	 */
	public String get(String key)
	{
		return get(key, null);
	}

	/**
	 * Used to retrieve a property value from the Locale specific
	 * ResourceBundle, based upon the key and arguments supplied. If the key
	 * cannot be found or if some other part of the call fails a default
	 * (English) error message is returned. <BR>
	 * Here the get recieves a second argument which is an array of Strings used
	 * to populate argument fields, denoted {<I>n</I>}, within the value String
	 * returned.
	 * 
	 * @param key
	 *            A <strong>String</strong> which is mapped to a initial String
	 *            within the ResourceBundle.
	 * @param args
	 *            A <strong>String[]</strong> used to populate argument fields
	 *            within the complete String.
	 * @return A <strong>String</strong> which has been referenced by the key
	 *         String and that either contains no argument fields, or has had
	 *         the argument fields populated with argument Strings provided in
	 *         the get call.
	 */
	public String get(String key, String args[])
	{
		String argsStr = "";
		if (args != null)
		{
			for (String arg : args)
			{
				argsStr += arg + " ";
			}
		}

		if (this.raw == null)
		{
			return null;
		}
		try
		{
			String initial_raw = this.raw.getString(key);
			// convert to unicode, copied from gatherer dictionary
			String initial = new String(initial_raw.getBytes("ISO-8859-1"), "UTF-8");

			// Remove any comments from the string
			if (initial.indexOf("#") != -1)
			{
				initial = initial.substring(0, initial.indexOf("#"));
			}
			// if we haven't been given any args, don't bother looking for them
			if (args == null)
			{
				return initial;
			}
			// If the string contains arguments we have to insert them.
			StringBuffer complete = new StringBuffer();
			// While we still have initial string left.
			while (initial.length() > 0 && initial.indexOf('{') != -1 && initial.indexOf('}') != -1)
			{
				// Remove preamble
				int opening = initial.indexOf('{');
				int closing = initial.indexOf('}');
				int comment_mark = initial.indexOf('-', opening); // May not exist
				if (comment_mark > closing)
				{ // May also be detecting a later comment
					comment_mark = -1;
				}
				complete.append(initial.substring(0, opening));

				// Parse arg_num
				String arg_str = null;
				if (comment_mark != -1)
				{
					arg_str = initial.substring(opening + 1, comment_mark);
				}
				else
				{
					arg_str = initial.substring(opening + 1, closing);
				}
				if (closing + 1 < initial.length())
				{
					initial = initial.substring(closing + 1);
				}
				else
				{
					initial = "";
				}
				int arg_num = Integer.parseInt(arg_str);
				// Insert argument
				if (args != null && 0 <= arg_num && arg_num < args.length)
				{
					complete.append(args[arg_num]);
				}
			}
			complete.append(initial);
			return complete.toString();
		}
		catch (Exception e)
		{
			//logger.debug("Dictionary Error: couldn't find string for key:" + key +" in resource "+this.resource);
			return null;
		}
	}
}
