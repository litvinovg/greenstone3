/*
 *    MyNodeList.java
 *   

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

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class MyNodeList implements NodeList {
    private ArrayList<Node> node_list = null;
    
    public MyNodeList() {
	node_list = new ArrayList<Node>();
    }
    public int getLength() {
	return node_list.size();
    }
    
    public Node item(int i) {
	if (i<0 || i >= node_list.size()) return null;
	return node_list.get(i);
    }
    
    public void addNode(Node n) {
	node_list.add(n);
    }
}
