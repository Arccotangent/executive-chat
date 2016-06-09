package net.arccotangent.executivechat.crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import net.arccotangent.executivechat.Main;

public class RSA {
	
	public static KeyPair generateRSAKeyPair()
	{
		KeyPairGenerator gen = null;
		try {
			gen = KeyPairGenerator.getInstance(Main.PUBKEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			Main.log.err("[CRYPTO/RSA] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		Main.log.info("[CRYPTO/RSA] Initializing key pair generator.");
		gen.initialize(Main.PUBKEY_SIZE);
		Main.log.info("[CRYPTO/RSA] Generating " + Main.PUBKEY_SIZE + " bit RSA keypair.");
		KeyPair keys = gen.genKeyPair();
		return keys;
	}
	
	public static byte[] encryptText(String text, PublicKey key)
	{
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.PUBKEY_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			Main.log.err("[CRYPTO/RSA] Bad algorithm/padding. Error below, please send to developers.");
			e1.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/RSA] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] textbytes = text.getBytes();
		byte[] crypted = null;
		Main.log.info("[CRYPTO/RSA] Encrypting text with RSA.");
		try {
			crypted = c.doFinal(textbytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/RSA] Illegal block size/bad padding. Error below, please send to developers.");
			e.printStackTrace();
		}
		Main.log.info("[CRYPTO/RSA/NUKER] Nuking plaintext bytes.");
		textbytes = MsgCrypto.NUKE_BYTES_RAND(textbytes);
		Main.log.info("[CRYPTO/RSA/NUKER] Random pass 1/3");
		textbytes = MsgCrypto.NUKE_BYTES_RAND(textbytes);
		Main.log.info("[CRYPTO/RSA/NUKER] Random pass 2/3");
		textbytes = MsgCrypto.NUKE_BYTES_RAND(textbytes);
		Main.log.info("[CRYPTO/RSA/NUKER] Random pass 3/3");
		textbytes = MsgCrypto.NUKE_BYTES_ZERO(textbytes);
		Main.log.info("[CRYPTO/RSA/NUKER] Zeroed bytes.");
		Main.log.info("[CRYPTO/RSA/NUKER] Plaintext bytes nuked. ;)");
		return crypted;
	}
	
	public static String decryptText(byte[] crypt, PrivateKey pkey)
	{
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.PUBKEY_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e1) {
			Main.log.err("[CRYPTO/RSA] Bad algorithm/padding. Error below, please send to developers.");
			e1.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, pkey);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/RSA] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] ctbytes = crypt;
		byte[] decrypted = null;
		Main.log.info("[CRYPTO/RSA] Decrypting ciphertext with RSA.");
		try {
			decrypted = c.doFinal(ctbytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/RSA] Illegal block size/bad padding. Error below, please send to developers.");
			e.printStackTrace();
		}
		String pstr = new String(decrypted);
		return pstr;
	}
	
	public static String signText()
	{
		return null;
	}
}
