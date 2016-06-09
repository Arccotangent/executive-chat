package net.arccotangent.executivechat.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.arccotangent.executivechat.Main;
import net.arccotangent.executivechat.crypto.KeyManager;
import net.arccotangent.executivechat.net.Client;

public class MsgTabBase extends JPanel {

	private static final long serialVersionUID = 1L;
	public InetAddress COMM_IP = null;
	private int timeout = Main.DEFAULT_TIMEOUT_SEC * 1000;
	private JTextArea msgDisplayArea;
	
	public void addReceivedMessage(String msg)
	{
		String txt = msgDisplayArea.getText();
		msgDisplayArea.setText(txt + "\n" + COMM_IP.getHostAddress() + ": " + msg + "");
	}
	
	public void addReceivedMessageUnverified(String msg)
	{
		String txt = msgDisplayArea.getText();
		msgDisplayArea.setText(txt + "\n" + COMM_IP.getHostAddress() + " (NOT VERIFIED): " + msg + "");
	}
	
	public void addOwnMessage(String msg)
	{
		String txt = msgDisplayArea.getText();
		msgDisplayArea.setText(txt + "\nYou: " + msg);
	}
	
	public void addOwnMessageCR(String msg)
	{
		String txt = msgDisplayArea.getText();
		msgDisplayArea.setText(txt + "\nYou (Connection refused, message not delivered): " + msg);
	}
	
	public void addOwnMessageCTO(String msg)
	{
		String txt = msgDisplayArea.getText();
		msgDisplayArea.setText(txt + "\nYou (Connection timed out, message not delivered): " + msg);
	}

	/**
	 * Create the panel.
	 */
	public MsgTabBase(final InetAddress ip, final int tm) {
		setLayout(null);
		COMM_IP = ip;
		timeout = tm;
		
		JScrollPane msgDisplayScroll = new JScrollPane();
		msgDisplayScroll.setBounds(12, 25, 776, 306);
		add(msgDisplayScroll);
		
		msgDisplayArea = new JTextArea();
		msgDisplayScroll.setViewportView(msgDisplayArea);
		msgDisplayArea.setEditable(false);
		msgDisplayArea.setText("");
		
		JScrollPane msgScroll = new JScrollPane();
		msgScroll.setBounds(12, 354, 776, 198);
		add(msgScroll);
		
		JTextArea msgArea = new JTextArea();
		msgArea.setEditable(true);
		msgScroll.setViewportView(msgArea);
		
		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				KeyManager.loadKeyByIP(COMM_IP); //Guarantee the target pubkey is in the local database
				Client.sendMessageNoInteraction(COMM_IP.getHostAddress(), timeout, msgArea.getText());
				msgArea.setText("");
			}
		});
		btnSend.setBounds(12, 564, 776, 25);
		add(btnSend);
		
		JLabel lblTypeMessageTo = new JLabel("Type message to send in below box, conversation is in above box.");
		lblTypeMessageTo.setBounds(12, 331, 776, 25);
		add(lblTypeMessageTo);
		this.setVisible(true);

	}
}
