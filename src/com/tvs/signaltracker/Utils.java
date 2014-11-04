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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.location.Location;
import android.util.Log;

public class Utils {
	private static final int DELTA_TIME = 1000 * 20;	//	20 segundos
	
    public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new EasySSLSocketFactory(trustStore);
            //sf.setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }
	public static JSONObject getODataJSONfromURL(String url){
		if(! (url == "")) {
    		//initialize
    		InputStream is = null;
    		String result = "";
    		JSONObject jArray = null;
    		String error = "";
    		//http post
    		try{
    			HttpClient httpclient = getNewHttpClient();
    			HttpGet httpget = new HttpGet(url);
    			HttpResponse response = httpclient.execute(httpget);
    			HttpEntity entity = response.getEntity();
    			is = entity.getContent();

    			

    		}catch(Exception e){
    			Log.e("SignalTracker::getJSONfromURL", "Error in http connection "+e.toString());
    			error = e.toString();
    		}

    		//convert response to string
    		try{
    			if(error == null || error == "") {
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
	    			StringBuilder sb = new StringBuilder();
	    			String line = null;
	    			while ((line = reader.readLine()) != null) {
	    				sb.append(line + "\n");
	    			}
	    			is.close();
	    			result=sb.toString();
    			}else{
    				result = "{result:'"+error+"'}";
    			}
    		}catch(Exception e){
    			Log.e("getJSONfromURL", "Error converting result "+e.toString());
    		}
    		//try parse the string to a JSON object
    		try{
        			result = TheUpCrypter.DecodeOData(result);
    	        	jArray = new JSONObject(result);
    		}catch(JSONException e){
    			Log.e("SignalTracker::getJSONfromURL", "Error parsing data "+e.toString());
    			Log.e("SignalTracker::getJSONfromURL", "Site output: "+result);
    			Log.e("SignalTracker::getJSONfromURL", "URL: "+url);
    		}
    		return jArray;
		}else{
			return null;
		}
	}
	public static JSONObject getJSONfromURL(String url){
		if(! (url == "")) {
    		//initialize
    		InputStream is = null;
    		String result = "";
    		JSONObject jArray = null;
    		String error = "";
    		//http post
    		try{
    			HttpClient httpclient = getNewHttpClient();
    			HttpGet httpget = new HttpGet(url);
    			HttpResponse response = httpclient.execute(httpget);
    			HttpEntity entity = response.getEntity();
    			is = entity.getContent();

    			

    		}catch(Exception e){
    			Log.e("SignalTracker::getJSONfromURL", "Error in http connection "+e.toString());
    			error = e.toString();
    		}

    		//convert response to string
    		try{
    			if(error == null || error == "") {
	    			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"),8);
	    			StringBuilder sb = new StringBuilder();
	    			String line = null;
	    			while ((line = reader.readLine()) != null) {
	    				sb.append(line + "\n");
	    			}
	    			is.close();
	    			result=sb.toString();
    			}else{
    				result = "{result:'"+error+"'}";
    			}
    		}catch(Exception e){
    			Log.e("getJSONfromURL", "Error converting result "+e.toString());
    		}

    		//try parse the string to a JSON object
    		try{
    	        	jArray = new JSONObject(result);
    		}catch(JSONException e){
    			Log.e("SignalTracker::getJSONfromURL", "Error parsing data "+e.toString());
    			Log.e("SignalTracker::getJSONfromURL", "Site output: "+result);
    			Log.e("SignalTracker::getJSONfromURL", "URL: "+url);
    		}
    		return jArray;
		}else{
			return null;
		}
	}
	public static String DoOperator(String op) {
		op = op.replace("fVIVO","VIVO")
			.replace("TIM 62", "TIM")
			.replace("TIM 3G+", "TIM")
			.replace("TIM 3G +", "TIM")
			.replace("72402", "TIM")
			.replace("72403", "TIM")
			.replace("72404", "TIM")
			.replace("72405", "CLARO")
			.replace("72406", "VIVO")
			.replace("72408", "TIM")
			.replace("72410", "VIVO")
			.replace("72411", "VIVO")
			.replace("72415", "SCT")
			.replace("72416", "BRT")
			.replace("72423", "TIM")
			.replace("72431", "OI")
			.replace("Oi", "OI");
		
		return op;
	}
	
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > DELTA_TIME;
	    boolean isSignificantlyOlder = timeDelta < -DELTA_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	public static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	public static Document XMLfromString(String xml) {

		Document doc = null;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}

		return doc;

	}
}
