package org.greenstone.admin.gui;

import java.awt.Graphics;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNEventAction;


public class MatUpdateEventHandler extends Thread implements ISVNEventHandler, Runnable {
	
	JTextArea messageArea = new JTextArea();
	String localDirectory;
	String extensionName;
	public MatUpdateEventHandler(JTextArea messgaeTextArea, String dir, String extensionName) {
		
		
		messageArea = messgaeTextArea;
		messageArea.setEditable(false);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		
		localDirectory = dir;
		this.extensionName = extensionName;

	}
	
    public MatUpdateEventHandler() {
		// TODO Auto-generated constructor stub
	}

	public void handleEvent(SVNEvent event, double progress) {
        /*
         * Gets the current action. An action is represented by SVNEventAction.
         * In case of an update an  action  can  be  determined  via  comparing 
         * SVNEvent.getAction() and SVNEventAction.UPDATE_-like constants. 
         */
		
		//System.out.println(" event occurs");
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
            System.out.println("Fetching external item into '"
                    + event.getFile().getAbsolutePath() + "'");
            System.out.println("External at revision " + event.getRevision());
            return;
        } else if (action == SVNEventAction.UPDATE_COMPLETED) {
            /*
             * Updating the working copy is completed. Prints out the revision.
             */
            //System.out.println("At revision " + event.getRevision());
        	messageArea.append("At revision " + event.getRevision()+"\n");
        	messageArea.append("The extension ("+  extensionName +") has been downloaded to the local folder: \n"+ localDirectory +"\n");
        	messageArea.setSelectionEnd(messageArea.getDocument().getLength());
            return;
        } else if (action == SVNEventAction.ADD){
            System.out.println("A     " +  event.getURL().getPath());
            return;
        } else if (action == SVNEventAction.DELETE){
            System.out.println("D     " +  event.getURL().getPath());
            return;
        } else if (action == SVNEventAction.LOCKED){
            System.out.println("L     " +  event.getURL().getPath());
            return;
        } else if (action == SVNEventAction.LOCK_FAILED){
            System.out.println("failed to lock    " +  event.getURL().getPath());
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

        
    String content = pathChangeType
    + propertiesChangeType
    + lockLabel
    + "       "
    +  event.getURL().getPath()+"\n";

       messageArea.append(content);
       messageArea.setSelectionEnd(messageArea.getDocument().getLength());




    }

    public void checkCancelled() throws SVNCancelException {    }

} 
