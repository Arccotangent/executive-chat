package net.arccotangent.executivechat.crypto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;

import net.arccotangent.executivechat.Main;
import net.arccotangent.executivechat.net.Server;

public class KeyManager {
	
	private static Socket socket;
	
	public static String pubkeyToB64(PublicKey pkey)
	{
		return Base64.encodeBase64String(pkey.getEncoded());
	}
	
	public static boolean havePublicKey(InetAddress ip)
	{
		String ipstr = ip.getHostAddress();
		Main.log.info_nln("[CRYPTO/KEYMGR/CHECKKEY] Checking for public key for IP address " + ipstr + "... ");
		File keyfile = new File(Main.KEY_DBDIR + "/" + ipstr);
		if (keyfile.exists())
		{
			Main.log.info_ln("found.");
			return true;
		}
		else
		{
			Main.log.info_ln("not found.");
			return false;
		}
		
	}
	
	public static PublicKey loadKeyByIP(InetAddress ip)
	{
		String ipstr = ip.getHostAddress();
		Main.log.info_nln("[CRYPTO/KEYMGR] Checking for public key for IP address " + ipstr + "... ");
		File keyfile = new File(Main.KEY_DBDIR + "/" + ipstr);
		PublicKey key = null;
		if (keyfile.exists())
		{
			Main.log.info_ln("found.");
			byte[] b64keyb = null;
			try {
				b64keyb = Files.readAllBytes(keyfile.toPath());
			} catch (IOException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error reading file. Error below, please send to developers.");
				e.printStackTrace();
			}
			byte[] rsakeyb = b64keyb;
			KeyFactory kf = null;
			try {
				kf = KeyFactory.getInstance(Main.PUBKEY_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			X509EncodedKeySpec pks = new X509EncodedKeySpec(rsakeyb);
			try {
				key = kf.generatePublic(pks);
			} catch (InvalidKeySpecException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error setting key specifications. Error below, please send to developers.");
				e.printStackTrace();
			}
		}
		else
		{
			Main.log.info_ln("not found. Requesting key.");
			PublicKey nkey = requestKeyFromIP(ip);
			savePublicKeyByIP(ip, nkey);
		}
		return key;
	}
	
	public static PublicKey requestKeyFromIP(InetAddress address)
	{
		PublicKey key = null;
		try
        {
            int port = Server.SERVER_PORT;
            int timeout = 2000;
            String host = address.getHostAddress();
            Main.log.info_nln("[CRYPTO/KEYMGR/CLIENT] Attempting to connect to server at " + host + " using timeout of " + timeout + " ms... ");
            socket = new Socket();
            try
            {
            	socket.connect(new InetSocketAddress(address, port), timeout);
            }
            catch (SocketTimeoutException e1)
            {
            	Main.log.info_ln("connection timed out.");
            	return null;
            }
            catch (ConnectException e2)
            {
            	Main.log.info_ln("connection refused.");
            	return null;
            }
            Main.log.info_ln("connected! Requesting key.");
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            String packet = "201 Send public key";
            bw.write(packet);
            bw.newLine();
            bw.flush();
            Main.log.info("[CRYPTO/KEYMGR/CLIENT] Requested public key from: " + host);
 
            //Get the return message from the server
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            //Main.log.info("[CRYPTO/KEYMGR/CLIENT] Message received from the server at " + host + " : " + message);
            if (message.equals("152 Pong"))
            {
            	JOptionPane.showMessageDialog(null, "Received ping response!", "Received Response", JOptionPane.INFORMATION_MESSAGE);
            }
            else if (message.equals("160 Message received confirmation"))
            {
            	//JOptionPane.showMessageDialog(null, "Server at " + host + " has received your message!", "Received Message", JOptionPane.INFORMATION_MESSAGE);
            	Main.log.info("[CRYPTO/KEYMGR/CLIENT] Received acknowledgement message from target server!");
            }
            else if (message.equals("161 Blocked"))
            {
            	Main.log.err("[CRYPTO/KEYMGR/CLIENT] The server at this address has blocked us.");
            	JOptionPane.showMessageDialog(null, "The server at this address has blocked us.", "Blocked By Server", JOptionPane.ERROR_MESSAGE);
            	return null;
            }
            else
            {
            	Main.log.info("[CRYPTO/KEYMGR/CLIENT] Received key. Saving key to disk.");
            	byte[] pubkeyb = Base64.decodeBase64(message);
            	KeyFactory kf = null;
            	X509EncodedKeySpec pubspec = new X509EncodedKeySpec(pubkeyb);
            	key = null;
            	kf = KeyFactory.getInstance(Main.PUBKEY_ALGORITHM);
            	key = kf.generatePublic(pubspec);
            	//savePublicKeyByIP(address, key);
            	//Main.log.info("[CRYPTO/KEYMGR] Saved key to disk.");
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
                return null;
            }
            Main.log.info_ln("success!");
        }
		return key;
	}
	
	public static void savePublicKeyByIP(InetAddress ip, PublicKey key)
	{
		String ipstr = ip.getHostAddress();
		File keyfile = new File(Main.KEY_DBDIR + "/" + ipstr);
		X509EncodedKeySpec pubspec = new X509EncodedKeySpec(key.getEncoded());
		try
		{
			keyfile.delete();
			keyfile.createNewFile();
			FileOutputStream fos = new FileOutputStream(keyfile.getAbsolutePath());
			fos.write(pubspec.getEncoded());
			fos.flush();
			fos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	protected static void deleteOwnKeyPair()
	{
		File pubfile = new File(Main.LOCAL_PUBFILE);
		File privfile = new File(Main.LOCAL_PRIVFILE);
		pubfile.delete();
		privfile.delete();
	}
	
	public static void saveOwnKeyPair(KeyPair kp)
	{
		PublicKey pub = kp.getPublic();
		PrivateKey priv = kp.getPrivate();
		
		X509EncodedKeySpec pubspec = new X509EncodedKeySpec(pub.getEncoded());
		File pubfile = new File(Main.LOCAL_PUBFILE);
		try {
			pubfile.createNewFile();
			FileOutputStream pubfos = new FileOutputStream(Main.LOCAL_PUBFILE);
			pubfos.write(pubspec.getEncoded());
			pubfos.flush();
			pubfos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		PKCS8EncodedKeySpec privspec = new PKCS8EncodedKeySpec(priv.getEncoded());
		File privfile = new File(Main.LOCAL_PRIVFILE);
		try {
			privfile.createNewFile();
			FileOutputStream privfos = new FileOutputStream(Main.LOCAL_PRIVFILE);
			privfos.write(privspec.getEncoded());
			privfos.flush();
			privfos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static PublicKey getOwnPublicKey()
	{
		PublicKey key = null;
		Main.log.info_nln("[CRYPTO/KEYMGR] Checking for local public key...");
		File keyfile = new File(Main.LOCAL_PUBFILE);
		if (keyfile.exists())
		{
			Main.log.info_ln("found.");
			byte[] b64keyb = null;
			try {
				b64keyb = Files.readAllBytes(keyfile.toPath());
			} catch (IOException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error reading public key file. Error below, please send to developers.");
				e.printStackTrace();
			}
			byte[] rsakeyb = b64keyb;
			KeyFactory kf = null;
			try {
				kf = KeyFactory.getInstance(Main.PUBKEY_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			X509EncodedKeySpec pks = new X509EncodedKeySpec(rsakeyb);
			try {
				key = kf.generatePublic(pks);
			} catch (InvalidKeySpecException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error setting key specifications. Error below, please send to developers.");
				e.printStackTrace();
			}
		}
		else
		{
			Main.log.info_ln("not found. Generating new RSA keypair.");
			KeyPair kp = RSA.generateRSAKeyPair();
			deleteOwnKeyPair();
			saveOwnKeyPair(kp);
			Main.log.info("[CRYPTO/KEYMGR] New RSA keypair generated and saved.");
			key = kp.getPublic();
		}
		return key;
	}
	
	public static PrivateKey getOwnPrivateKey()
	{
		PrivateKey key = null;
		Main.log.info_nln("[CRYPTO/KEYMGR] Checking for local private key...");
		File keyfile = new File(Main.LOCAL_PRIVFILE);
		if (keyfile.exists())
		{
			Main.log.info_ln("found.");
			byte[] b64keyb = null;
			try {
				b64keyb = Files.readAllBytes(keyfile.toPath());
			} catch (IOException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error reading private key file. Error below, please send to developers.");
				e.printStackTrace();
			}
			byte[] rsakeyb = b64keyb;
			KeyFactory kf = null;
			try {
				kf = KeyFactory.getInstance(Main.PUBKEY_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			PKCS8EncodedKeySpec pks = new PKCS8EncodedKeySpec(rsakeyb);
			try {
				key = kf.generatePrivate(pks);
			} catch (InvalidKeySpecException e) {
				Main.log.err("[CRYPTO/KEYMGR] Error setting key specifications. Error below, please send to developers.");
				e.printStackTrace();
			}
		}
		else
		{
			Main.log.info_ln("not found. Generating new RSA keypair.");
			KeyPair kp = RSA.generateRSAKeyPair();
			deleteOwnKeyPair();
			saveOwnKeyPair(kp);
			Main.log.info("[CRYPTO/KEYMGR] New RSA keypair generated and saved.");
			key = kp.getPrivate();
		}
		return key;
	}
}
