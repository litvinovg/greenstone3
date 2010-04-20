package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.io.File;

import java.lang.reflect.Constructor;

public class RemoveStep extends Step
{
    public RemoveStep(DownloadStep dls, SequenceList parent)
    {
	super("AUTOMATIC_REMOVE", "Remove Extension", dls.getId(), dls.getId(), parent);

	_button.addActionListener(new RemoveButtonListener());   
    }

    public RemoveStep(Element removeStepElement, SequenceList parent)
    {
	super(removeStepElement, parent);

	if(removeStepElement != null){	    
	    _button.addActionListener(new RemoveButtonListener());   
	}
	else{
	     System.err.println("This remove <" + ExtXMLHelper.STEP + "> element is null");
	}
    }

    public class RemoveButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    _button.setEnabled(false);
	    _button.setText("Removing...");
	    ExtensionInformation info = _parent.getParent();
	    JPanel descPanel = new JPanel();
	    descPanel.setLayout(new BorderLayout());

	    JTextArea descArea = new JTextArea();
	    descArea.setEditable(false);
	    descArea.setText(info.getDescription());
	    descArea.setLineWrap(true);
	    descArea.setWrapStyleWord(true);

	    descPanel.add(descArea);

	    _parent.getParent().changeExtPane(descPanel);

	    ExtPane.deleteDir(new File(info.getExtensionDirectory()));

	    _button.setEnabled(true);
	    _button.setText(_label);

	    _parent.clearPropertySteps();
	    _parent.registerStepCompletion(RemoveStep.this);
	}
    }
}