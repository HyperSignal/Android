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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class DatabaseManager {
	private static final String DATABASE_NAME = "signaltracker.db";
	private static final int DATABASE_VERSION = 6;
	
	private Context context;
	private SQLiteDatabase db;
	private SQLiteStatement insSignal;
	private static final String INSERTSIGNAL = "insert into signals(latitude,longitude,sinal,state) values (?,?,?,0)";
	
	private List<DBUsers> users;
	
	/**
	 * Initializes a Database Manager
	 * @param context The Context that will initialize the database
	 */
	public DatabaseManager(Context context) {
		this.context = context;
	    OpenHelper openHelper = new OpenHelper(this.context);
	    this.db = openHelper.getWritableDatabase();
	    this.insSignal = this.db.compileStatement(INSERTSIGNAL);
	    this.users = new ArrayList<DBUsers>();
	}
	
	/**
	 * Do a login on database
	 * @param user	The Activity name that will use the database
	 */
	public void LoginDB(String user)	{
		this.users.add(new DBUsers(user));
	}
	
	/**
	 * Do a logout on database
	 * @param user	The Activity name that used the database
	 */
	public void LogoutDB(String user)	{
		for(int i=0;i<this.users.size();i++)	{
			if(this.users.get(i).name == user)	{
				this.users.remove(i);
				break;
			}
		}
	}
	
	/**
	 * Deletes a Signal from database with a ID
	 * @param id	The Database ID of Signal
	 */
	public void deleteSignal(int id)	{ 
		this.db.delete("signals", "id=?", new String[] {Integer.toString(id) } );
	}
	
	/**
	 * 
	 * @param latitude		The Latitude of Signal
	 * @param longitude		The Longitude of Signal
	 * @param sinal			The Strenght of Signal
	 * @return	The Row ID of inserted Signal
	 */
	public long insertSignal (Double latitude, Double longitude, int sinal) {
		this.insSignal.bindDouble(1, latitude);
		this.insSignal.bindDouble(2, longitude);
		this.insSignal.bindLong(3, sinal);
		return this.insSignal.executeInsert();
	}

	/**
	 * 	Do a clean on Signals table.
	 * 	This will erase all signal data
	 * 	stored on device.
	 */
	public void CleanTowerSignal() {
		this.db.delete("signals", null, null);
	}
	
	/**
	 * 	Do a clean on Preferences table.
	 * 	This will erase all preferences stored
	 * 	on device.
	 */
	public void CleanPreferences()	{
		this.db.delete("preferences", null, null);
	}
	
	/**
	 * 	Do a complete clean on database.
	 * 	This will reset the Database to original state.
	 */
	public void CleanAll()	{
		this.db.delete("signals", null, null);
		this.db.delete("preferences", null, null);		
	}
	
	/**
	 * 	Closes the database, if no users (activities) are logged
	 */
	public void Close() {
		if(this.users.size() == 0)
			this.db.close();
	}
	
	/**
	 * Check if the database is open.
	 * @return	<b>True</b> if database is open. <b>False</b> otherwise.
	 */
	public boolean isOpen()	{
		return this.db.isOpen();
	}
	
	/**
	 * Opens a database with the context.
	 * It will only open the database if it 
	 * is not open.
	 * @param context	The context that will open database
	 */
	public void Open(Context context)	{
		if(!this.db.isOpen())	{
			this.context = context;
		    OpenHelper openHelper = new OpenHelper(this.context);
		    this.db = openHelper.getWritableDatabase();
		    this.insSignal = this.db.compileStatement(INSERTSIGNAL);
		    if(this.users == null)
		    	this.users = new ArrayList<DBUsers>();
		}
	}
	
	/**
	 * Updates a signal data on database.
	 * This will delete the signal from database 
	 * if <b>state == 2</b>, or update all data if <b>signal != 2</b>
	 * @param lat	Latitude of Signal
	 * @param lon	Longitude of Signal
	 * @param signal	Strength of Signal
	 * @param state	State of Signal
	 * @return	Number of rows affected (if all right, 1, else 0)
	 */
	public long UpdateSignal(double lat, double lon, short signal, short state)	{
		if(state == 2)	{
			return this.db.delete("signals", "latitude=? and longitude=? and sinal=?", new String[] { Double.toString(lat), Double.toString(lon), Integer.toString(signal) });
		}else{
			ContentValues values = new ContentValues();
			values.put("state", state);
			return this.db.update("signals", values, "latitude=? and longitude=? and sinal=?", new String[] { Double.toString(lat), Double.toString(lon), Integer.toString(signal) });	
		}
	}
	
	/**
	 * 	Performes a cleanup for already sent signals
	 * 	that still remains on database.
	 */
	public void CleanDoneSignals()	{
		this.db.delete("signals", "state=?", new String[] { "2" });
	}
	
	/**
	 * Get the list of signals stored in database.
	 * @return	List of SignalObject
	 */
	public List<SignalObject> getSignals() {
		List<SignalObject> table = new ArrayList<SignalObject>();
		Cursor cursor = this.db.query("signals", new String[] { "latitude", "longitude", "sinal", "state", "id"}, null, null, null, null, null, null);
		if(cursor.moveToFirst()) {
			do {
				if(cursor.getShort(3)!=2)	{
					SignalObject tmp = new SignalObject(cursor.getDouble(0),cursor.getDouble(1),(short) cursor.getShort(2),(short) 0);
					tmp.id = cursor.getInt(4);
					table.add(tmp);
				}
			}while(cursor.moveToNext());
		}
		if(cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return table;
	}

	/**
	 * Sets a preference value on database
	 * @param key	The preference key that you want to set
	 * @param value	The preference value that set at key
	 * @return Number of Rows affected if preference exists, or Row ID if not.
	 */
	public long setPreference(String key, String value)	{
		if(key != null & value != null)	{
			if(getPreference(key) != null)	{
				ContentValues values = new ContentValues();
				values.put("prefval", value);		
				return this.db.update("preferences", values, "prefkey=?", new String[] { key });			
			}else{
				ContentValues values = new ContentValues();
				values.put("prefkey", key);
				values.put("prefval", value);
				return this.db.insert("preferences", null, values);
			}
		}else
			return -1;
	}
	
	/**
	 * Get the value of preference key in database.
	 * @param key	The preference key
	 * @return	Returns the value of preference, or null if not found.
	 */
	public String	getPreference(String key)	{
		if(key != null)	{
			Cursor cursor = this.db.query("preferences", new String[] 	{ "prefkey", "prefval" }, "prefkey = ?" , new String[] { key }, null, null, null);
			if(cursor.moveToFirst())	{
				String result = cursor.getString(cursor.getColumnIndex("prefval"));
				cursor.close();
				return result;
			}else{
				cursor.close();
				return null;
			}
		}else{
			return null;
		}
	}
	
	/**
	 * Insert one operator to database
	 * @param mcc		The Mobile Country Code from Operator
	 * @param mnc		The pMobile Network Code from Operator
	 * @param name		The Operator Short Name
	 * @param fullname	The Operator Fullname
	 * @return Number of Rows affected if preference exists, or Row ID if not.
	 */
	public long insertOperator(int mcc, int mnc, String name, String fullname)	{
			if(getOperator(mcc,mnc) != null)	{
				ContentValues values = new ContentValues();
				values.put("name", name);	
				values.put("fullname", fullname);		
				return this.db.update("operators", values, "mcc=? and mnc=?", new String[] { Integer.toString(mcc), Integer.toString(mnc) });			
			}else{
				ContentValues values = new ContentValues();
				values.put("name", name);	
				values.put("fullname", fullname);	
				values.put("mcc",mcc);
				values.put("mnc",mnc);
				return this.db.insert("operators", null, values);
			}
	}
	
	/**
	 * Get the operator data from database
	 * @param mcc		The Mobile Country Code from Operator
	 * @param mnc		The pMobile Network Code from Operator
	 * @return	Returns an Operator object, or null if not found
	 * @see Operator
	 */
	public Operator	getOperator(int mcc, int mnc)	{
			Cursor cursor = this.db.query("operators", new String[] 	{ "mcc","mnc","name", "fullname" }, "mcc=? and mnc=?" , new String[] { Integer.toString(mcc), Integer.toString(mnc) }, null, null, null);
			if(cursor.moveToFirst())	{
				Operator operator = new Operator(cursor.getInt(cursor.getColumnIndex("mcc")),cursor.getInt(cursor.getColumnIndex("mnc")),cursor.getString(cursor.getColumnIndex("name")),cursor.getString(cursor.getColumnIndex("fullname")));
				cursor.close();
				return operator;
			}else{
				cursor.close();
				return null;
			}
	}
	/**
	 * Get the operator list data from database
	 * @return	Returns an Operator object array, or null if not found
	 * @see Operator
	 */
	public Operator[] getOperatorList()	{
		Operator[] OperatorList = null;
		Cursor cursor = this.db.query("operators", new String[] 	{ "mcc","mnc","name", "fullname" }, null , null, null, null, null);
		if(cursor.moveToFirst())	{
			OperatorList = new Operator[cursor.getCount()];
			for(int i=0,len=OperatorList.length;i<len;i++)	{
				OperatorList[i] = new Operator(cursor.getInt(cursor.getColumnIndex("mcc")),cursor.getInt(cursor.getColumnIndex("mnc")),cursor.getString(cursor.getColumnIndex("name")),cursor.getString(cursor.getColumnIndex("fullname")));
				cursor.moveToNext();
			}
		}
		cursor.close();
		return OperatorList;
	}
	/**
	 * Static DBUser Class for manage activities that is acessing database
	 * @author Lucas Teske
	 *
	 */
	private static class DBUsers {
		public String name;
		public DBUsers(String name)	{
			this.name = name;
		}
	}
	
	/**
	 * Static Helper Class do manage SQLite Database
	 * @author Lucas Teske
	 *
	 */
	private static class OpenHelper extends SQLiteOpenHelper {

		/**
		 * Opens the Helper
		 * @param context	The context that will initialize the helper
		 */
	    OpenHelper(Context context) {
	       super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) { 
	    	Log.i("Database Manager", "Creating base table");
	    	db.execSQL("CREATE TABLE signals (id INTEGER PRIMARY KEY, latitude DOUBLE, longitude DOUBLE, sinal INTEGER, state INTEGER)");
	    	db.execSQL("CREATE TABLE preferences (prefkey TEXT PRIMARY KEY, prefval TEXT)");
	    	db.execSQL("CREATE TABLE operators (mcc INTEGER NOT NULL , mnc INTEGER NOT NULL, name TEXT, fullname TEXT, PRIMARY KEY(mcc,mnc))");
	    }
	    /**
	     * Fills the new database with data from the old database.
	     * @param db	The Database
	     * @param sigtable	The List of SignalObject
	     * @param preferences	The HashMap of preferences
	     */
	    private void FillNewDB(SQLiteDatabase db, List<SignalObject> sigtable, HashMap<String, String> preferences)	{
	    	SQLiteStatement insSignal = db.compileStatement(INSERTSIGNAL);
			Iterator<Entry<String, String>> it = preferences.entrySet().iterator();
			while(it.hasNext())	{
		        HashMap.Entry<String, String> pairs = (HashMap.Entry<String, String>)it.next();
		        //System.out.println(pairs.getKey() + " = " + pairs.getValue());
		        Log.i("Database Manager", "Inserting preference ("+pairs.getKey()+"): "+pairs.getValue());
		    	ContentValues values = new ContentValues();
				values.put("prefkey", pairs.getKey());
				values.put("prefval", pairs.getValue());
				db.insert("preferences", null, values);
		        it.remove(); // avoids a ConcurrentModificationException
			}
			for(int i=0;i<sigtable.size();i++)	{
				SignalObject sig = sigtable.get(i);
				insSignal.bindDouble(1, sig.latitude);
				insSignal.bindDouble(2, sig.longitude);
				insSignal.bindLong(3, sig.signal);
				Log.i("Database Manager", "Inserting Signal: "+sig.toString());
				insSignal.executeInsert();
			}
			sigtable = null;
			preferences = null;
	    }
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	       Log.w("Database Manager", "Upgrading table.");
	       
			List<SignalObject> sigtable = new ArrayList<SignalObject>();
			Log.i("Database Manager", "Loading Signals");
			Cursor cursor = db.query("signals", new String[] { "latitude", "longitude", "sinal", "state", "id"}, null, null, null, null, null, null);
			if(cursor.moveToFirst()) {
				do {
					if(cursor.getShort(3)!=2)	{
						SignalObject tmp = new SignalObject(cursor.getDouble(0),cursor.getDouble(1),(short) cursor.getShort(2),(short) 0);
						tmp.id = cursor.getInt(4);
						sigtable.add(tmp);
					}
				}while(cursor.moveToNext());
			}
			if(cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			HashMap<String, String> preferences = new HashMap<String, String>();
			Log.i("Database Manager", "Loading preferences");
			cursor = db.query("preferences", new String[] 	{ "prefkey", "prefval" }, null , null, null, null, null);
			
			if(cursor.moveToFirst())	{
				do	{
					preferences.put(cursor.getString(cursor.getColumnIndex("prefkey")), cursor.getString(cursor.getColumnIndex("prefval")));
				}while(cursor.moveToNext());
			}
				
			if(cursor != null && !cursor.isClosed())
				cursor.close();
			
	       db.execSQL("DROP TABLE IF EXISTS signals");
	       db.execSQL("DROP TABLE IF EXISTS towers");
	       db.execSQL("DROP TABLE IF EXISTS preferences");
	       db.execSQL("DROP TABLE IF EXISTS operators");
	       onCreate(db);
	       FillNewDB(db, sigtable, preferences);
	       sigtable = null;
	       preferences = null;
	    }
	 }	
}