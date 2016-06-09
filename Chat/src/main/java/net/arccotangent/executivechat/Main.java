package net.arccotangent.executivechat;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileView;

import net.arccotangent.executivechat.dialog.MsgDialog;
import net.arccotangent.executivechat.net.IP;
import net.arccotangent.executivechat.net.Server;

public class Main extends Thread {

	private JFrame frmChatV;
	public static final Server SERVER = new Server();
	public static JCheckBox chkEnableServer;
	private static final String HOME_DIR = System.getProperty("user.home");
	public static final String CONTACT_DBDIR = HOME_DIR + "/.arcchatcontacts";
	public static final String KEY_DBDIR = HOME_DIR + "/.arcchatkeys";
	public static final String LOCAL_PUBFILE = KEY_DBDIR + "/_local.pub";
	public static final String LOCAL_PRIVFILE = KEY_DBDIR + "/_local.priv";
	//public static final String KEYSERVER_FILE = HOME_DIR + "/eckeyservers.txt";
	public static final String VERSION = "0.9"; //Update this and only this when updating version
	public static final String CHAT_VERSION = "Executive Chat v" + VERSION;
	public static final Font DIALOG_FONT = new Font("Courier", Font.PLAIN, 13);
	public static String LOCAL_IP_ADDRESS = "";
	public static final ChatLogger log = new ChatLogger();
	public static boolean debug = true;
	public static int DEFAULT_TIMEOUT_SEC = 2;
	public static final String PUBKEY_ALGORITHM = "RSA";
	public static final int PUBKEY_SIZE = 4096;
	public static final String SYMMETRIC_ALGORITHM = "AES";
	public static final int SYMMETRIC_SIZE = 256;
	public static final String SIGNATURE_TYPE = "SHA512withRSA";
	public static final String BLOCKED_IP_FILE = CONTACT_DBDIR + "/.blocked";
	//TODO add group chat functionality in later release
	
	public static final String getHomeDir()
	{
		return HOME_DIR;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Main.log.info("[CORE] Initializing " + CHAT_VERSION);
		if (args.length >= 1)
		{
			if (args[1].equalsIgnoreCase("debug"))
			{
				debug = true;
				Main.log.dbg("[CORE] Debug mode enabled.");
			}
		}
		Main.log.info("[CORE] Operating system: " + System.getProperty("os.name"));
		Server.getLocalIPAddr();
		Main.log.info("[CORE] Setting main dialog font to: " + DIALOG_FONT.getFontName() + " " + DIALOG_FONT.getSize() + "px");
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run()
			{
				//Run this code on shutdown
				Main.log.info("[SHUTDOWN] Shutting down " + CHAT_VERSION);
				SERVER.CloseServer();
				Main.log.info("[SHUTDOWN] Bye!");
			}
		});
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmChatV.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Main.log.info_nln("[CORE] Checking for contact database... ");
		File basedir = new File(CONTACT_DBDIR);
		if (basedir.exists())
		{
			Main.log.info_ln("found!");
		}
		else
		{
			Main.log.info_ln("not found! Creating.");
			boolean success = basedir.mkdir();
			if (success)
				Main.log.info("[CORE] Successfully created contact database!");
			else
			{
				Main.log.info("[CORE] Unable to create contact database. Please contact developers if this persists.");
				JOptionPane.showMessageDialog(null, "Error creating contact database.\nContact developers if this error persists.\n\nError Code 1", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		Main.log.info_nln("[CORE] Checking for key database... ");
		File keydir = new File(KEY_DBDIR);
		if (keydir.exists())
		{
			Main.log.info_ln("found!");
		}
		else
		{
			Main.log.info_ln("not found! Creating.");
			boolean success = keydir.mkdir();
			if (success)
				Main.log.info("[CORE] Successfully created key database!");
			else
			{
				Main.log.info("[CORE] Unable to create key database. Please contact developers if this persists.");
				JOptionPane.showMessageDialog(null, "Error creating key database.\nContact developers if this error persists.\n\nError Code 1", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		//KSClient.chkKSFile();
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmChatV = new JFrame();
		frmChatV.setTitle(CHAT_VERSION);
		frmChatV.setBounds(100, 100, 800, 600);
		frmChatV.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmChatV.getContentPane().setLayout(null);
		
		JLabel lblTitle = new JLabel(CHAT_VERSION);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Arial", Font.PLAIN, 31));
		lblTitle.setBounds(6, 6, 788, 37);
		frmChatV.getContentPane().add(lblTitle);
		
		JLabel lblInfo = new JLabel("<html><body><p>This is an application that allows you to chat directly with other people using the same software. All you need is their IP address and UPnP support on your router. This is encrypted P2P chat, only you and the recipient see the exchanged messages.</p></body></html>");
		lblInfo.setVerticalAlignment(SwingConstants.TOP);
		lblInfo.setBounds(6, 55, 788, 54);
		frmChatV.getContentPane().add(lblInfo);
		
		JButton btnGetLocalIp = new JButton("Get Local IP");
		btnGetLocalIp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IP.getLocalIP();
				} catch (UnknownHostException e1) {
					JOptionPane.showMessageDialog(null, "Unknown Host Error", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnGetLocalIp.setBounds(167, 121, 124, 29);
		frmChatV.getContentPane().add(btnGetLocalIp);
		
		JButton btnGetExternalIp = new JButton("Get External IP");
		btnGetExternalIp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IP.getExternalIP();
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "Connection Error\nAre you connected to the Internet?", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnGetExternalIp.setBounds(303, 121, 156, 29);
		frmChatV.getContentPane().add(btnGetExternalIp);
		
		chkEnableServer = new JCheckBox("Enable Server (Receive Messages)");
		chkEnableServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chkEnableServer.isSelected())
				{
					SERVER.start();
				}
				else
				{
					SERVER.CloseServer();
				}
			}
		});
		chkEnableServer.setBounds(7, 191, 267, 23);
		frmChatV.getContentPane().add(chkEnableServer);
		
		JButton btnManageContacts = new JButton("Manage Contacts");
		btnManageContacts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Contact.launch_window();
			}
		});
		btnManageContacts.setBounds(6, 222, 180, 25);
		frmChatV.getContentPane().add(btnManageContacts);
		
		JButton btnCheckForUpdates = new JButton("Check for Updates");
		btnCheckForUpdates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ChatUpdate.checkUpdates();
				} catch (IOException e1) {
					log.err("[UPDATE] Error while checking for updates. Error is printed below.");
					e1.printStackTrace();
				}
			}
		});
		btnCheckForUpdates.setBounds(6, 259, 180, 25);
		frmChatV.getContentPane().add(btnCheckForUpdates);
		
		JButton btnSendMsg = new JButton("Send Message");
		btnSendMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JTextField IPField = new JTextField(1);
				InetAddress selip = Contact.getContactIP();
				if (selip != null)
					IPField.setText(selip.getHostAddress());
				JTextField TimeoutField = new JTextField(1);
				JButton btnSC = new JButton("Select Contact");
				JButton btnCC = new JButton("Clear Selection");
				btnSC.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						JFileChooser fc = new JFileChooser();
						fc.setCurrentDirectory(new File(Main.CONTACT_DBDIR));
						fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
						fc.setDialogTitle("Select Contact");
						fc.setFileView(new FileView() {
						    @Override
						    public Boolean isTraversable(File f) {
						    	File DBDIRFILE = new File(Main.CONTACT_DBDIR);
						        return DBDIRFILE.equals(f);
						    }
						});
						try
						{
							fc.showDialog(null, "Select Contact");
						}
						catch (Exception e1)
						{
							Main.log.err("Operation aborted.");
						}
						File selected = fc.getSelectedFile();
						String name = selected.getName();
						String ip = Contact.getIPByFile(selected);
						IPField.setText(ip);
						Contact.CONTACT_NAME = name;
						Contact.CONTACT_IP = ip;
					}
				});
				btnCC.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e)
					{
						IPField.setText("");
						Contact.CONTACT_NAME = "None";
						Contact.CONTACT_IP = "";
					}
				});
				Object[] sendDialogMsg = new Object[]
						{
						"Address to send this message to\n", IPField,
						"\n\nConnection Timeout (Seconds to try to connect before giving up, leave blank for default " + DEFAULT_TIMEOUT_SEC + " seconds)\n", TimeoutField,
						btnSC, btnCC
						};
				String[] sendOpt = new String[]
						{
						"Open Messaging Dialog", "Cancel"
						};
				String sendTitle = "Send Message";
				int option = JOptionPane.showOptionDialog(null, sendDialogMsg, sendTitle, 0, JOptionPane.QUESTION_MESSAGE, null, sendOpt, sendOpt[0]);
				if (option != 0)
					return;
				if (!MsgDialog.isOpen())
					MsgDialog.launch_window();
				try {
					String tms = TimeoutField.getText();
					if (tms.equals("") || tms == null)
						tms = "" + DEFAULT_TIMEOUT_SEC;
					int tm = Integer.parseInt(tms);
					MsgDialog.addMessagingTab(InetAddress.getByName(IPField.getText()), tm * 1000);
				} catch (NumberFormatException | UnknownHostException e1) {
					JOptionPane.showMessageDialog(null, "Bad IP address or timeout value!", "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				
			}
		});
		btnSendMsg.setBounds(6, 123, 149, 25);
		frmChatV.getContentPane().add(btnSendMsg);
		
		JButton btnManageBlockList = new JButton("Manage Block List");
		btnManageBlockList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BlockList.launch_window();
			}
		});
		btnManageBlockList.setBounds(198, 222, 162, 25);
		frmChatV.getContentPane().add(btnManageBlockList);
		
	}
}
