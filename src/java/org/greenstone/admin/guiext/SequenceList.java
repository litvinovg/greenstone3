package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import org.greenstone.admin.GAI;

import java.util.Properties;
import java.util.ArrayList;

import javax.swing.JButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SequenceList
{
    protected ArrayList<Step> _steps = new ArrayList<Step>();
    protected ExtensionInformation _parent = null;
    protected Properties _dependencies = null;

    public SequenceList(Element sequenceElement, ExtensionInformation parent)
    {
	_parent = parent;

	loadExtensionStatesFromFile();

	if(sequenceElement != null){
	    Element[] stepElements = ExtXMLHelper.getMultipleChildElements(sequenceElement, ExtXMLHelper.STEP, true);
	    if(stepElements == null || stepElements.length == 0){
		System.err.println("This <" + ExtXMLHelper.SEQUENCE_LIST + "> element does not have any valid <" + ExtXMLHelper.STEP + "> elements");
	    }
	    else{
		for(int i = 0; i < stepElements.length; i++){
		    Element currentStep = stepElements[i];
		    String action = currentStep.getAttribute("action");
		    
		    if(i == 0 && !action.equals("download")){
			_steps.add(new DownloadStep(this));
		    }

		    if(action.equals("download")){
			_steps.add(new DownloadStep(currentStep, this));
		    }
		    else if(action.equals("properties")){
			_steps.add(new PropertiesStep(currentStep, this));
		    }
		    else if(action.equals("button")){
			_steps.add(new CommandStep(currentStep, this));
		    }
		    else if(action.equals("panel")){
			_steps.add(new PanelStep(currentStep, this));
		    }
		    else if(action.equals("remove")){
			_steps.add(new RemoveStep(currentStep, this));
		    }
		    else{
			System.err.println("\"" + action + "\" is not a valid action attribute for <" + ExtXMLHelper.STEP + "> elements (valid options are \"download\", \"properties\", \"panel\", \"button\") or \"remove\"");
		    }

		    if((i == stepElements.length-1) && !action.equals("remove")){
			_steps.add(new RemoveStep((DownloadStep)_steps.get(0), this));
		    }
		}
	    }
	}
	else{
	    System.err.println("This <" + ExtXMLHelper.SEQUENCE_LIST + "> element is null");
	}
    }
    
    public ExtensionInformation getParent()
    {
	return _parent;
    }

    public Step[] getSteps()
    {
	return (_steps.toArray(new Step[0]));
    }

    public JButton[] getButtons()
    {
	if(_steps == null){
	    System.err.println("Cannot get buttons for this sequence list as it has no steps");
	    return null;
	}
	
	JButton[] buttons = new JButton[_steps.size()];
	for(int i = 0; i < _steps.size(); i++){
	    buttons[i] = _steps.get(i).getButton();
	}
	
	return buttons;
    }

    private void loadExtensionStatesFromFile()
    {
	if(_dependencies == null){
	    _dependencies = new Properties();
	}

	String fileName = GAI.getGSDL3ExtensionHome() + System.getProperty("file.separator") + "extensionStates.properties";
	try{
	    File stateFile = new File(fileName);
	    
	    if(stateFile.exists()){
		_dependencies.load(new FileInputStream(new File(fileName)));
	    }
	}
	catch(Exception ex){
	    ex.printStackTrace();
	}
    }

    private void saveExtensionStatesToFile()
    {
	String fileName = GAI.getGSDL3ExtensionHome() + System.getProperty("file.separator") + "extensionStates.properties";
	try{
	    _dependencies.store(new FileOutputStream(new File(fileName)), null);
	}
	catch(Exception ex){
	    ex.printStackTrace();
	}
    }

    public boolean addDependency(String id)
    {
	if(_dependencies != null){
	    _dependencies.setProperty(_parent.getFileStem() + "." + id, "true");
	    saveExtensionStatesToFile();
	    return true;
	}
	else{
	    System.err.println("Cannot add dependency as it has not yet been loaded");
	    return false;
	}
    }

    public void rollbackTo(String id)
    {
	if(_dependencies != null){
	    rollbackToRecursive(id);
	    saveExtensionStatesToFile();
	}
	else{
	    System.err.println("Cannot remove dependency as it has not yet been loaded");
	}
    }

    private void rollbackToRecursive(String id)
    {
	Step rollbackStep = null;
	ArrayList<String> dependsSteps = new ArrayList<String>();
	for(int i = 0; i < _steps.size(); i++){
	    if((_steps.get(i)).getId().equals(id)){
		rollbackStep = _steps.get(i);
	    }
	    
	    String[] depends = _steps.get(i).getDependsOn().split(",");

	    for(int j = 0; j < depends.length; j++){
		if(depends[j].equals(id)){
		    dependsSteps.add(_steps.get(i).getId());
		}
	    }
	}
	
	_dependencies.setProperty(_parent.getFileStem() + "." + id, "false");

	for(int i = 0; i < dependsSteps.size(); i++){
	    rollbackToRecursive(dependsSteps.get(i));
	}
    }

    public void clearPropertySteps()
    {
	for(int i = 0; i < _steps.size(); i++){
	    Step currentStep = _steps.get(i);
	    if(currentStep instanceof PropertiesStep){
		((PropertiesStep)currentStep).setPropertiesToDefaults();
	    }
	}
    }

    public void registerStepCompletion(Step step)
    {
	addDependency(step.getId());
	   
	if(!step.getRollbacks().equals("")){
	    String[] rollbacks = step.getRollbacks().split(",");
	    for(int k = 0; k < rollbacks.length; k++){
		rollbackTo(rollbacks[k]);
	    }
	}

	updateButtons();
    }

    public void updateButtons()
    {
	loadExtensionStatesFromFile();

	ExtensionInformation currentExtension = _parent;
	String fileStem = currentExtension.getFileStem();

	File extDir = new File(currentExtension.getExtensionDirectory());
	if(!extDir.exists()){
	    rollbackTo("");
	}
	
	for(int i = 0; i < _steps.size(); i++){
	    Step currentStep = _steps.get(i);
	    JButton currentButton = currentStep.getButton();

	    //If this step button depends on another button then disabled it
	    String dependsOn = currentStep.getDependsOn();
	    if(dependsOn.equals("")){
		currentButton.setEnabled(true);
	    }
	    else{
		String[] dependsList = dependsOn.split(",");
		boolean enable = true;		
		for(int j = 0; j < dependsList.length; j++){
		    String propertyName = fileStem + "." + dependsList[j];

		    if(_dependencies.getProperty(propertyName) == null || _dependencies.getProperty(propertyName).equals("false")){
			enable = false;
		    }
		}
		currentButton.setEnabled(enable);
	    }
	}
    }
}