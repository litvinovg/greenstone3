/**
 *#########################################################################
 *
 * A component of the Core GUI application, part of the Greenstone digital
 * library suite from the New Zealand Digital Library Project at the
 * University of Waikato, New Zealand.
 *
 * <BR><BR>
 *
 * Author: John Thompson, Greenstone Digital Library, University of Waikato
 *
 * <BR><BR>
 *
 * Copyright (C) 1999 New Zealand Digital Library Project
 *
 * <BR><BR>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * <BR><BR>
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * <BR><BR>
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *########################################################################
 */
package org.greenstone.admin.gui;

import java.awt.*;
import java.lang.Object;
import javax.swing.*;

/** This class provides a progress bar to be displayed whenever the module 
 * is running some background tasks
 * @author John Thompson, Greenstone Digital Library, University of Waikato
 * @version 2.3
 */
public class ParsingProgress
    extends JDialog {
    //implements ActionListener {
    
    public final static int ONE_SECOND = 1000;

    /** The content pane within the dialog box. */
    private JPanel content_pane = null;
    private Timer timer;
    /** The progress bar itself. */
    private JProgressBar progressBar = null;
    
    /** The default size of the progress dialog. */
    static final Dimension SIZE = new Dimension(400,75);
    
    
    /** Constructor.
     * @param title The title to show on the dialog, as a <strong>String</strong>.
     * @param message The message to show on the dialog, as a <strong>String</strong>.
     * @param max The total amount of the time the process is running before 
     * the progress bar can be disposed of, as an <i>int</i>.
     */
    public ParsingProgress(String title, String message, int max) {
	super();
	this.setSize(SIZE);
	this.setTitle(title);
	//updateUI();

	// Creation
	this.content_pane = (JPanel) getContentPane();
	this.content_pane = new JPanel();
	
	this.progressBar = new JProgressBar();
	this.progressBar.setMaximum(max);
	this.progressBar.setIndeterminate(true);
       

	// Layout
	this.content_pane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	this.content_pane.setLayout(new BorderLayout());
	this.content_pane.add(new JLabel(message), BorderLayout.NORTH);
	this.content_pane.add(progressBar, BorderLayout.CENTER);
	
	//Create a timer
	//timer = new Timer (ONE_SECOND, new ActionListener() {
	//	public void actionPerformed (ActionEvent evt) {
		    progressBar.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    
		    //container.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    /*  progressBar.setValue(max);
			Toolkit.getDefaultToolkit().beep();
			timer.stop();
			startButton.setEnabled(true);
			setCursor(null); //turn off the wait cursor
			progressBar.setValue(progressBar.getMinimum());*/
		    //}
		    //});
	
	// Center and display
	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screen_size.width - SIZE.width) / 2,
		    (screen_size.height - SIZE.height) / 2);
	
	setVisible(true);
    }
    
    public void destroy() {
    }
    
    /** Method which increments the progress count by one, which should be called 
     ** after every successful parsing of a classifier or plugin.
     */
    public void inc() {
	this.progressBar.setValue(progressBar.getValue() + 1);
    }
}
