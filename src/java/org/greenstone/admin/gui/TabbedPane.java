/**
 *#########################################################################
 *
 * A component of the Gatherer application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * Author: Chi-Yu Huang, Greenstone Digital Library, University of Waikato
 *
 * Copyright (C) 1999 New Zealand Digital Library Project
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
 *########################################################################
 */

package org.greenstone.admin.gui;


import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.KeyEvent;

public class TabbedPane extends JPanel {

    private static final Dimension SIZE = new Dimension(800, 540);

    public TabbedPane() {
	super(new GridLayout(1, 1));
	setBackground(new Color(176,208,176));
	
        JTabbedPane tabbedPane = new JTabbedPane();
        JComponent panel1 = makeTextPanel("Log information Pane");
	/*   tabbedPane.addTab("Log Pane", icon, panel1,
	     "Log Pane");*/
	panel1.setPreferredSize(SIZE);
	panel1.setBackground(new Color(176,208,176));
	tabbedPane.addTab("Log Pane", panel1);
	tabbedPane.setMnemonicAt(0, KeyEvent.VK_L);
      

        JComponent panel2 = makeTextPanel("Runtime Status");
        /*tabbedPane.addTab("Status Pane", icon, panel2,
	  "Status Pane");*/

	panel2.setPreferredSize(SIZE);
	panel2.setBackground(new Color(176,208,176));
	tabbedPane.addTab("Status Pane", panel2);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_R);

        //Add the tabbed pane to this panel.
        add(tabbedPane);
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TabbedPane.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
