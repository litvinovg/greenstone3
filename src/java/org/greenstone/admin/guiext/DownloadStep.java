package org.greenstone.admin.guiext;

import org.w3c.dom.Element;

import java.util.ArrayList;

import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNCancelException;

import org.greenstone.admin.GAI;
import org.greenstone.admin.LoggedMessageArea;

public class DownloadStep extends Step
{
    Source _mainSource = null;
    Source[] _auxSources = null;
    LoggedMessageArea _messageArea = new LoggedMessageArea(this.getClass());

    public DownloadStep(SequenceList parent)
    {
	super("AUTOMATIC_DOWNLOAD", "Download Extension", "", "", parent);

	ExtensionInformation info = _parent.getParent();
	
	_mainSource = new Source("svn", info.getBaseURL() + info.getFileStem() + "/trunk/src", "", this);

	_button.addActionListener(new DownloadButtonListener());
    }

    public DownloadStep(Element downloadStepElement, SequenceList parent)
    {
	super(downloadStepElement, parent);
	
	if(downloadStepElement != null){    
	    Element mainSourceElement = ExtXMLHelper.getSingleChildElement(downloadStepElement, ExtXMLHelper.MAIN_SOURCE, true);
	    if(mainSourceElement == null){
		System.err.println("This download <" + ExtXMLHelper.STEP + "> element has no <" + ExtXMLHelper.MAIN_SOURCE + "> element");
	    }
	    else{
		_mainSource = new Source(mainSourceElement, this);
	    }
	
	    Element[] auxSourceElements = ExtXMLHelper.getMultipleChildElements(downloadStepElement, ExtXMLHelper.AUX_SOURCE, false);
	    if(auxSourceElements != null){
		_auxSources = new Source[auxSourceElements.length];
		for(int i = 0; i < auxSourceElements.length; i++){
		    _auxSources[i] = new Source(auxSourceElements[i], this);
		}
	    }
	    
	    _button = new JButton(_label);
	    _button.setEnabled(false);
	    _button.addActionListener(new DownloadButtonListener());
	}
	else{
	    System.err.println("This download <" + ExtXMLHelper.STEP + "> element is null");
	}
    }

    public class DownloadButtonListener implements ActionListener
    {
	boolean _svnInitialised = false;
	public DownloadButtonListener()
	{
	    if(!_svnInitialised){
		//Setup the SVN client system
		DAVRepositoryFactory.setup();
		SVNRepositoryFactoryImpl.setup();
		FSRepositoryFactory.setup();
	    }
	    _svnInitialised = true;
	}
	
	public void actionPerformed(ActionEvent e)
	{
	    _button.setEnabled(false);
	    _button.setText("Downloading...");

	    DownloadThread downloadThread = new DownloadThread();
	    downloadThread.start();
	}
    }

    public class DownloadThread extends Thread
    {
	public void run()
	{
	    ExtensionInformation info = _parent.getParent();

	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());
	    panel.add(_messageArea, BorderLayout.CENTER);
	    info.changeExtPane(panel);

	    String fileStem = info.getFileStem();
	    String defaultDownloadLocation = GAI.getGSDL3ExtensionHome() + System.getProperty("file.separator") + fileStem;

	    ArrayList sourceElements = new ArrayList();
	    sourceElements.add(_mainSource);

	    if(_auxSources != null){
		for(int i = 0; i < _auxSources.length; i++){
		    sourceElements.add(_auxSources[i]);
		}
	    }

	    for(int i = 0; i < sourceElements.size(); i++){
		
		Source currentSource = (Source)sourceElements.get(i);
		String destinationFolder = null;

		if(currentSource.getMethod().equals("svn")){
		    String name = "";
		    String password = "";

		    //Setup the SVN client
		    SVNClientManager svnManager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(true), name, password);
		    SVNUpdateClient svnCheckout = svnManager.getUpdateClient();
		    svnCheckout.setEventHandler(new SVNCheckoutEventHandler());
		    svnCheckout.setIgnoreExternals(false);

		    //The i != 0 enforces that the mainSource should always go to the default download location
		    if(i != 0 && !currentSource.getFolder().equals("")){
			destinationFolder = currentSource.getFolder();
		    }
		    else{
			destinationFolder = defaultDownloadLocation;
		    }
		    
		    //Check if the directory already exists. If it does exist then as the use if they wish to delete it
		    File wcDir = new File(destinationFolder);
		    if (wcDir.exists()) {
			
			Object[] option = {"Continue","Cancel"};
			int n = JOptionPane.showOptionDialog(new JFrame(),"The folder for this extension already exists, continuing will remove all the files within this folder before proceeding.","Attention", 
							     JOptionPane.YES_NO_CANCEL_OPTION,
							     JOptionPane.QUESTION_MESSAGE,
							     null,
							     option,
							     option[0]);
			if(n == 0){
			    ExtPane.deleteDir(wcDir);
			}
			else{
			    _button.setEnabled(true);
			    _button.setText(_label);
			    return;
			}
		    }
		    wcDir.mkdirs();
		    
		    //Perform the specified checkout
		    String url = currentSource.getURL();
		    try{
			SVNURL repositoryURL = SVNURL.parseURIEncoded(url);
			long x = svnCheckout.doCheckout(repositoryURL, wcDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);	
		    }
		    catch(SVNException ex){
			ex.printStackTrace();
			_messageArea.append("Error retrieving file from SVN");
			_button.setEnabled(true);
			_button.setText(_label);
			return;
		    }
		}
	    }
	    
	    _button.setEnabled(true);
	    _button.setText(_label);
	    _parent.registerStepCompletion(DownloadStep.this);
	}
    }

    public class SVNCheckoutEventHandler implements ISVNEventHandler
    {	
	public void handleEvent(SVNEvent event, double progress)
	{
	    /*
	     * Gets the current action. An action is represented by SVNEventAction.
	     * In case of an update an  action  can  be  determined  via  comparing 
	     * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants. 
	     */
		
	    //_messageArea.append(" event occurs");
	    SVNEventAction action = event.getAction();
	    String pathChangeType = " ";
	    if (action == SVNEventAction.UPDATE_ADD) {
		/*
		 * the item was added
		 */
		pathChangeType = "A";
	    } else if (action == SVNEventAction.UPDATE_DELETE) {
		/*
		 * the item was deleted
		 */
		pathChangeType = "D";
	    } else if (action == SVNEventAction.UPDATE_UPDATE) {
		/*
		 * Find out in details what  state the item is (after  having  been 
		 * updated).
		 * 
		 * Gets  the  status  of  file/directory  item   contents.  It   is 
		 * SVNStatusType  who contains information on the state of an item.
		 */
		SVNStatusType contentsStatus = event.getContentsStatus();
		if (contentsStatus == SVNStatusType.CHANGED) {
		    /*
		     * the  item  was  modified in the repository (got  the changes 
		     * from the repository
		     */
		    pathChangeType = "U";
		}else if (contentsStatus == SVNStatusType.CONFLICTED) {
		    /*
		     * The file item is in  a  state  of Conflict. That is, changes
		     * received from the repository during an update, overlap  with 
		     * local changes the user has in his working copy.
		     */
		    pathChangeType = "C";
		} else if (contentsStatus == SVNStatusType.MERGED) {
		    /*
		     * The file item was merGed (those  changes that came from  the 
		     * repository  did  not  overlap local changes and were  merged 
		     * into the file).
		     */
		    pathChangeType = "G";
		}
	    } else if (action == SVNEventAction.UPDATE_EXTERNAL) {
		/*for externals definitions*/
		_messageArea.append("Fetching external item into '"
				   + event.getFile().getAbsolutePath() + "'");
		_messageArea.append("External at revision " + event.getRevision());
		return;
	    } else if (action == SVNEventAction.UPDATE_COMPLETED) {
		/*
		 * Updating the working copy is completed. Prints out the revision.
		 */
		//_messageArea.append("At revision " + event.getRevision());
        	_messageArea.append("At revision " + event.getRevision()+"\n");
        	//_messageArea.append("The extension ("+  _extensionName +") has been downloaded to the local folder: \n"+ _destination +"\n"); //xxxxx
        	_messageArea.setSelectionEnd(_messageArea.getDocument().getLength());
		return;
	    } else if (action == SVNEventAction.ADD){
		_messageArea.append("A     " +  event.getURL().getPath());
		return;
	    } else if (action == SVNEventAction.DELETE){
		_messageArea.append("D     " +  event.getURL().getPath());
		return;
	    } else if (action == SVNEventAction.LOCKED){
		_messageArea.append("L     " +  event.getURL().getPath());
		return;
	    } else if (action == SVNEventAction.LOCK_FAILED){
		_messageArea.append("failed to lock    " +  event.getURL().getPath());
		return;
	    }
	    
	    /*
	     * Now getting the status of properties of an item. SVNStatusType  also
	     * contains information on the properties state.
	     */
	    SVNStatusType propertiesStatus = event.getPropertiesStatus();
	    /*
	     * At first consider properties are normal (unchanged).
	     */
	    String propertiesChangeType = " ";
	    if (propertiesStatus == SVNStatusType.CHANGED) {
		/*
		 * Properties were updated.
		 */
		propertiesChangeType = "U";
	    } else if (propertiesStatus == SVNStatusType.CONFLICTED) {
		/*
		 * Properties are in conflict with the repository.
		 */
		propertiesChangeType = "C";
	    } else if (propertiesStatus == SVNStatusType.MERGED) {
		/*
		 * Properties that came from the repository were  merged  with  the
		 * local ones.
		 */
		propertiesChangeType = "G";
	    }
	    
	    /*
	     * Gets the status of the lock.
	     */
	    String lockLabel = " ";
	    SVNStatusType lockType = event.getLockStatus();
	    
	    if (lockType == SVNStatusType.LOCK_UNLOCKED) {
		/*
		 * The lock is broken by someone.
		 */
		lockLabel = "B";
	    }

	    /*
	    System.err.println("pathChangeType = " + pathChangeType);
	    System.err.println("propertiesChangeType = " + propertiesChangeType);
	    System.err.println("lockLabel = " + lockLabel);
	    System.err.println("event = " + event);
	    System.err.println("event.getURL = " + event.getURL());
	    */

	    final String content = pathChangeType
		+ propertiesChangeType
		+ lockLabel
		+ "       "
		+ (event.getURL() == null ? "" : event.getURL().getPath()) + "\n";

	    _messageArea.append(content);
	    _messageArea.setSelectionEnd(_messageArea.getDocument().getLength());
	}

	public void checkCancelled() throws SVNCancelException
	{
	}
    }
}