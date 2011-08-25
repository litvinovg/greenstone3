package org.greenstone.server;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.log4j.*;
/**
 * Base class to help start or restart the library server
 * 
 */
public abstract class BaseServerControl extends JFrame {

    static Logger logger = Logger.getLogger(BaseServerControl.class.getName());
 
    /** The dimension of the frame */
    static final private Dimension FRAME_SIZE = new Dimension(350, 250);

    Color bg_color = Color.white;    
    
    /** some components we need to refer to later */
    protected JLabel info_label;
    protected JButton enter_button;
    protected JMenu fMenu;
    protected BaseServer server;
    protected JFrame thisframe;
    
    public BaseServerControl(BaseServer server,String frame_title) 
    {
	super(frame_title);
 
	this.server = server;
        thisframe = this;

	setSize(FRAME_SIZE);
	setDefaultCloseOperation(EXIT_ON_CLOSE);

	// set the icon for the Greenstone Server Interface
	try {
	    ImageIcon image = new ImageIcon(getClass().getResource("/images/servericon.png"));
	    if (image != null) {
		this.setIconImage(image.getImage());
	    }	
	}
	catch (Exception exception) {
	    System.err.println("Error: Could not load servericon.png");
	    logger.error("Error: Could not load servericon.png");
	}	

	Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((screen_size.width - FRAME_SIZE.width) / 2,
		    (screen_size.height - FRAME_SIZE.height) / 2);
	setBackground(Color.white);
        addWindowListener(new MyAdapter()); 
   
        JPanel title_panel = new JPanel();
	title_panel.setLayout(new BorderLayout());

        JLabel title_label = new JLabel();


        String title = BaseServer.dictionary.get(BaseServer.Property.SERVER_CONTROL+".Title");           
        title_label.setText(stringToHTML(title)); 
        title_label.setOpaque(false);	
	title_label.setHorizontalAlignment(SwingConstants.CENTER);
        title_label.setFont(new Font("SansSerif",Font.PLAIN,18));
       

        JLabel version_label = new JLabel();
        String version = BaseServer.dictionary.get(BaseServer.Property.SERVER_CONTROL+".Version").toLowerCase();
         
        version_label.setText(stringToHTML(version)); 
        version_label.setOpaque(false);	
	version_label.setHorizontalAlignment(SwingConstants.CENTER);
        version_label.setFont(new Font("SansSerif",Font.PLAIN,14));
        
        title_panel.add(title_label,BorderLayout.CENTER);
        title_panel.add(version_label,BorderLayout.SOUTH);
	title_panel.setBackground(bg_color);	
	title_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

   	info_label = new JLabel();
        info_label.setOpaque(false);
        info_label.setHorizontalAlignment(SwingConstants.LEFT);
        info_label.setVerticalAlignment(SwingConstants.CENTER);
        info_label.setFont(new Font("SansSerif",Font.PLAIN,14));
	info_label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			
        JPanel button_panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	enter_button = new JButton();
	enter_button.setEnabled(false);
	enter_button.addActionListener(new EnterButtonListener());
        enter_button.setText(BaseServer.dictionary.get("ServerControl.EnterLibrary"));
	button_panel.add(enter_button);
	button_panel.setBackground(bg_color);	
	button_panel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
	getRootPane().setDefaultButton(enter_button); // button gets the focus for enterpress

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(title_panel,BorderLayout.NORTH);
	getContentPane().add(info_label,BorderLayout.CENTER);
	getContentPane().add(button_panel,BorderLayout.SOUTH);
	getContentPane().setBackground(bg_color);
    	setJMenuBar(createMenu());
	setVisible(true);
    }

    protected abstract JMenuBar createMenu();

    protected JMenuBar createMenu(JMenuItem iConf) {
	JMenuBar menuBar = new JMenuBar();
         fMenu = new JMenu(BaseServer.dictionary.get("ServerControl.Menu.File"));
	JMenuItem iExit = new JMenuItem(BaseServer.dictionary.get("ServerControl.Menu.Exit"));
	iExit.setBackground(Color.white);
	iExit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    /*Thread runInThread = new Thread(new Runnable(){
		      public void run(){
		      server.stop();
		      
		      }
		      },"stop server");
		      try{
		      runInThread.start(); 
		      
		      }
		    catch(Exception e){
		    logger.error(e);
		    }*/    
		    thisframe.dispose();
		    server.stop();
		    System.exit(0);
		}
	    });

	fMenu.add(iConf);
	fMenu.add(iExit);
	fMenu.setEnabled(false);
	menuBar.add(fMenu);
	menuBar.setBorder(new BevelBorder(BevelBorder.RAISED));
	return menuBar;
    }

    public void updateControl(){
      	switch (server.getServerState()){
	case BaseServer.START_SERVER:
	    {
		info_label.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.Help_EnterLibrary")));
		enter_button.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.EnterLibrary")));
		enter_button.setEnabled(true);
		fMenu.setEnabled(true);
		break;
	    }
	case BaseServer.SERVER_STARTED:
	    {
		info_label.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.Help_RestartLibrary")));
		enter_button.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.RestartLibrary")));
		enter_button.setEnabled(true);
		fMenu.setEnabled(true);
		break;
	    }
	case BaseServer.SERVER_START_FAILED:
	    {
		enter_button.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.StartServer")));
		enter_button.setEnabled(true);
		fMenu.setEnabled(true);
		break;	
	    }
	case BaseServer.BROWSER_LAUNCHED:
	    {
		info_label.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.BrowserLaunched",new String[]{server.getBrowserURL()})
						+ BaseServer.dictionary.get("ServerControl.Help_RestartLibrary")));
		enter_button.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.RestartLibrary")));
		thisframe.setState(Frame.ICONIFIED);
		enter_button.setEnabled(true);
                fMenu.setEnabled(true);
		break;
	    }
	case BaseServer.BROWSER_LAUNCH_FAILED:
	    {
		info_label.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.BrowserLaunchFailed",new String[]{server.getBrowserURL()})));
		enter_button.setText(stringToHTML(BaseServer.dictionary.get("ServerControl.EnterLibrary")));
		enter_button.setEnabled(true);
		fMenu.setEnabled(true);
		break;
	    }
        default:
	    {
		enter_button.setText(BaseServer.dictionary.get("ServerControl.EnterLibrary"));
		enter_button.setEnabled(false);
		fMenu.setEnabled(false);
	    }
	}
    }
           
   
    public void displayMessage(String message){
	info_label.setText(stringToHTML("<br>"+message));           
    }

    private class MyAdapter extends WindowAdapter{   
	public void windowClosing(WindowEvent env){
	    /*          Thread runInThread = new Thread(new Runnable(){
			public void run(){
			server.stop();
			
			}
			},"stop server");
			try{
			runInThread.start(); 
			}
			catch(Exception e){
			logger.error(e);
			}  
			thisframe.dispose();
	    */
	    thisframe.dispose();
	    server.stop();
	    System.exit(0);
	}
    } 

    private String stringToHTML(String s){
	return "<html><body>"+s+"</body></html>";	
    }
    
    private class EnterButtonListener
	implements ActionListener {
	
	public void actionPerformed(ActionEvent ev) {
	    switch (server.getServerState()){
	    case BaseServer.START_SERVER:
		{
		 Thread runInThread = new Thread(new Runnable(){
			    public void run(){
				server.start();
				server.launchBrowser();
			    }
			},"start server and launch browser");
		    
		    runInThread.start(); 
                   break;     
		}
	    case BaseServer.SERVER_STARTED:
		{
		 Thread runInThread = new Thread(new Runnable(){
			    public void run(){
				server.launchBrowser();
			    }
			},"launch browser");
		    
		    runInThread.start(); 
                   break;     
		}
	    case BaseServer.SERVER_START_FAILED:
		{
		    Thread runInThread = new Thread(new Runnable(){
			    public void run(){
				server.start();
			    }
			},"start server");
		    runInThread.start();
		    break;
		}
	    case BaseServer.BROWSER_LAUNCHED: case BaseServer.BROWSER_LAUNCH_FAILED:
		{
		    Thread runInThread = new Thread(new Runnable(){
			    public void run(){
				server.restart();
				  
			    }
			},"restart server");
		    runInThread.start();
		}  
	    }   
	  
	}
    }
	
	public void errorMessage(String message) {
		JOptionPane.showMessageDialog(null,message,"Error", JOptionPane.ERROR_MESSAGE);
	}
}
