package net.arccotangent.executivechat;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BlockList extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private static JTextArea taBlockList;
	
	public static String getBlockList()
	{
		Main.log.info("[BLOCK LIST] Loading block list from file.");
		File f = new File(Main.BLOCKED_IP_FILE);
		
		Main.log.info_nln("[BLOCK LIST] Checking for block list file... ");
		if (!f.exists())
		{
			Main.log.info_ln("not found! Attempting to create.");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				Main.log.err("[BLOCK LIST] Failed to create block list file.");
				return null;
			}
			Main.log.info("[BLOCK LIST] Successfully created block list file.");
		}
		else
		{
			Main.log.info_ln("found!");
		}
		
		byte[] blocklistbytes = null;
		try {
			blocklistbytes = Files.readAllBytes(f.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			Main.log.err("[BLOCK LIST] Error reading from block list file.");
			return null;
		}
		Main.log.dbg("[BLOCK LIST] Read block list from file successfully.");
		String blocklist = new String(blocklistbytes);
		return blocklist;
	}
	
	private static void loadBlockList()
	{
		Main.log.info("[BLOCK LIST] Loading block list from file.");
		File f = new File(Main.BLOCKED_IP_FILE);
		
		Main.log.info_nln("[BLOCK LIST] Checking for block list file... ");
		if (!f.exists())
		{
			Main.log.info_ln("not found! Attempting to create.");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				Main.log.err("[BLOCK LIST] Failed to create block list file.");
				return;
			}
			Main.log.info("[BLOCK LIST] Successfully created block list file.");
		}
		else
		{
			Main.log.info_ln("found!");
		}
		
		byte[] blocklistbytes = null;
		try {
			blocklistbytes = Files.readAllBytes(f.toPath());
		} catch (IOException e) {
			e.printStackTrace();
			Main.log.err("[BLOCK LIST] Error reading from block list file.");
			return;
		}
		Main.log.dbg("[BLOCK LIST] Read block list from file successfully.");
		String blocklist = new String(blocklistbytes);
		taBlockList.setText(blocklist);
	}
	
	private static void saveBlockList()
	{
		Main.log.info("[BLOCK LIST] Saving block list to file.");
		File f = new File(Main.BLOCKED_IP_FILE);
		
		Main.log.info_nln("[BLOCK LIST] Checking for block list file... ");
		if (!f.exists())
		{
			Main.log.info_ln("not found! Attempting to create.");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				Main.log.err("[BLOCK LIST] Failed to create block list file.");
				return;
			}
			Main.log.info("[BLOCK LIST] Successfully created block list file.");
		}
		else
		{
			Main.log.info_ln("found!");
		}
		
		String blocklist = taBlockList.getText();
		try {
			FileWriter w = new FileWriter(f);
			w.write(blocklist);
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
			Main.log.err("[BLOCK LIST] Error saving block list to file.");
			return;
		}
		Main.log.info("[BLOCK LIST] Saved block list to file successfully.");
	}
	
	public static void launch_window() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BlockList frame = new BlockList();
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
	public BlockList() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblTitle = new JLabel("Blocked IP Address List");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Arial", Font.PLAIN, 31));
		lblTitle.setBounds(12, 12, 774, 37);
		contentPane.add(lblTitle);
		
		JLabel lblDescription = new JLabel("<html><body><p>Manage blocked IP addresses here. Any IP address on this block list will not be able to send you messages.<br>\nEnter 1 IP address per line, and save the block list to effectively block.</p></body></html>");
		lblDescription.setVerticalAlignment(SwingConstants.TOP);
		lblDescription.setBounds(12, 61, 774, 30);
		contentPane.add(lblDescription);
		
		JScrollPane spBlockList = new JScrollPane();
		spBlockList.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		spBlockList.setBounds(12, 103, 774, 381);
		contentPane.add(spBlockList);
		
		taBlockList = new JTextArea();
		taBlockList.setLineWrap(true);
		spBlockList.setViewportView(taBlockList);
		loadBlockList();
		
		JButton btnSaveList = new JButton("Save Block List");
		btnSaveList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveBlockList();
			}
		});
		btnSaveList.setBounds(12, 496, 774, 25);
		contentPane.add(btnSaveList);
		
		JButton btnLoadList = new JButton("Refresh/Load Block List");
		btnLoadList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadBlockList();
			}
		});
		btnLoadList.setBounds(12, 533, 774, 25);
		contentPane.add(btnLoadList);
	}
}
