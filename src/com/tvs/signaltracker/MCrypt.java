package com.tvs.signaltracker;

/**
 *  _   _                       ____  _                   _ 
 * | | | |_   _ _ __   ___ _ __/ ___|(_) __ _ _ __   __ _| |
 * | |_| | | | | '_ \ / _ \ '__\___ \| |/ _` | '_ \ / _` | |
 * |  _  | |_| | |_) |  __/ |   ___) | | (_| | | | | (_| | |
 * |_| |_|\__, | .__/ \___|_|  |____/|_|\__, |_| |_|\__,_|_|
 *       |___/|_|                      |___/               
 *  ____  _                   _ _____               _             
 * / ___|(_) __ _ _ __   __ _| |_   _| __ __ _  ___| | _____ _ __ 
 * \___ \| |/ _` | '_ \ / _` | | | || '__/ _` |/ __| |/ / _ \ '__|
 * _ __) | | (_| | | | | (_| | | | || | | (_| | (__|   <  __/ |   
 * |____/|_|\__, |_| |_|\__,_|_| |_||_|  \__,_|\___|_|\_\___|_|   
 *         |___/                                                 
 * 
 * Created by: Lucas Teske from Teske Virtual System
 * Package: com.tvs.signaltracker
 */


import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

public class MCrypt {

	private String iv = "***REMOVED***";
	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private Cipher cipher;
	private String SecretKey = "***REMOVED***";

	public MCrypt() {
		
		try {
		    ApplicationInfo ai = SignalTracker.getContext().getPackageManager().getApplicationInfo(SignalTracker.getContext().getPackageName(), PackageManager.GET_META_DATA);
		    Bundle bundle = ai.metaData;
		    iv = bundle.getString("ODATA_IV");
		    SecretKey = bundle.getString("ODATA_KEY");
		} catch (NameNotFoundException e) {
		    Log.e("MCrypt::MCrypt", "Failed to load meta-data, NameNotFound: " + e.getMessage());
		} catch (NullPointerException e) {
		    Log.e("MCrypt::MCrypt", "Failed to load meta-data, NullPointer: " + e.getMessage());         
		}
		
		ivspec = new IvParameterSpec(iv.getBytes());
		keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public byte[] encrypt(String text) throws Exception {
		if (text == null || text.length() == 0)
			throw new Exception("Empty string");

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			encrypted = cipher.doFinal(padString(text).getBytes());
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}

		return encrypted;
	}
	public byte[] decrypt(byte[] code) throws Exception {

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(code);
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}
	public byte[] decrypt(String code) throws Exception {
		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(hexToBytes(code));
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}

	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}

		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16)
				str = str + "0"
						+ java.lang.Integer.toHexString(data[i] & 0xFF);
			else
				str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(
						str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}

	private static String padString(String source) {
		char paddingChar = ' ';
		int size = 16;
		int x = source.length() % size;
		int padLength = size - x;

		for (int i = 0; i < padLength; i++) {
			source += paddingChar;
		}

		return source;
	}
}