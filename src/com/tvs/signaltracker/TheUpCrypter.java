package com.tvs.signaltracker;

/**
 * @author Lucas Teske
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
 * 	Signal Mapping Project
    Copyright (C) 2012  Lucas Teske
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import android.util.Base64;

public class TheUpCrypter {
	public static byte[] crypted2;
	
	public static String GenOData(String text) {
		MCrypt crypter = new MCrypt();
		String output = "";
		try {
			byte[] crypted = crypter.encrypt(text);
			Deflater deflater = new Deflater();
			deflater.setInput(crypted);
			deflater.finish();
			crypted2 = crypted;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			while (!deflater.finished()) {
				int byteCount = deflater.deflate(buf);
				baos.write(buf, 0, byteCount);
			}
			deflater.end();
			byte[] compressed = baos.toByteArray();
			output =  Base64.encodeToString(compressed,Base64.DEFAULT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	public static String DecodeOData(String text) {
		MCrypt crypter = new MCrypt();
		try {		
			byte[] b64decoded = Base64.decode(text,Base64.DEFAULT);
			Inflater inflater = new Inflater();
			inflater.setInput(b64decoded);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[8192];
			while (!inflater.finished()) {
				int byteCount = inflater.inflate(buf);
				baos.write(buf, 0, byteCount);
			}
			inflater.end();
			byte[] uncompressed = baos.toByteArray();
			return new String(crypter.decrypt(uncompressed));
		} catch(Exception e)	{
			e.printStackTrace();
		}
		return null;
	}
}