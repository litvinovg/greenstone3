/**********************************************************************
 *
 * PhindTitle.java -- backdrops to empty ResultDisplay panels.
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

PhindTitle is for drawing backdrops in empty ResultDisplay panels.

**********************************************************************/
package org.greenstone.applet.phind;
//package org.nzdl.gsdl.Phind;

import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

public class PhindTitle extends Canvas {

    Phind phind;
    static Image backgroundImage;
    boolean displayInfo;
    
    PhindTitle(Phind p) {
	phind = p;
    }

    public void update(Graphics g) {
	paint(g);
    }

    public void paint(Graphics g) {
	Dimension canvasSize = getSize();

	Color fore = phind.panel_fg;
	Color back = phind.panel_bg;

	// set the screen background
	if (phind.showImage)
	    try {
		g.drawImage(phind.backgroundImage, 
			    0, 0, canvasSize.width, canvasSize.height, back, null);
	    } catch (Exception e) {
		System.err.println("PhindTitle paint: "  + e);
	    }
	else {
	    g.setColor(back);
	    g.fillRect(0,0, canvasSize.width, canvasSize.height);
	}
    }
}

 





