package org.greenstone.admin.gui;

import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;

public class MatWorkingCopy {

    private static SVNClientManager ourClientManager;
    private static ISVNEventHandler myMatUpdateEventHandler;
    public static boolean suc = true; 
    JTextArea messageTextArea;

    
    public void Download(JTextArea messageArea,String svnURL, String destination, String extensionName) throws SVNException {
    	
    	messageTextArea = messageArea;
        setupLibrary();
        SVNURL repositoryURL = null;
        try {
            repositoryURL = SVNURL.parseURIEncoded(svnURL);
        } catch (SVNException e) {
        	e.printStackTrace();
        }
        String name = "";
        String password = "";
        String myMatWorkingCopyPath = destination;

 
        SVNURL url = repositoryURL;
 
        myMatUpdateEventHandler = new MatUpdateEventHandler(messageTextArea, destination, extensionName);
       
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
     
        ourClientManager = SVNClientManager.newInstance(options, name, password);
     
        ourClientManager.getUpdateClient().setEventHandler(myMatUpdateEventHandler);

        File wcDir = new File(myMatWorkingCopyPath);
        if (wcDir.exists()) {
        	
        	Object[] option = {"Remove it!","Keep it!"};
        	int n = JOptionPane.showOptionDialog(new JFrame(),"The folder '"+ wcDir.getAbsolutePath() + "' already exists!","Attention", 
        			JOptionPane.YES_NO_CANCEL_OPTION,
        			JOptionPane.QUESTION_MESSAGE,
        			null,
        			option,
        			option[0]);

        	if(n == 0){
        		deleteDir(wcDir);
        	}
        	else{return;}
        }
        wcDir.mkdirs();

        messageTextArea.append("Checking out a working copy from '" + url + "'...\n");
   
        try {
          checkout(url, SVNRevision.HEAD, wcDir, true);
        } catch (SVNException svne) {
            error("error while checking out a working copy for the location '"
                            + url + "'", svne);
        }

    }

    private static boolean deleteDir(File dir) {
        
    	if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteDir(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
    	return dir.delete();
        } 

    private static void setupLibrary() {

        DAVRepositoryFactory.setup();

        SVNRepositoryFactoryImpl.setup();

        FSRepositoryFactory.setup();
    }

    private static void checkout(SVNURL url,
            SVNRevision revision, File destPath, boolean isRecursive)
            throws SVNException {

        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
    
        updateClient.setIgnoreExternals(false);

        long x = updateClient.doCheckout(url, destPath, revision, revision, isRecursive);

    }

	public boolean getStatus(){	
		return suc;
	}

    private static void error(String message, Exception e){
        System.err.println(message+(e!=null ? ": "+e.getMessage() : ""));
        JOptionPane.showMessageDialog(new JFrame(),Mat.DownloadErrorMsg);	
	suc = false;
    }
} 
