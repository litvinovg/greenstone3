/**********************************************************************
 *
 * ResultTitle.java -- describe the contents of a ResultBox
 *
 * Copyright 1997-2000 Gordon W. Paynter
 * Copyright 2000 The New Zealand Digital Library Project
 *
 * A component of the Greenstone digital library software
 * from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *********************************************************************/

/*********************************************************************

This class is used in the Phind java applet (Phind.java).

ResultTitle is part of the result list displayed to the user.  It lists the
phrase in the Result box, the number of expansions it occurs in, and the
number of documents it occurs in.

**********************************************************************/

//package org.nzdl.gsdl.Phind;
package org.greenstone.applet.phind;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Dimension;
import java.awt.Color;


public class ResultTitle extends Canvas {
    
    // Other objects associated with this 
    Phind phind;
    ResultBox parent;
    ResultCanvas canvas;

    // fonts and font spacings to use
    Font plain, bold;
    Graphics g;
    
    // Create a ResultTitle from the ResultBox which is its parent.
    ResultTitle(ResultBox p) {
	
	parent = p;
	phind = p.phind;
	canvas = p.c;
	g = getGraphics();
	
	plain = phind.plainFont;
	bold  = phind.boldFont;
	
	// Note: the size() and resize() methods are deprecated, but the newer
	// getSize() and setSize() methods fail in NetScape 4
	Dimension d = getSize();
	d.height = phind.fontSize + 10;
	setSize(d);
	
	
    }
    
    public void update(Graphics g) {
	paint(g);
    }
    
    public void paint(Graphics g) {
	
	// calculate the canvas size, margins, and spacing
	Dimension canvasSize = getSize();
	
	g.setFont(plain);
	int margin = g.getFontMetrics().stringWidth(" 8888 ");
	int y = phind.fontSize + 5;
	
	int rightMargin = canvas.getSize().width;
	int secondColumn = rightMargin - margin;
	int firstColumn = secondColumn - margin;
	
	g.setColor(Color.white);
	g.fillRect(0, 0, canvasSize.width, canvasSize.height);
	g.setColor(Color.black);
    
    
	// What is the phrase we are searching for?
	String phrase = parent.searchPhrase.replace('+', ' ');
	
	// Construct the description phrase
	String links = "";
	if (parent.numberOfThesaurusLinks <= 0) {
	    links = "";
	} else if (parent.numberOfThesaurusLinks == 1) { 
	    links = "1 link";
	} else if (parent.thesaurusLinksRetrieved == parent.numberOfThesaurusLinks) {
	    links = parent.numberOfThesaurusLinks + " links";
	} else {
	    links = parent.thesaurusLinksRetrieved + " of " 
		+ parent.numberOfThesaurusLinks + " links";
	}
	
	String expansions = "";
	if (parent.numberOfExpansions <= 0) {
	    expansions = "no phrases";
	} else if (parent.numberOfExpansions == 1) { 
	    expansions = "1 phrase";
	} else if (parent.expansionsRetrieved == parent.numberOfExpansions) {
	    expansions = parent.numberOfExpansions + " phrases";
	} else {
	    expansions = parent.expansionsRetrieved + " of " 
		+ parent.numberOfExpansions + " phrases";
	}
	
	String documents = "";
	if (parent.numberOfDocuments <= 0) { 
	    documents = "no documents";
	} else if (parent.documentsRetrieved == 1) {
	    documents = "1 document";
	} else if (parent.documentsRetrieved == parent.numberOfDocuments) {
	    documents = parent.numberOfDocuments + " documents";
	} else {
	    documents = parent.documentsRetrieved + " of " 
		+ parent.numberOfDocuments + " documents";
	}
	
	String status = "(";
	if (parent.numberOfThesaurusLinks > 0) {
	    status = status + links + ", ";
	} 
	status = status + expansions + ", " + documents + ")";
	
	
	// Draw the text 
	g.setFont(bold);
	g.drawString(phrase, 0, y);
	int tab = g.getFontMetrics().stringWidth(phrase + "  ");
	
	g.setFont(plain);
	g.drawString(status, tab, y);
	tab = tab + g.getFontMetrics().stringWidth(status);
	
	if (tab < firstColumn) {
	    g.drawString("docs", firstColumn, y);
	    g.drawString("freq", secondColumn, y);
	}
    }
}
