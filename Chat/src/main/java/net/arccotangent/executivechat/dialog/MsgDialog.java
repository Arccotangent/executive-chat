package net.arccotangent.executivechat.dialog;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import net.arccotangent.executivechat.Main;

public class MsgDialog {
	
	private static JTabbedPane window;
	private static ArrayList<String> addresses = new ArrayList<String>();
	private static JFrame mainframe;
	private static boolean open = false;
	
	public static boolean isOpen()
	{
		return open;
	}
	
	public static void addReceivedMessage(String srcip, String msg)
	{
		int index = getaddrindex(srcip);
		if (index != -1) 
		{
			MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
			tab.addReceivedMessage(msg);
		}
		else
		{
			try {
				addMessagingTab(InetAddress.getByName(srcip), Main.DEFAULT_TIMEOUT_SEC);
				index = getaddrindex(srcip);
				MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
				tab.addReceivedMessage(msg);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Main.log.err("[SERVER] Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.");
				JOptionPane.showMessageDialog(null, "Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void addReceivedMessageUnverified(String srcip, String msg)
	{
		int index = getaddrindex(srcip);
		if (index != -1) 
		{
			MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
			tab.addReceivedMessageUnverified(msg);
		}
		else
		{
			try {
				addMessagingTab(InetAddress.getByName(srcip), Main.DEFAULT_TIMEOUT_SEC);
				index = getaddrindex(srcip);
				MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
				tab.addReceivedMessageUnverified(msg);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Main.log.err("[SERVER] Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.");
				JOptionPane.showMessageDialog(null, "Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void addOwnMessage(String destip, String msg)
	{
		int index = getaddrindex(destip);
		if (index != -1) 
		{
			MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
			tab.addOwnMessage(msg);
		}
		else
		{
			try {
				addMessagingTab(InetAddress.getByName(destip), Main.DEFAULT_TIMEOUT_SEC);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Main.log.err("[SERVER] Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.");
				JOptionPane.showMessageDialog(null, "Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void addOwnMessageCR(String destip, String msg)
	{
		int index = getaddrindex(destip);
		if (index != -1) 
		{
			MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
			tab.addOwnMessageCR(msg);
		}
		else
		{
			try {
				addMessagingTab(InetAddress.getByName(destip), Main.DEFAULT_TIMEOUT_SEC);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Main.log.err("[SERVER] Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.");
				JOptionPane.showMessageDialog(null, "Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static void addOwnMessageCTO(String destip, String msg)
	{
		int index = getaddrindex(destip);
		if (index != -1) 
		{
			MsgTabBase tab = (MsgTabBase) window.getComponentAt(index);
			tab.addOwnMessageCTO(msg);
		}
		else
		{
			try {
				addMessagingTab(InetAddress.getByName(destip), Main.DEFAULT_TIMEOUT_SEC);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Main.log.err("[SERVER] Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.");
				JOptionPane.showMessageDialog(null, "Someone tried to send us a message, but we had an error deciphering the IP address. Please report this error to the developers.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public static int getaddrindex(String ip)
	{
		int index = -1;
		for (int i = 0; i < addresses.size(); i++)
		{
			if (ip.equals(addresses.get(i)))
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	public static void addMessagingTab(InetAddress ip, int tm)
	{
		if (getaddrindex(ip.getHostAddress()) == -1)
		{
			if (!open)
				launch_window();
			window.addTab(ip.getHostAddress(), new MsgTabBase(ip, tm));
			Main.log.dbg("[MSGDIALOG] Tabcount: " + window.getTabCount());
			addresses.add(ip.getHostAddress());
			Main.log.info("[MSGDIALOG] Added new messaging tab for IP address " + ip.getHostAddress());
		}
	}
	
	public static void launch_window()
	{
		if (!open)
			window = new JTabbedPane();
		mainframe = new JFrame();
		mainframe.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		mainframe.setTitle(Main.CHAT_VERSION + " Messaging");
		mainframe.setBounds(50, 50, 800, 650);
		mainframe.getContentPane().add(window);
		mainframe.setVisible(true);
		window.setVisible(true);
		open = true;
		Main.log.info("[MSGDIALOG] Launched window.");
	}

}
