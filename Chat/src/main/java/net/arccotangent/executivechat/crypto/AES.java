package net.arccotangent.executivechat.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import net.arccotangent.executivechat.Main;

public class AES {
	
	public static SecretKey generateAESKey()
	{
		KeyGenerator gen = null;
		try {
			gen = KeyGenerator.getInstance(Main.SYMMETRIC_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			Main.log.err("[CRYPTO/AES] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		gen.init(Main.SYMMETRIC_SIZE);
		SecretKey key = gen.generateKey();
		return key;
	}
	
	public static byte[] encryptText(String text, SecretKey key)
	{
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.SYMMETRIC_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			Main.log.err("[CRYPTO/AES] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/AES] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] ct = null;
		try {
			ct = c.doFinal(text.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/AES] Illegal block size/bad padding. Error below, please send to developers.");
			e.printStackTrace();
		}
		return ct;
	}
	
	public static String decryptText(byte[] text, SecretKey key)
	{
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.SYMMETRIC_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			Main.log.err("[CRYPTO/AES] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			c.init(Cipher.DECRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/AES] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] ct = null;
		try {
			ct = c.doFinal(text);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/AES] Illegal block size/bad padding. Error below, please send to developers.");
			e.printStackTrace();
		}
		String ctstr = new String(ct);
		return ctstr;
	}

}
