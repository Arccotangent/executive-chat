package net.arccotangent.executivechat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileView;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.awt.event.ActionEvent;
import net.arccotangent.executivechat.Main;

public class Contact extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public static String CONTACT_NAME = "None";
	public static String CONTACT_IP = "";
	private static String SELECTION_STRING = "<html><body><p><center>\n<u><strong>Selected Contact</strong></u>\n<br>Name: " + CONTACT_NAME + "\n<br>IP Address: " + CONTACT_IP + "\n</center></p></body></html>";
	private JLabel lblSelectedContact;
	
	/**
	 * Get the currently selected contact's IP address as an InetAddress object
	 * @return The selected contact's IP address
	 * @throws UnknownHostException If the selected contact's IP address is invalid
	 */
	public static InetAddress getContactIP()
	{
		if (CONTACT_IP.isEmpty())
			return null;
		InetAddress ip = null;
		try {
			ip = InetAddress.getByName(CONTACT_IP);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return ip;
	}
	
	/*
	private static String[] getContactList()
	{
		File basedir = new File(Main.CONTACT_DBDIR);
		File[] filelist = basedir.listFiles();
		ArrayList<String> namearraylist = new ArrayList<String>();
		int amt = filelist.length;
		for (int i = 0; i < amt; i++)
		{
			if (filelist[i].isFile())
			{
				String name = filelist[i].getName();
				int nend = name.lastIndexOf(".accontact");
				name.substring(0, nend);
				namearraylist.add(name);
			}
		}
		return null;
	}
	*/
	
	/*
	private static String getIPByName(String name)
	{
		File cfile = new File(Main.CONTACT_DBDIR + "/" + name + ".accontact");
		byte[] ipaddrbytes = null;
		try {
			ipaddrbytes = Files.readAllBytes(cfile.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading from contact file, the name is invalid.");
		}
		String ipaddr = new String(ipaddrbytes);
		return ipaddr;
	}
	*/
	
	/**
	 * Get the contact's IP address from a file
	 * @param name The file to get the IP from
	 * @return The IP address as a String
	 */
	public static String getIPByFile(File name)
	{
		if (!name.isFile())
			return null;
		byte[] ipaddrbytes = null;
		try {
			ipaddrbytes = Files.readAllBytes(name.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			Main.log.err("[GetIPByFile] Error reading from contact file.");
		}
		String ipaddr = new String(ipaddrbytes);
		return ipaddr;
	}
	
	private static int saveContact(String name, String ip)
	{
		Main.log.info("Saving/updating contact with name " + name + " and IP " + ip);
		File cfile = new File(Main.CONTACT_DBDIR + "/" + name);
		Main.log.info_nln("Checking if file exists... ");
		if (!cfile.exists())
		{
			Main.log.info_nln2("file does not exist.\nAttempting to create new contact file... ");
			try {
				cfile.createNewFile();
			} catch (IOException e) {
				Main.log.info_ln("error creating new contact file. Aborting operation.");
				e.printStackTrace();
				return 2;
			}
			Main.log.info_ln("success!");
		}
		else
		{
			Main.log.info_ln("file exists. Will not attempt to create.");
		}
		FileWriter fw = null;
		Main.log.info_nln("Writing contact data to file... ");
		try {
			fw = new FileWriter(cfile);
			fw.write(ip);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			Main.log.info_ln("error with file output.");
			e.printStackTrace();
			return 1;
		}
		Main.log.info_ln("success!");
		return 0;
	}

	/**
	 * Open the contact management dialog. This is the only way to do so.
	 */
	public static void launch_window() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Contact frame = new Contact();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	private Contact() {
		SELECTION_STRING = "<html><body><p><center>\n<u><strong>Selected Contact</strong></u>\n<br>Name: " + CONTACT_NAME + "\n<br>IP Address: " + CONTACT_IP + "\n</center></p></body></html>";
		setTitle("Contact Management");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnUpdateList = new JButton("Select Contact");
		btnUpdateList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//load ip from file, contact name is file name
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
				File selected = null;
				String name = "";
				String ip = "";
				try
				{
					fc.showDialog(null, "Select Contact");
					selected = fc.getSelectedFile();
					name = selected.getName();
					ip = getIPByFile(selected);
				}
				catch (Exception e1)
				{
					Main.log.err("Contact selection aborted.");
					return;
				}
				CONTACT_NAME = name;
				CONTACT_IP = ip;
				SELECTION_STRING = "<html><body><p><center>\n<u><strong>Selected Contact</strong></u>\n<br>Name: " + CONTACT_NAME + "\n<br>IP Address: " + CONTACT_IP + "\n</center></p></body></html>";
				lblSelectedContact.setText(SELECTION_STRING);
				Main.log.info("Selected contact " + CONTACT_NAME + " with IP address " + CONTACT_IP);
			}
		});
		btnUpdateList.setBounds(273, 98, 137, 25);
		contentPane.add(btnUpdateList);
		
		JButton btnNewContact = new JButton("Create New/Update Contact");
		btnNewContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextField nameField = new JTextField(1);
				JTextField ipField = new JTextField(1);
				Object[] ncmsg = {
						"Contact Name\n", nameField,
						"Contact IP Address\n", ipField
				};
				String[] ncopt = {
						"Save Contact", "Cancel"
				};
				int ncarg = JOptionPane.showOptionDialog(null, ncmsg, "Create New/Update Contact", 0, JOptionPane.QUESTION_MESSAGE, null, ncopt, ncopt[0]);
				if (ncarg == 0)
				{
					String name = nameField.getText();
					String ip = ipField.getText();
					int ecode = saveContact(name, ip);
					if (ecode == 0)
						Main.log.info("[CONTACTS] All is good.");
					else
					{
						Main.log.err("[CONTACTS] An error occurred while saving contact data.");
						Main.log.err("[CONTACTS] Exit code " + ecode);
					}
				}
			}
		});
		btnNewContact.setBounds(12, 98, 249, 25);
		contentPane.add(btnNewContact);
		
		JLabel lblChatContacts = new JLabel("Contact Management");
		lblChatContacts.setHorizontalAlignment(SwingConstants.CENTER);
		lblChatContacts.setFont(new Font("Dialog", Font.PLAIN, 31));
		lblChatContacts.setBounds(12, 12, 774, 31);
		contentPane.add(lblChatContacts);
		
		JLabel lblContactDescription = new JLabel("<html><body><p>Here you can manage your contacts and their IP addresses. Contacts are stored in a database on your own hard disk. To exit this window simply close it.</p></body></html>");
		lblContactDescription.setBounds(12, 55, 774, 31);
		contentPane.add(lblContactDescription);
		
		lblSelectedContact = new JLabel(SELECTION_STRING);
		lblSelectedContact.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedContact.setVerticalAlignment(SwingConstants.TOP);
		lblSelectedContact.setFont(new Font("Courier", Font.PLAIN, 23));
		lblSelectedContact.setBounds(12, 454, 774, 104);
		contentPane.add(lblSelectedContact);
		
		JButton btnDeleteContact = new JButton("Delete Contact");
		btnDeleteContact.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(Main.CONTACT_DBDIR));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDialogTitle("Delete Contact");
				fc.setFileView(new FileView() {
				    @Override
				    public Boolean isTraversable(File f) {
				    	File DBDIRFILE = new File(Main.CONTACT_DBDIR);
				        return DBDIRFILE.equals(f);
				    }
				});
				File selected = null;
				String selstr = "";
				String name = "";
				try
				{
					fc.showDialog(null, "Delete Contact");
					selected = fc.getSelectedFile();
					selstr = selected.getAbsolutePath();
					name = selected.getName();
				}
				catch (Exception e1)
				{
					Main.log.warn("Contact deletion aborted.");
					return;
				}
				if (selected.isFile() && !name.isEmpty())
				{
					int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this contact?\nName: " + name + "\n", "Confirm Contact Deletion", JOptionPane.YES_NO_OPTION);
					if (option == JOptionPane.YES_OPTION)
					{
						Main.log.info_nln("Attempting to delete contact file... ");
						boolean dsuccess = selected.delete();
						if (dsuccess)
							Main.log.info_ln("success!");
						else
							Main.log.info_ln("error.");
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Error: The selected file appears to be invalid.\n" + "Selected file: " + selstr + "\n\nError code 2", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnDeleteContact.setBounds(590, 98, 145, 25);
		contentPane.add(btnDeleteContact);
		
		JLabel lblContactInfo = new JLabel("<html><body><p>\nAfter you select a contact, that contact's IP address will be auto-filled in every message you send until you clear the selection. There are buttons in the message dialog that allow you to manage your selection.\n</p></body></html>");
		lblContactInfo.setVerticalAlignment(SwingConstants.TOP);
		lblContactInfo.setBounds(12, 411, 774, 31);
		contentPane.add(lblContactInfo);
		
		JButton btnClearSelection = new JButton("Clear Selection");
		btnClearSelection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CONTACT_NAME = "None";
				CONTACT_IP = "";
				SELECTION_STRING = "<html><body><p><center>\n<u><strong>Selected Contact</strong></u>\n<br>Name: " + CONTACT_NAME + "\n<br>IP Address: " + CONTACT_IP + "\n</center></p></body></html>";
				lblSelectedContact.setText(SELECTION_STRING);
				Main.log.info("Cleared contact selection.");
			}
		});
		btnClearSelection.setBounds(422, 98, 156, 25);
		contentPane.add(btnClearSelection);
	}
}
