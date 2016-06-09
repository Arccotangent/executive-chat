package net.arccotangent.executivechat.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import net.arccotangent.executivechat.Main;

public class MsgCrypto { 
	
	public static byte[] NUKE_BYTES_RAND(byte[] btn)
	{
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(btn);
		return btn;
	}
	
	public static byte[] NUKE_BYTES_ZERO(byte[] btn)
	{
		for (int i = 0; i < btn.length; i++)
		{
			btn[i] = 0x00;
		}
		return btn;
	}
	
	public static String getAESKey(String crypted)
	{
		int index = crypted.indexOf("\n");
		String aes = crypted.substring(0, index);
		return aes;
	}
	
	public static String encryptMsg(String msg, PublicKey rsa)
	{
		SecretKey aes = AES.generateAESKey();
		byte[] aescrypted = AES.encryptText(msg, aes);
		byte[] cryptedaeskey = encryptAESKey(aes, rsa);
		String acb64 = Base64.encodeBase64String(aescrypted);
		String ack64 = Base64.encodeBase64String(cryptedaeskey);
		String ecct = ack64 + "\n" + acb64;
		return ecct;
	}
	
	public static String decryptMsg(String msg, PrivateKey rsa, SecretKey aes)
	{
		byte[] data = Base64.decodeBase64(msg);
		String decryptedText = AES.decryptText(data, aes);
		return decryptedText;
	}
	
	public static String signText(String msg, PrivateKey rsa)
	{
		Signature sig = null;
		try {
			sig = Signature.getInstance(Main.SIGNATURE_TYPE);
		} catch (NoSuchAlgorithmException e) {
			Main.log.err("[CRYPTO/SIGNATURE] Bad algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			sig.initSign(rsa);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/SIGNATURE] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] signatureb = null;
		try {
			sig.update(msg.getBytes());
			signatureb = sig.sign();
		} catch (SignatureException e) {
			Main.log.err("[CRYPTO/SIGNATURE] Error signing text. Error below, please send to developers.");
			e.printStackTrace();
		}
		String signature = Base64.encodeBase64String(signatureb);
		return signature;
	}
	
	public static boolean verifyText(String msg, String signature, PublicKey rsa)
	{
		Signature sig = null;
		try {
			sig = Signature.getInstance(Main.SIGNATURE_TYPE);
		} catch (NoSuchAlgorithmException e) {
			Main.log.err("[CRYPTO/VERIFY] Bad algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			sig.initVerify(rsa);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/VERIFY] Invalid key error. Error below, please send to developers.");
			e.printStackTrace();
		}
		boolean verified = false;
		try {
			sig.update(msg.getBytes());
			verified = sig.verify(Base64.decodeBase64(signature));
		} catch (SignatureException e) {
			verified = false;
			Main.log.err("[CRYPTO/VERIFY] Error verifying text. Error below, please send to developers.");
			e.printStackTrace();
		}
		return verified;
	}
	
	public static byte[] encryptAESKey(SecretKey key, PublicKey rsakey)
	{
		byte[] akbytes = key.getEncoded();
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.PUBKEY_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			Main.log.err("[CRYPTO/AESKEY/ENCRYPT] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			c.init(Cipher.ENCRYPT_MODE, rsakey);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/AESKEY/ENCRYPT] Error initializing cipher object. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] eak = null;
		try {
			eak = c.doFinal(akbytes);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/AESKEY/ENCRYPT] Error encrypting AES key. Error below, please send to developers.");
			e.printStackTrace();
		}
		return eak;
	}
	
	public static SecretKey decryptAESKey(byte[] ekey, PrivateKey rsakey)
	{
		Cipher c = null;
		try {
			c = Cipher.getInstance(Main.PUBKEY_ALGORITHM);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			Main.log.err("[CRYPTO/AESKEY/DECRYPT] Error initializing algorithm. Error below, please send to developers.");
			e.printStackTrace();
		}
		try {
			c.init(Cipher.DECRYPT_MODE, rsakey);
		} catch (InvalidKeyException e) {
			Main.log.err("[CRYPTO/AESKEY/DECRYPT] Error initializing cipher object. Error below, please send to developers.");
			e.printStackTrace();
		}
		byte[] ak = null;
		try {
			ak = c.doFinal(ekey);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			Main.log.err("[CRYPTO/AESKEY/DECRYPT] Error decrypting AES key. Error below, please send to developers.");
			e.printStackTrace();
		}
		SecretKey key = new SecretKeySpec(ak, Main.SYMMETRIC_ALGORITHM);
		return key;
	}

}
