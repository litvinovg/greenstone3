/*
 *    Communicator.java
 *    Copyright (C) 2002 New Zealand Digital Library, http://www.nzdl.org
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
package org.greenstone.gsdl3.comms;

import org.greenstone.gsdl3.core.ModuleInterface;
import org.greenstone.gsdl3.util.*;

//XML packages
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Communicator - base class for Modules that talk via some protocol to other modules
 *
 * can be used by a MessageRouter - in this case set localSiteName
 * can be used by any other module - in this case it has no local site - dont set localsiteName. the setting of localSiteName affects the handling of teh to and from fields in the message. 
 * @author <a href="mailto:kjdon@cs.waikato.ac.nz">Katherine Don</a>
 * @version $Revision$
 */
abstract public class Communicator
    implements ModuleInterface {

    /** name of local site */
    protected String local_site_name_ = null;

    /** name of site to connect to */
    protected String remote_site_name_=null;

    /** converter for String to DOM and vice versa */
    protected XMLConverter converter_= null;

    public Communicator() {
	converter_ = new XMLConverter();
    }
    public void cleanUp() {}

    public void setLocalSiteName(String name) {
	local_site_name_ = name;
    }
    /** this should be done as part of configure */
    public void setRemoteSiteName(String name) {
	remote_site_name_ = name;
    }
    /** configures the Communicator using the <site> element */
    abstract public boolean configure(Element site_elem); 

    public String process(String xml_in) {
	Node n = converter_.getDOM(xml_in);
	Node result = process(n);
	return converter_.getString(result);	
    }

    abstract public Node process(Node xml_in_node);
}
