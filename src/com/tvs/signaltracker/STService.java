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
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;
import android.location.GpsSatellite;
import android.location.GpsStatus;

public class STService extends Service{
	
	//	Notificações do serviço
	private static final String TAG = "SignalTracker";
	private static final int NOTIFICATION = R.string.app_name;

	//	Controle do Celular
	private TelephonyManager Tel;
	private MyPhoneStateListener MyListener;
	
	//	Controle do GPS
	private LocationManager mlocManager;	
	private LocationListener GPSLocListener;
	private LocationListener NetLocListener;
	private GPSStatusListener GPSs;
	private long mLastLocationTime;
	
	//	Internos do Serviço
	public static boolean LocalRunning = false;
	public static boolean Opened	=	false;
	NotificationCompat.Builder mBuilder;
	
	//	Tasks (HandlerType)
	private Handler ServiceHandler = new Handler();
	
	//	-	ReSendTask
	private Runnable ReSendRun = new Runnable() {
		public void run() {
			SupplicantState supState; 
			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			supState = wifiInfo.getSupplicantState();
			CommonHandler.WifiConnected = (supState == SupplicantState.COMPLETED);
			CommonHandler.DoResend();
			ServiceHandler.postDelayed(this, 10000);
		}
	};
	
	//	-	RunCheckTask
	private Runnable ReCheck = new Runnable()	{
		public void run()	{
			CheckRunning();
			ServiceHandler.postDelayed(this, 1000);
		}
	};
	
	//	-	LightModeTask
	private Runnable LightMode = new Runnable()	{
		public void run()	{
			try	{
				InitBase();
				GPSLocListener			=	new GPSLocationListener();
				mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonHandler.MinimumTime, CommonHandler.MinimumDistance,	GPSLocListener);	
				GPSs					=	new GPSStatusListener();
				mlocManager.addGpsStatusListener(GPSs);	
				LocalRunning = true;
				showServiceNotification();	
				Log.i("SignalTracker::LightMode","Searching location");
			}catch(Exception e)	{
				Log.e("SignalTracker::LightMode","Error: "+e.getMessage());
			}
		}
	};
	private void InitBase()	{
		Tel						=	(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		MyListener				=	new MyPhoneStateListener();
		
		mlocManager				=	(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		NetLocListener			=	new NETLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10,	NetLocListener);	
		Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS| PhoneStateListener.LISTEN_CELL_LOCATION );
		//CommonHandler.Operator	=	Utils.DoOperator(Tel.getNetworkOperatorName());
		Operator x = CommonHandler.dbman.getOperator(CommonHandler.MCC, CommonHandler.MNC);
		if(x != null)
			CommonHandler.Operator = x.name;
		else
			CommonHandler.Operator = CommonHandler.MCC+""+CommonHandler.MNC;
	}
	private void UpdateWiFi()	{
		SupplicantState supState; 
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		supState = wifiInfo.getSupplicantState();
		CommonHandler.WifiConnected = (supState == SupplicantState.COMPLETED);
		
	}
	private void UpdateData(boolean fromGPS)	{
		UpdateWiFi();
		if(CommonHandler.Signal > -1 & CommonHandler.Signal <= 31 & !CommonHandler.Operator.trim().isEmpty())	{
			if(Utils.isBetterLocation(CommonHandler.GPSLocation, CommonHandler.NetLocation))
				CommonHandler.AddSignal(CommonHandler.GPSLocation.getLatitude(), CommonHandler.GPSLocation.getLongitude(), CommonHandler.Signal, CommonHandler.Weight, true);
			else
				CommonHandler.AddSignal(CommonHandler.NetLocation.getLatitude(), CommonHandler.NetLocation.getLongitude(), CommonHandler.Signal, 0.2f, false);
			if((CommonHandler.ServiceMode == 1 || CommonHandler.ServiceMode == 3) & fromGPS)	{
				//	LightModes - dados atualizados. Então parar o serviço e agendar ele pra LightModeTime * 60 
				Log.i("SignalTracker::LightMode","Location Acquired, scheduling next search for "+(CommonHandler.LightModeDelayTime*60)+" seconds ahead.");
				CommonHandler.KillService = true;
				StopWorks();
	    		Intent myIntent = new Intent(STService.this, STService.class);
	        	PendingIntent pendingIntent = PendingIntent.getService(STService.this, 0, myIntent, 0);
	        	AlarmManager alarmManager = (AlarmManager)STService.this.getSystemService(Context.ALARM_SERVICE);
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTimeInMillis(System.currentTimeMillis());
	            calendar.add(Calendar.SECOND, CommonHandler.LightModeDelayTime * 60);
	            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
				this.stopSelf();
			}else if(CommonHandler.ServiceRunning)
				showServiceNotification();
		}
	}
	
	private void CheckRunning()	{
		if(CommonHandler.KillService){
			CommonHandler.ServiceRunning = false;
			StopWorks();
			this.stopSelf();
		}
		if(LocalRunning & !CommonHandler.ServiceRunning)	{
			Log.i("SignalTracker::STService","Stopping service workers");
			StopWorks();
		}
		if(!LocalRunning & CommonHandler.ServiceRunning)	{
			Log.i("SignalTracker::STService","Starting service workers");
			StartWorks();
		}
	}
	private void StopWorks()	{
		if(CommonHandler.ServiceMode < 3)
			ServiceHandler.removeCallbacks(ReSendRun);
		Log.i("SignalTracker::STService", "Stopping works");
		try {
			if(mlocManager != null)	{
				mlocManager.removeGpsStatusListener(GPSs);
				mlocManager.removeUpdates(GPSLocListener);
				mlocManager.removeUpdates(NetLocListener);
			}
			if(Tel != null)
				Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
		}catch(Exception e)	{}
		try {
			String ns = Context.NOTIFICATION_SERVICE;
		    NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
		    nMgr.cancel(NOTIFICATION);
		}catch(Exception e) {}
			
		GPSs			=	null;
		GPSLocListener	=	null;
		NetLocListener	=	null;
		CommonHandler.GPSFix = false;
		CommonHandler.NumSattelites = 0;
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.stservicestopped), Toast.LENGTH_LONG).show();
		LocalRunning = false;
		stopForeground(true); 
	}
	
	private void StartWorks()	{
		InitForeground();
		if(!CommonHandler.Configured | CommonHandler.dbman == null | CommonHandler.Signals == null )	{
			CommonHandler.Configured = false;
			try{CommonHandler.dbman.Close();}catch(Exception e){};
			CommonHandler.dbman = null;
			CommonHandler.Signals = null;
			CommonHandler.ServiceRunning = false;
			CommonHandler.ServiceMode = 0;
			CommonHandler.KillService = true;
			try{this.stopSelf();}catch(Exception e){};
            Intent MainMenuIntent  = new Intent().setClass(STService.this, SplashScreen.class);
            MainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(MainMenuIntent);
		}else{
			if(CommonHandler.ServiceMode < 3)	
					ServiceHandler.postDelayed(ReSendRun, 1000);

			if(CommonHandler.ServiceMode == 2 || CommonHandler.ServiceMode == 4)	{
				InitBase();
				GPSLocListener			=	new GPSLocationListener();
				mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, CommonHandler.MinimumTime, CommonHandler.MinimumDistance/10f,	GPSLocListener);	
				GPSs					=	new GPSStatusListener();
				mlocManager.addGpsStatusListener(GPSs);	
				showServiceNotification();
			}else{
				ServiceHandler.post(LightMode);
			}
			if( CommonHandler.ServiceMode != 1 && CommonHandler.ServiceMode != 3)
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.stservicestarted), Toast.LENGTH_LONG).show();
			LocalRunning = true;
		}
	}
    private void showServiceNotification() {
    	int signals = 0;
    	if(CommonHandler.Signals != null)
    		signals = CommonHandler.Signals.size();
    	String bartext = String.format(Locale.getDefault(), getResources().getString(R.string.barnotice) ,(signals*CommonHandler.MinimumDistance)/1000f, CommonHandler.NumConSattelites, CommonHandler.NumSattelites);
    	if(mBuilder == null)	{
    		String msg	= "";
    		if( CommonHandler.ServiceMode == 1 || CommonHandler.ServiceMode == 3)
    			msg = getResources().getString(R.string.lightmode);
    		else
    			msg = TAG;
    		mBuilder = new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_stat_service)
    	        .setContentTitle(msg)
    	        .setContentText(bartext);
	    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    		if( CommonHandler.ServiceMode == 1 || CommonHandler.ServiceMode == 3)	{
	    		Intent resultIntent = new Intent(this, MainMenu.class);
		    	stackBuilder.addParentStack(MainMenu.class);
		    	stackBuilder.addNextIntent(resultIntent);
    		}else{
	    		Intent resultIntent = new Intent(this, MainScreen.class);
		    	stackBuilder.addParentStack(MainScreen.class);
		    	stackBuilder.addNextIntent(resultIntent);    			
    		}
	    	PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
	    	mBuilder.setContentIntent(resultPendingIntent);
    	}else{
    		mBuilder.setContentText(bartext);
    	}
    	NotificationManager mNotificationManager =
    	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.notify(NOTIFICATION, mBuilder.build());   	

    }	
    private void InitForeground() {
    	if(mBuilder == null)	{
    		String msg	= "";
    		if( CommonHandler.ServiceMode == 1 || CommonHandler.ServiceMode == 3)
    			msg = getResources().getString(R.string.lightmode);
    		else
    			msg = TAG;
	    	mBuilder =
	    	        new NotificationCompat.Builder(this)
	    	        .setSmallIcon(R.drawable.ic_stat_service)
	    	        .setContentTitle(msg)
	    	        .setContentText(getResources().getString(R.string.notasktodo));
	    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    		if( CommonHandler.ServiceMode == 1 || CommonHandler.ServiceMode == 3)	{
	    		Intent resultIntent = new Intent(this, MainMenu.class);
		    	stackBuilder.addParentStack(MainMenu.class);
		    	stackBuilder.addNextIntent(resultIntent);
    		}else{
	    		Intent resultIntent = new Intent(this, MainScreen.class);
		    	stackBuilder.addParentStack(MainScreen.class);
		    	stackBuilder.addNextIntent(resultIntent);    			
    		}
	    	PendingIntent resultPendingIntent =  stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
	    	mBuilder.setContentIntent(resultPendingIntent);
    	}else
    		mBuilder.setContentText(getResources().getString(R.string.notasktodo));
		startForeground(NOTIFICATION, mBuilder.build());
	}
    
    /*	Classes auxiliares	*/
	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			CommonHandler.Signal = (short) signalStrength.getGsmSignalStrength();
			GsmCellLocation tower = (GsmCellLocation) Tel.getCellLocation();
			String networkOperator = Tel.getNetworkOperator();
			if(tower != null & networkOperator != null & networkOperator.length() > 0)	{
				int cid = tower.getCid();
				int lac = tower.getLac();
				int mcc = Integer.parseInt(networkOperator.substring(0, 3));
				int mnc = Integer.parseInt(networkOperator.substring(3));
				//TODO: Add MCC,MNC,LAC,CID to CommonHandler to Send through API
			}
			if(CommonHandler.ServiceMode != 2 && CommonHandler.ServiceMode != 4)
				UpdateData(false);
		}
	}

	// GPS
	public class GPSStatusListener	implements GpsStatus.Listener {
		@Override
		public void onGpsStatusChanged(int event) {
			switch(event)	{
				case GpsStatus.GPS_EVENT_FIRST_FIX:
	                CommonHandler.GPSFix = true;
	                break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					if(CommonHandler.GPSLocation != null)	{
							CommonHandler.GPSFix = (SystemClock.elapsedRealtime() - mLastLocationTime) < 10000;
					}
					GpsStatus status = mlocManager.getGpsStatus(null);
					if(status != null)	{
						Iterable<GpsSatellite>satellites = status.getSatellites();
						Iterator<GpsSatellite> sat = satellites.iterator();
						int count = 0;
						int conncount = 0;
						GpsSatellite st = null;
						while(sat.hasNext())	{
							st = sat.next();
							if(st != null)	{
								conncount = (st.usedInFix())?conncount+1:conncount;
								count++;
							}
						}
						CommonHandler.NumSattelites = count;
						CommonHandler.NumConSattelites = conncount;
					}
					break;
			}
		}
	}
	public class GPSLocationListener implements LocationListener {
		
		public GPSLocationListener() {
			CommonHandler.GPSLocation = new Location("");			
		}
		@Override
		public void onLocationChanged(Location loc) {
			CommonHandler.GPSLocation = loc;
			mLastLocationTime = SystemClock.elapsedRealtime();
			UpdateData(true);
		}
		@Override
		public void onProviderDisabled(String provider) {
			CommonHandler.GPSEnabled = false;
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsdisabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			CommonHandler.GPSEnabled = true;
			Toast.makeText(getApplicationContext(), getResources().getString(R.string.gpsenabled), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

	//Network
	public class NETLocationListener implements LocationListener {
		public NETLocationListener() {
			CommonHandler.NetLocation = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if(CommonHandler.NetLocation == null) 
				CommonHandler.NetLocation = new Location("");			
		}
		@Override
		public void onLocationChanged(Location loc) {
			CommonHandler.NetLocation = loc;
			if(CommonHandler.Signal > -1 & CommonHandler.Signal <= 31 & !CommonHandler.Operator.trim().isEmpty())	{
				CommonHandler.AddSignal(CommonHandler.NetLocation.getLatitude(), CommonHandler.NetLocation.getLongitude(), CommonHandler.Signal, 0.2f, false);
			}
		}
		@Override
		public void onProviderDisabled(String arg0) {}
		@Override
		public void onProviderEnabled(String provider) {}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}	
	@Override
	public IBinder onBind(Intent intent) {return null;}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Log.i("SignalTracker::STService", "Service Started");
		ServiceHandler.postDelayed(ReCheck, 1000);
	}
	
	@Override
	public void onCreate() {
		try	{
			CommonHandler.InitDB(this);		
			if(!CommonHandler.ServiceRunning)	{
				/*	Inicializar Banco de Dados	*/
				CommonHandler.dbman.LoginDB("STService");
				CommonHandler.LoadPreferences();
				
				/*	Inicializar Listas e Callbacks	*/
				CommonHandler.LoadLists();
				CommonHandler.InitLists();
				CommonHandler.InitCallbacks();
				
				CommonHandler.dbman.LogoutDB("STService");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::STService(onCreate)","Error: "+e.getMessage());
		}
		Opened = true;
	}
	@Override
	public void onDestroy() {
		ServiceHandler.removeCallbacks(ReCheck);
		ServiceHandler.removeCallbacks(ReSendRun);
		ServiceHandler.removeCallbacks(LightMode);
		
		if(CommonHandler.ServiceMode == 2 || CommonHandler.ServiceMode == 4){
			Log.i("SignalTracker::STService", "Parando trabalhos");
			try	{
				if(mlocManager != null)	{
					mlocManager.removeGpsStatusListener(GPSs);
					mlocManager.removeUpdates(GPSLocListener);
					mlocManager.removeUpdates(NetLocListener);
				}
				if(Tel != null)
					Tel.listen(MyListener, PhoneStateListener.LISTEN_NONE);
			}catch(Exception e){};
			
			MyListener 		=	null;
			Tel				=	null;
			mlocManager		=	null;
			GPSs			=	null;
			GPSLocListener	=	null;
			NetLocListener	=	null;
			CommonHandler.GPSFix = false;
			CommonHandler.NumSattelites = 0;
			CommonHandler.NumConSattelites = 0;
			try {
				String ns = Context.NOTIFICATION_SERVICE;
			    NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
			    nMgr.cancel(NOTIFICATION);
			}catch(Exception e) {}
		}
		Toast.makeText(getApplicationContext(), getResources().getString(R.string.stservicestopped), Toast.LENGTH_LONG).show();
		if(!CommonHandler.KillService)	{
	    	Intent myIntent = new Intent(STService.this, STService.class);
	    	PendingIntent pendingIntent = PendingIntent.getService(STService.this, 0, myIntent, 0);
	    	AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	        Calendar calendar = Calendar.getInstance();
	        calendar.setTimeInMillis(System.currentTimeMillis());
	        calendar.add(Calendar.SECOND, 2);
	        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);  
		}
		CommonHandler.KillService = false;
		LocalRunning = false;
		Opened = false;
	}
}
