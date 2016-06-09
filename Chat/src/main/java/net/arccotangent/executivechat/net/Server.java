package net.arccotangent.executivechat.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.support.igd.PortMappingListener;
import org.fourthline.cling.support.model.PortMapping;

import net.arccotangent.executivechat.BlockList;
import net.arccotangent.executivechat.Main;
import net.arccotangent.executivechat.crypto.KeyManager;
import net.arccotangent.executivechat.crypto.MsgCrypto;
import net.arccotangent.executivechat.dialog.MsgDialog;

public class Server extends Thread {
	
	private static Socket socket;
	private static ServerSocket serverSocket;
	private static UpnpService UPNP_SERVICE = null;
	public static final int SERVER_PORT = 21367;
	private static boolean upnp_open = false;
	
	/*
	private static int countlines(String str)
	{
		String[] lines = str.split("\r\n|\r|\n");
		return  lines.length;
	}
	*/
	
    public static RegistryListener REGISTRY_LISTENER = new RegistryListener() {

        public void remoteDeviceDiscoveryStarted(Registry registry,
                                                 RemoteDevice device) {
        	Main.log.info(
                    "[UPNP/REGISTRY] Discovery started: " + device.getDisplayString()
            );
        }

        public void remoteDeviceDiscoveryFailed(Registry registry,
                                                RemoteDevice device,
                                                Exception ex) {
        	Main.log.err(
                    "[UPNP/REGISTRY] Discovery failed: " + device.getDisplayString() + " => " + ex
            );
        }

        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
        	Main.log.info(
                    "[UPNP/REGISTRY] Remote device available: " + device.getDisplayString()
            );
        }

        public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        	Main.log.info(
                    "[UPNP/REGISTRY] Remote device updated: " + device.getDisplayString()
            );
        }

        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        	Main.log.warn(
                    "[UPNP/REGISTRY] Remote device removed: " + device.getDisplayString()
            );
        }

        public void localDeviceAdded(Registry registry, LocalDevice device) {
        	Main.log.info(
                    "[UPNP/REGISTRY] Local device added: " + device.getDisplayString()
            );
        }

        public void localDeviceRemoved(Registry registry, LocalDevice device) {
        	Main.log.warn(
                    "[UPNP/REGISTRY] Local device removed: " + device.getDisplayString()
            );
        }

        public void beforeShutdown(Registry registry) {
        	Main.log.info(
                    "[UPNP/REGISTRY] Before shutdown, the registry has devices: "
                    + registry.getDevices().size()
            );
        }

        public void afterShutdown() {
        	Main.log.info("[UPNP/REGISTRY] Shutdown of registry complete!");

        }
    };
	
	public static void getLocalIPAddr()
	{
		if (System.getProperty("os.name").contains("Linux"))
		{
			ArrayList<Inet4Address> i4 = new ArrayList<Inet4Address>();
			Enumeration<NetworkInterface> e = null;
			try {
				e = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e1) {
				e1.printStackTrace();
			}
			while(e.hasMoreElements())
			{
			    NetworkInterface n = (NetworkInterface) e.nextElement();
			    System.out.print(n.getName() + ": ");
			    Enumeration<InetAddress> ee = n.getInetAddresses();
			    while (ee.hasMoreElements())
			    {
			        InetAddress i = (InetAddress) ee.nextElement();
			        if (i instanceof Inet4Address)
			        {
			        	System.out.println(i.getHostAddress());
			        	i4.add((Inet4Address) i);
			        }
			    }
			}
			for (int i = 0; i < i4.size(); i++)
			{
				Inet4Address i4b = i4.get(i);
				if (!i4b.isLoopbackAddress() && !i4b.getHostAddress().startsWith("127."))
				{
					Main.log.info("[LINUX] Found seemingly valid IPv4 address in enumeration: " + i4b.getHostAddress());
					Main.LOCAL_IP_ADDRESS = i4b.getHostAddress();
					return;
				}
			}
			if (Main.LOCAL_IP_ADDRESS.isEmpty())
			{
				try {
					Main.LOCAL_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
			}
			Main.log.info("[LINUX] IP address detected as: " + Main.LOCAL_IP_ADDRESS);
		}
		else
		{
			try {
				Main.LOCAL_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			Main.log.info("[NON-LINUX] IP address detected as: " + Main.LOCAL_IP_ADDRESS);
		}
	}
	
	public void run()
	{
		Main.log.info("[SERVER] Attempting to start server.");
		try
        {
			if (!upnp_open)
				UPNPOpenPorts();
            serverSocket = new ServerSocket(SERVER_PORT);
            //Server is running always. This is done using this while(true) loop
            while(true) 
            {
                //Reading the message from the client
            	Main.log.info("[SERVER] Server started and listening on port " + SERVER_PORT + ", awaiting messages.");
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String l1 = br.readLine();
                
                final InetAddress srcip = socket.getInetAddress();
                String srcipaddr = srcip.getHostAddress();
                //message = "This message comes from IP address: " + srcipaddr + "\n\n" + message;
                if (isBlocked(srcipaddr))
                {
                	Main.log.info("[SERVER] Received message from blocked IP address " + srcipaddr + " , not displaying.");
                	String ret = "161 Blocked";
                	OutputStream os = socket.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(ret);
                    bw.flush();
                    bw.close();
                	serverSocket.close();
                	run();
                }
                else
                {
                	Main.log.dbg("[SERVER] Received message from nonblocked IP address " + srcipaddr + ":" + l1);
                    if (l1 == null || l1.isEmpty())
                    {
                    	Main.log.err("[SERVER] Received blank message from " + srcipaddr + ". Very strange...");
                    	serverSocket.close();
                    	run();
                    }
                    else if (l1.equals("151 ping"))
                    {
                    	Main.log.info("[SERVER] Received ping from " + srcipaddr);
                    	String returnMessage;
                        returnMessage = "152 Pong";
         
                        //Sending the response back to the client.
                        OutputStream os = socket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bw = new BufferedWriter(osw);
                        bw.write(returnMessage);
                        Main.log.info("[SERVER] Responded to client with pong message: " + returnMessage);
                        bw.flush();
                        bw.close();
                    	serverSocket.close();
                        //JOptionPane.showMessageDialog(null, "Received ping from " + srcipaddr, "Received Ping Message", JOptionPane.WARNING_MESSAGE);
                        run();
                    }
                    else if (l1.equals("201 Send public key"))
                    {
                    	Main.log.info("[SERVER] Client at " + srcipaddr + " has requested our RSA public key.");
                    	String returnMsg;
                    	PublicKey key = KeyManager.getOwnPublicKey();
                    	returnMsg = Base64.encodeBase64String(key.getEncoded());
                    	
                    	
                    	OutputStream os = socket.getOutputStream();
                    	OutputStreamWriter osw = new OutputStreamWriter(os);
                    	BufferedWriter bw = new BufferedWriter(osw);
                    	bw.write(returnMsg);
                    	Main.log.info("[SERVER] Sent our RSA public key to requesting client.");
                    	bw.flush();
                    	bw.close();
                    	serverSocket.close();
                    	run();
                    }
                    else
                    {
                    	Main.log.info("[SERVER] Received message from " + srcipaddr);
                    	//String l2 = new String(Base64.decodeBase64(l1));
                    	PrivateKey key = KeyManager.getOwnPrivateKey();
                    	String cryptedaes = l1; //RSA encrypted AES key
                    	SecretKey aes = MsgCrypto.decryptAESKey(Base64.decodeBase64(cryptedaes), key);
                    	String crypteddata = br.readLine(); //AES encrypted message
                    	String aeskeysig = br.readLine();
                    	String message = MsgCrypto.decryptMsg(crypteddata, key, aes);
                    	Main.log.dbg("[SERVER] Decoded and decrypted message.");
                    	/*
                        int numlines = Integer.parseInt(l1);
                        Main.log.dbg("[SERVER] Message header: " + numlines + " lines in message");
                        String message = "";
                        for (int i = 0; i < numlines; i++)
                        {
                        	message = message + br.readLine() + "\n";
                        }
                        */
                    	//Returning message to client to confirm message received
                        OutputStream os = socket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bw = new BufferedWriter(osw);
                    	if (!KeyManager.havePublicKey(srcip))
                    	{
                    		Main.log.warn("[SERVER] We don't have the source's public key. Requesting key to verify message.");
                    		bw.write("202 Send client pubkey");
                    		bw.newLine();
                    		bw.flush();
                    		Main.log.dbg("[SERVER] Sent: 202 Send client pubkey");
                    		String b64pkey = br.readLine();
                    		X509EncodedKeySpec pubspec = new X509EncodedKeySpec(Base64.decodeBase64(b64pkey));
                    		KeyFactory kf = KeyFactory.getInstance(Main.PUBKEY_ALGORITHM);
                    		PublicKey clikey = kf.generatePublic(pubspec);
                    		KeyManager.savePublicKeyByIP(srcip, clikey);
                    	}
                    	PublicKey clikey = KeyManager.loadKeyByIP(srcip);
                		Main.log.info("[SERVER] Verifying message authenticity.");
                    	boolean verified = MsgCrypto.verifyText(cryptedaes, aeskeysig, clikey);
                    	if (verified)
                			Main.log.info("[SERVER] Verified! This message appears to be authentic.");
                		else
                			Main.log.warn("[SERVER] ***MESSAGE NOT VERIFIED*** This may be someone trying to do bad things or the source's public key may have simply changed.");
                        String returnMessage;
                        returnMessage = "160 Message received confirmation";
                        //Sending the response back to the client.
                        bw.write(returnMessage);
                        Main.log.info("[SERVER] Responded to client with acknowledgement message: " + returnMessage);
                        Main.log.dbg("[SERVER] Sent: 160 Message received confirmation");
                        bw.flush();
                        bw.close();
                    	serverSocket.close();
                    	if (verified)
                    		MsgDialog.addReceivedMessage(srcipaddr, message);
                    	else
                    		MsgDialog.addReceivedMessageUnverified(srcipaddr, message);
                    	/*
                        final JFrame reportDisplayFrame = new JFrame("Received Message!");
                		  reportDisplayFrame.setSize(800, 600);
                		  reportDisplayFrame.getContentPane().setLayout(new FlowLayout());
                		  JTextArea reportDisplayText = new JTextArea(25, 70);
                		  reportDisplayText.setEditable(false);
                		  reportDisplayText.setLineWrap(true);
                		  reportDisplayText.setWrapStyleWord(true);
                		  reportDisplayText.setFont(Main.DIALOG_FONT);
                		  int horizontalPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
                		  int verticalPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
                		  String reportText = "Message received:\n\n" + message;
                		  reportDisplayText.setText(reportText);
                		  JScrollPane finalNumberScroll = new JScrollPane(reportDisplayText, verticalPolicy, horizontalPolicy);
                		  reportDisplayFrame.getContentPane().add(finalNumberScroll);
                		  JButton replyButton = new JButton("Reply to " + srcipaddr);
                		  replyButton.addActionListener( new ActionListener() {
    								public void actionPerformed(ActionEvent e) {
    									try {
    										Client.SendReplyMessage(srcip);
    									} catch (UnknownHostException e1) {
    										JOptionPane.showMessageDialog(null, "Unknown Host Error", "Error", JOptionPane.ERROR_MESSAGE);
    									} catch (NullPointerException e1) {
    										JOptionPane.showMessageDialog(null, "No Message Sent!", "No Message Sent!", JOptionPane.ERROR_MESSAGE);
    									} catch (ConnectException e1) {
    										JOptionPane.showMessageDialog(null, "Error: Unable to connect to server. The connection was refused.", "Connection Refused", JOptionPane.ERROR_MESSAGE);
    									}
    								}
                				  });
                		  JButton closeButton = new JButton("Close");
                		  closeButton.addActionListener(new ActionListener() {
    								public void actionPerformed(ActionEvent e) {
    									reportDisplayFrame.dispatchEvent(new WindowEvent(reportDisplayFrame, WindowEvent.WINDOW_CLOSING));
    								}
                				  });
                		  reportDisplayFrame.getContentPane().add(replyButton);
                		  reportDisplayFrame.getContentPane().add(closeButton);
                		  reportDisplayFrame.setVisible(true);
                		  */
                		  run();
                    }
                }
            }
        }
        catch (Exception e) 
        {
        	if (e instanceof BindException)
        		Main.log.err("[SERVER] Error: A server is already running on this port (" + SERVER_PORT + ").");
        	else if (e instanceof SocketException)
        		Main.log.err("[SERVER] Server closed by socket error.");
        	else
        	{
        		Main.log.err("[SERVER] Error: An error occurred during operation. Please send this error to the developers.");
                e.printStackTrace();
        	}
        	Main.log.warn("[SERVER] Server shutting down.");
        	Main.chkEnableServer.setSelected(false);
        	try {
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        	CloseServer();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e){}
        }
	}
	
	public void CloseServer()
	{
		Main.log.info_nln("[SERVER] Checking if server is running... ");
		if (!serverSocket.isClosed())
		{
			Main.log.info_ln("running. Shutting down.");
			if (upnp_open)
				UPNPClosePorts();
			try
			{
				serverSocket.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error: IO error occurred while shutting down server.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			//JOptionPane.showMessageDialog(null, "Server stopped! You will no longer be able to receive messages", "Server Stopped", JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			Main.log.info_ln("not running.");
			//JOptionPane.showMessageDialog(null, "Error: Cannot stop server as there is no server running.", "Server Not Running", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void UPNPOpenPorts()
	{
		PortMapping[] ports = new PortMapping[1];

		Main.log.info("[UPNP] Listing ports to be mapped");
		ports[0] = new PortMapping(SERVER_PORT, Main.LOCAL_IP_ADDRESS, PortMapping.Protocol.TCP, Main.CHAT_VERSION + " TCP");
		//arr[index] = new PortMapping(port, ipaddr, protocol, description);
		for (int i = 0; i < ports.length; i++)
		{
			Main.log.info("[UPNP/PORTMAPPER] Mapping " + i + ": Port " + ports[i].getExternalPort() + ", Protocol " + ports[i].getProtocol().toString());
		}
		Main.log.info("[UPNP] Initializing UPNP service.");
	    PortMappingListener pml = new PortMappingListener(ports);
	    Main.log.info("[UPNP] Registering port mappings.");
		UPNP_SERVICE = new UpnpServiceImpl(pml);
	    UPNP_SERVICE.getRegistry().addListener(REGISTRY_LISTENER);
	    Main.log.info("[UPNP/REGISTRY] Advertising local services.");
	    UPNP_SERVICE.getRegistry().advertiseLocalDevices();
	    Main.log.info("[UPNP/CONTROL-POINT] Sending search message to all devices and services, devices should respond soon.");
	    Main.log.info("[UPNP/CONTROL-POINT] This function's purpose is to try and find the router, then forward ports through it.");
	    UPNP_SERVICE.getControlPoint().search(new STAllHeader());
	    Main.log.info("[UPNP] It seems that we forwarded ports successfully!");
	    Main.log.warn("[UPNP] If it doesn't work, it should within a matter of seconds. You should've gotten some discovery messages. If no discovery messages are printed, something is wrong.");
	    upnp_open = true;
	}
	
	public void UPNPClosePorts()
	{
		Main.log.info("[UPNP] Shutting down port forwarding service.");
		//Main.log.info("[UPNP/REGISTRY] Disconnecting all remote devices.");
		//UPNP_SERVICE.getRegistry().removeAllRemoteDevices();
		//Main.log.info("[UPNP/REGISTRY] Disconnecting all local devices.");
		//UPNP_SERVICE.getRegistry().removeAllLocalDevices();
		UPNP_SERVICE.shutdown();
		Main.log.info("[UPNP] Port forwarding service shut down.");
		upnp_open = false;
	}
	
	public boolean isBlocked(String ip)
	{
		String rawbl = BlockList.getBlockList();
		String[] iparr = rawbl.split("\n");
		for (int i = 0; i < iparr.length; i++)
		{
			if (iparr[i].equals(ip))
				return true;
		}
		return false;
	}
}
