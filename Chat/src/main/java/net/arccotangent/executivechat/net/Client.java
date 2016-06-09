package net.arccotangent.executivechat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;

import net.arccotangent.executivechat.Main;
import net.arccotangent.executivechat.crypto.KeyManager;
import net.arccotangent.executivechat.crypto.MsgCrypto;
import net.arccotangent.executivechat.dialog.MsgDialog;

public class Client {
	
	private static Socket socket;

	/*
	private static int countlines(String str)
	{
		String[] lines = str.split("\r\n|\r|\n");
		return  lines.length;
	}
	*/
	
	/*
	public static void SendMessage() throws UnknownHostException, ConnectException, NullPointerException
	{
		final JTextField IPField = new JTextField(1);
		InetAddress selip = Contact.getContactIP();
		if (selip != null)
			IPField.setText(selip.getHostAddress());
		JTextArea MsgArea = new JTextArea(15, 60);
		MsgArea.setFont(Main.DIALOG_FONT);
		JScrollPane MsgField = new JScrollPane(MsgArea);
		JTextField SigField = new JTextField(1);
		SigField.setText(Main.SIGNATURE);
		JTextField TimeoutField = new JTextField(1);
		JButton btnSC = new JButton("Select Contact");
		JButton btnCC = new JButton("Clear Selection");
		JCheckBox saveSig = new JCheckBox("Save Signature");
		saveSig.setSelected(ssig);
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
				"\n\nMessage\n", MsgField,
				"\n\nSignature (Leave blank for no signature)\n", SigField, saveSig,
				"\n\nConnection Timeout (Seconds to try to connect before giving up, leave blank for default 2 seconds)\n", TimeoutField,
				btnSC, btnCC
				};
		String[] sendOpt = new String[]
				{
				"Send Message", "Cancel"
				};
		String sendTitle = "Send Message";
		int option = JOptionPane.showOptionDialog(null, sendDialogMsg, sendTitle, 0, JOptionPane.QUESTION_MESSAGE, null, sendOpt, sendOpt[0]);
		String ip = IPField.getText();
		String sig = SigField.getText();
		String tm = TimeoutField.getText();
		if (sig.equals(null) || sig.equals(""))
		{
			sig = "";
		}
		if (tm.equals(null) || tm.equals(""))
		{
			tm = "3";
		}
		if (saveSig.isSelected())
		{
			Main.SIGNATURE = sig;
			Main.log.info("[CLIENT] Saved signature for next message.");
			ssig = true;
		}
		else
		{
			ssig = false;
		}
		int tm2 = Integer.parseInt(tm);
		String msg = MsgArea.getText() + "\nSignature: " + sig;
		if (option == 0)
		{
			try
	        {
	            String host = ip;
	            int port = Server.SERVER_PORT;
	            InetAddress address = InetAddress.getByName(host);
	            int timeout = tm2 * 1000; //sec -> msec
	            Main.log.info_nln("[CLIENT] Attempting to connect to server at " + host + " using timeout of " + timeout + " ms... ");
	            socket = new Socket();
	            try
	            {
	            	socket.connect(new InetSocketAddress(address, port), timeout);
	            }
	            catch (SocketTimeoutException e1)
	            {
	            	JOptionPane.showMessageDialog(null, "Error: Unable to connect to server. A response was not received from the address you specified (" + host + ")\nwithin the set time (" + tm + " seconds)", "Connection Timed Out", JOptionPane.ERROR_MESSAGE);
	            	Main.log.info_ln("connection timed out.");
	            	return;
	            }
	            catch (ConnectException e2)
	            {
	            	JOptionPane.showMessageDialog(null, "Error: Unable to connect to server. The connection was refused.", "Connection Refused", JOptionPane.ERROR_MESSAGE);
	            	Main.log.info_ln("connection refused.");
	            	return;
	            }
	            Main.log.info_ln("connected! Sending message");
	            //Send the message to the server
	            OutputStream os = socket.getOutputStream();
	            OutputStreamWriter osw = new OutputStreamWriter(os);
	            BufferedWriter bw = new BufferedWriter(osw);
	            int numlines = countlines(msg);
	 
	            String number = numlines + "\n" + msg;
	 
	            String sendMessage = number + "\n";
	            bw.write(sendMessage);
	            bw.flush();
	            Main.log.dbg("[CLIENT] RAW message sent to the server at " + host + " : " + sendMessage);
	 
	            //Get the return message from the server
	            InputStream is = socket.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String message = br.readLine();
	            Main.log.dbg("[CLIENT] Message received from the server at " + host + " : " + message);
	            if (message.equals("152 Pong"))
	            {
	            	JOptionPane.showMessageDialog(null, "Received ping response!", "Received Response", JOptionPane.INFORMATION_MESSAGE);
	            }
	            else if (message.equals("160 Message received confirmation"))
	            {
	            	JOptionPane.showMessageDialog(null, "Server at " + host + " has received your message!", "Received Message", JOptionPane.INFORMATION_MESSAGE);
	            	Main.log.info("[CLIENT] Received acknowledgement message from target server!");
	            }
	        }
	        catch (Exception exception) 
	        {
	            exception.printStackTrace();
	        }
	        finally
	        {
	            //Closing the socket
	            try
	            {
	            	Main.log.info_nln("[CLIENT] Closing connection... ");
	                socket.close();
	            }
	            catch(Exception e)
	            {
	                e.printStackTrace();
	                Main.log.info_ln("error.");
	                return;
	            }
	            Main.log.info_ln("success!");
	        }
		}
		else
		{
			throw new NullPointerException();
		}
	}
	
	public static void SendReplyMessage(final InetAddress replyIP) throws UnknownHostException, ConnectException, NullPointerException
	{
		JTextField IPField = new JTextField(1);
		IPField.setText(replyIP.getHostAddress());
		IPField.setEditable(false);
		JTextArea MsgArea = new JTextArea(15, 60);
		MsgArea.setFont(Main.DIALOG_FONT);
		JScrollPane MsgField = new JScrollPane(MsgArea);
		JTextField SigField = new JTextField(1);
		SigField.setText(Main.SIGNATURE);
		JTextField TimeoutField = new JTextField(1);
		Object[] sendDialogMsg = new Object[]
				{
				"Address to send this message to\n", IPField,
				"\n\nMessage\n", MsgField,
				"\n\nSignature (Leave blank for no signature)\n", SigField,
				"\n\nConnection Timeout (Seconds to try to connect before giving up, leave blank for default 2 seconds)\n", TimeoutField
				};
		String[] sendOpt = new String[]
				{
				"Send Message", "Cancel"
				};
		String sendTitle = "Send Message";
		int option = JOptionPane.showOptionDialog(null, sendDialogMsg, sendTitle, 0, JOptionPane.QUESTION_MESSAGE, null, sendOpt, sendOpt[0]);
		String ip = IPField.getText();
		//String LOCAL_IP = Inet4Address.getLocalHost().getHostAddress();
		String sig = SigField.getText();
		String tm = TimeoutField.getText();
		if (sig.equals(null) || sig.equals(""))
		{
			sig = "";
		}
		if (tm.equals(null) || tm.equals(""))
		{
			tm = "3";
		}
		int tm2 = Integer.parseInt(tm);
		String msg = MsgArea.getText() + "\nSignature: " + sig;
		if (option == 0)
		{
			try
	        {
	            String host = ip;
	            int port = Server.SERVER_PORT;
	            InetAddress address = InetAddress.getByName(host);
	            int timeout = tm2 * 1000; //sec -> msec
	            Main.log.info_nln("Attempting to connect to server at " + host + " using timeout of " + timeout + " ms... ");
	            socket = new Socket();
	            try
	            {
	            	socket.connect(new InetSocketAddress(address, port), timeout);
	            }
	            catch (SocketTimeoutException e1)
	            {
	            	JOptionPane.showMessageDialog(null, "Error: Unable to connect to server. A response was not received from the address you specified (" + host + ")\nwithin the set time (" + tm + " seconds)", "Connection Timed Out", JOptionPane.ERROR_MESSAGE);
	            	Main.log.info_ln("connection timed out.");
	            	return;
	            }
	            catch (ConnectException e2)
	            {
	            	JOptionPane.showMessageDialog(null, "Error: Unable to connect to server. The connection was refused.", "Connection Refused", JOptionPane.ERROR_MESSAGE);
	            	Main.log.info_ln("connection refused.");
	            	return;
	            }
	            Main.log.info_ln("connected! Sending message");
	            //Send the message to the server
	            OutputStream os = socket.getOutputStream();
	            OutputStreamWriter osw = new OutputStreamWriter(os);
	            BufferedWriter bw = new BufferedWriter(osw);
	            int numlines = countlines(msg);
	 
	            String number = numlines + "\n" + msg;
	 
	            String sendMessage = number + "\n";
	            bw.write(sendMessage);
	            bw.flush();
	            Main.log.info("[CLIENT] RAW message sent to the server at " + host + " : " + sendMessage);
	 
	            //Get the return message from the server
	            InputStream is = socket.getInputStream();
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String message = br.readLine();
	            Main.log.info("[CLIENT] Message received from the server at " + host + " : " + message);
	            if (message.equals("152 Pong"))
	            {
	            	JOptionPane.showMessageDialog(null, "Received ping response!", "Received Response", JOptionPane.INFORMATION_MESSAGE);
	            }
	            else if (message.equals("160 Message received confirmation"))
	            {
	            	JOptionPane.showMessageDialog(null, "Server at " + host + " has received your message!", "Received Message", JOptionPane.INFORMATION_MESSAGE);
	            	Main.log.info("[CLIENT] Received acknowledgement message from target server.");
	            }
	        }
	        catch (Exception exception) 
	        {
	            exception.printStackTrace();
	        }
	        finally
	        {
	            //Closing the socket
	            try
	            {
	            	Main.log.info_nln("Closing connection... ");
	                socket.close();
	            }
	            catch(Exception e)
	            {
	                e.printStackTrace();
	                Main.log.info_ln("error.");
	                return;
	            }
	            Main.log.info_ln("success!");
	        }
		}
		else
		{
			throw new NullPointerException();
		}
	}
	*/
	
	public static void sendMessageNoInteraction(String host, int timeout, String msg)
	{
		try
        {
            int port = Server.SERVER_PORT;
            InetAddress address = InetAddress.getByName(host);
            PublicKey key = KeyManager.loadKeyByIP(address);
            if (key == null)
            {
            	Main.log.err("[CLIENT] Error retrieving key.");
            }
            Main.log.info_nln("[CLIENT] Attempting to connect to server at " + host + " using timeout of " + timeout + " ms... ");
            socket = new Socket();
            try
            {
            	socket.connect(new InetSocketAddress(address, port), timeout);
            }
            catch (SocketTimeoutException e1)
            {
            	MsgDialog.addOwnMessageCTO(host, msg);
            	Main.log.info_ln("connection timed out.");
            	return;
            }
            catch (ConnectException e2)
            {
            	MsgDialog.addOwnMessageCR(host, msg);
            	Main.log.info_ln("connection refused.");
            	return;
            }
            Main.log.info_ln("connected! Sending message");
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            //int numlines = countlines(msg);
 
            String number = msg;
 
            String sendMessage = number + "\n";
            String cryptedmsg = MsgCrypto.encryptMsg(sendMessage, key);
            PrivateKey privkey = KeyManager.getOwnPrivateKey();
            String aeskey = MsgCrypto.getAESKey(cryptedmsg);
            String sig = MsgCrypto.signText(aeskey, privkey);
            String packet = cryptedmsg + "\n" + sig; //maybe add compression to save bandwidth
            bw.write(packet);
            bw.newLine();
            bw.flush();
            Main.log.info("[CLIENT] RAW message sent to the server at " + host + " : " + packet);
 
            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String retmessage = br.readLine();
            Main.log.info("[CLIENT] Message received from the server at " + host + " : " + retmessage);
            if (retmessage.equals("152 Pong"))
            {
            	JOptionPane.showMessageDialog(null, "Received ping response!", "Received Response", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (retmessage.equals("160 Message received confirmation"))
            {
            	//JOptionPane.showMessageDialog(null, "Server at " + host + " has received your message!", "Received Message", JOptionPane.INFORMATION_MESSAGE);
            	Main.log.info("[CLIENT] Received acknowledgement message from target server!");
            }
            else if (retmessage.equals("161 Blocked"))
            {
            	Main.log.err("[CLIENT] The server at this address has blocked us.");
            	JOptionPane.showMessageDialog(null, "The server at this address has blocked us.", "Blocked By Server", JOptionPane.ERROR_MESSAGE);
            }
            else if (retmessage.equals("202 Send client pubkey"))
            {
            	Main.log.info("[CLIENT] Server requested our public key, sending.");
            	PublicKey pkey = KeyManager.getOwnPublicKey();
            	byte[] pkeyb = pkey.getEncoded();
            	String pkeyb64 = Base64.encodeBase64String(pkeyb);
            	bw.write(pkeyb64);
            	bw.newLine();
            	bw.flush();
            	Main.log.info("[CLIENT] Sent public key to server, awaiting acknowledgement message.");
            	String rm2 = br.readLine();
            	if (rm2.equals("160 Message received confirmation"))
            	{
            		Main.log.info("[CLIENT] Received acknowledgement message from target server!");
            	}
            }
            MsgDialog.addOwnMessage(host, msg);
        }
        catch (Exception exception) 
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
            	Main.log.info_nln("[CLIENT] Closing connection... ");
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Main.log.info_ln("error.");
                return;
            }
            Main.log.info_ln("success!");
        }
	}

}
