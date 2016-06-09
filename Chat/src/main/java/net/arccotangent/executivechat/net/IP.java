package net.arccotangent.executivechat.net;

import java.awt.FlowLayout;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.arccotangent.executivechat.Main;

public class IP {
	
	public static final String EXTERNAL_IP_SERVER = "http://checkip.amazonaws.com";
	
	public static void getLocalIP() throws UnknownHostException
	{
		String LOCAL_IP = Main.LOCAL_IP_ADDRESS;
		final JFrame reportDisplayFrame = new JFrame("IP Addresses for Connections");
		  reportDisplayFrame.setVisible(true);
		  reportDisplayFrame.setSize(800, 600);
		  reportDisplayFrame.getContentPane().setLayout(new FlowLayout());
		  JTextArea reportDisplayText = new JTextArea(25, 70);
		  reportDisplayText.setEditable(false);
		  reportDisplayText.setLineWrap(true);
		  reportDisplayText.setWrapStyleWord(true);
		  reportDisplayText.setFont(Main.DIALOG_FONT);
		  int horizontalPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
		  int verticalPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
		  String reportText = "Your local address for receiving connections is: " + LOCAL_IP + "\n\nThis address can only be used by people on your Wi-Fi network.\nIf it is incorrect, contact developers.";
		  reportDisplayText.setText(reportText);
		  JScrollPane finalNumberScroll = new JScrollPane(reportDisplayText, verticalPolicy, horizontalPolicy);
		  reportDisplayFrame.getContentPane().add(finalNumberScroll);
	}
	
	public static void getExternalIP() throws IOException
	{
		URL SERVER_URL = new URL(EXTERNAL_IP_SERVER);
		@SuppressWarnings("resource")
		String ip = new Scanner(SERVER_URL.openStream(), "UTF-8").useDelimiter("\\A").next();
		final JFrame reportDisplayFrame = new JFrame("IP Addresses for Connections");
		  reportDisplayFrame.setVisible(true);
		  reportDisplayFrame.setSize(800, 600);
		  reportDisplayFrame.getContentPane().setLayout(new FlowLayout());
		  JTextArea reportDisplayText = new JTextArea(25, 70);
		  reportDisplayText.setEditable(false);
		  reportDisplayText.setLineWrap(true);
		  reportDisplayText.setWrapStyleWord(true);
		  reportDisplayText.setFont(Main.DIALOG_FONT);
		  int horizontalPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
		  int verticalPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
		  String reportText = "Your external address for receiving connections is: " + ip + "\n\nThis address can only be used by people outside your Wi-Fi network.";
		  reportDisplayText.setText(reportText);
		  JScrollPane finalNumberScroll = new JScrollPane(reportDisplayText, verticalPolicy, horizontalPolicy);
		  reportDisplayFrame.getContentPane().add(finalNumberScroll);
	}

}
