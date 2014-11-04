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


import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainScreen  extends FragmentActivity {	
	//	Callbacks
	public static STCallBack SignalCallBack;
	
	//	Localização
	public static Location lastLocation;
	
	//	Objectos Gráficos
	public static GoogleMap map;
	public ProgressBar signalBar;
	public TextView collectedData, connectionInfo, runMode, signalPercent;
	public ToggleButton tileView, controlLock;
	public static List<GroundOverlay> signals;
	public TileOverlay STOverlay;
	public AsyncTask<String, Integer, Long> LoadToMapTask;

	//	Booleans
	private boolean controlLocked, tileViewing;
	
	//String messages
	private static String gpssatmsg, netsatmsg;
	
	//	Handlers
	private static Handler MainScreenHandler = new Handler()	{
		@Override
		public void handleMessage(Message msg)	{
			if(map != null && signals != null)	{
				switch(msg.what)	{
					case 0:	//	AddSignal
						int lvl	=	FinalVars.SignalLevels[msg.getData().getShort("signal")];
						GroundOverlay sig = map.addGroundOverlay(new GroundOverlayOptions()
				        .image(BitmapDescriptorFactory.fromResource(lvl)).anchor(0.5f, 0.5f)
				        .position(new LatLng(msg.getData().getDouble("lat"), msg.getData().getDouble("lon")), CommonHandler.MinimumDistance*2f)); 
						signals.add(sig);
						if(signals.size() > CommonHandler.MaxMapContent)	{
							signals.get(0).remove();
							signals.remove(0);
						}
						Log.i("SignalTracker::MainScreenHandler","Added Signal "+sig);
						break;
					case 1:	//	AddTower
						Log.w("SignalTracker::MainScreenHandler","Adding tower is deprecated. Ignoring");
				}
			}else{
				Log.i("SignalTracker::MainScreenHandler","Variables not initialized, delaying in 2 seconds the message.");
				MainScreenHandler.sendMessageDelayed(msg, 2000);
			}
		}
		
	};
	
	//	Tasks
	private Runnable UpdateUI	=	new Runnable()	{

		@Override
		public void run() {
			if(CommonHandler.Signals != null)
				collectedData.setText(
						getResources().getString(R.string.signals)+
						": "+
						(CommonHandler.Signals.size()*CommonHandler.MinimumDistance/1000.0f)+
						" km - ("+
						CommonHandler.Operator+
						")"
						);
			if(controlLocked)	{
				if(lastLocation != null )	{
					if(CommonHandler.GPSLocation != null)
						if((lastLocation.getLatitude() != CommonHandler.GPSLocation.getLatitude() || lastLocation.getLongitude() != CommonHandler.GPSLocation.getLongitude()) )
							lastLocation = CommonHandler.GPSLocation;
				}else{
					if(CommonHandler.GPSLocation != null)	
						lastLocation = CommonHandler.GPSLocation;
				}
			}
			if(Utils.isBetterLocation(CommonHandler.GPSLocation, CommonHandler.NetLocation))
				connectionInfo.setText(String.format(Locale.getDefault(), gpssatmsg, CommonHandler.NumConSattelites, CommonHandler.NumSattelites));
			else
				connectionInfo.setText(String.format(Locale.getDefault(), netsatmsg, CommonHandler.NumConSattelites, CommonHandler.NumSattelites));
			signalBar.setProgress(CommonHandler.Signal);
			signalPercent.setText((Math.round((CommonHandler.Signal/31.0f)*100))+"%");
			if(lastLocation != null && controlLocked && CommonHandler.GPSLocation.getLatitude() != 0 && CommonHandler.GPSLocation.getLongitude() != 0)
				UpdateMapPosition(lastLocation.getLatitude(), lastLocation.getLongitude(), 15);
			MainScreenHandler.postDelayed(this, 2000);
		}
		
	};
	
	
	private void UpdateMapPosition(double latitude, double longitude, int zoom)	{
		if(map != null)	{
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),zoom));
			LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
	
			if(!bounds.contains(new LatLng(latitude, longitude)))        {
				CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(zoom).build(); 
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
			}
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainscreen);
		if(STService.Opened == false)	{
    		Intent myIntent = new Intent(MainScreen.this, STService.class);
    		startService(myIntent);
		}
		if(CommonHandler.ServiceMode == 1 | CommonHandler.ServiceMode == 3)	{
			Toast.makeText(this, getResources().getString(R.string.lightmodemsg), Toast.LENGTH_SHORT).show();
			finish();
		}
		gpssatmsg = getResources().getString(R.string.gpssatmsg);
		netsatmsg = getResources().getString(R.string.netsatmsg);

		controlLocked = true;
		tileViewing = false;
		
		signalBar		=	(ProgressBar)	findViewById(R.id.signalBar);
		collectedData	=	(TextView)		findViewById(R.id.collectedData);
		connectionInfo	=	(TextView)		findViewById(R.id.connectionInfo);
		runMode			=	(TextView)		findViewById(R.id.runMode);
		signalPercent	=	(TextView)		findViewById(R.id.signalPercent);
		tileView		=	(ToggleButton)	findViewById(R.id.tileViewBtn);
		controlLock		=	(ToggleButton)	findViewById(R.id.controlLockBtn);
		
		controlLock.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				controlLocked = !isChecked;
				if(map != null)	{
		            map.getUiSettings().setAllGesturesEnabled(!controlLocked);
					if(STOverlay != null)
						STOverlay.setVisible(tileViewing);
				}
				
			}
		});
		
		tileView.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				tileViewing = isChecked;
				if(STOverlay != null)
					STOverlay.setVisible(tileViewing);
				
			}
		});
		
		setUpMap();

		switch(CommonHandler.ServiceMode)	{
			case 0:	runMode.setText(getResources().getString(R.string.service_disabled));	break;
			case 1:	runMode.setText(getResources().getString(R.string.lightmode));			break;
			case 2:	runMode.setText(getResources().getString(R.string.fullmode));			break;
			case 3:	runMode.setText(getResources().getString(R.string.lightmodeoff));		break;
			case 4:	runMode.setText(getResources().getString(R.string.fullmodeoff));		break;
		}

		InitUp();
		CommonHandler.ServiceRunning = true;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@SuppressLint("NewApi")
	private void setUpMap() {
		signals = new ArrayList<GroundOverlay>();

		try {
	    		if(CommonHandler.Signals == null )	{
	                Intent MainMenuIntent  = new Intent().setClass(MainScreen.this, SplashScreen.class);
	                startActivity(MainMenuIntent);
	                finish();
	    		}
	            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.seemap)).getMap();
	            map.setMyLocationEnabled(false);
	            map.getUiSettings().setScrollGesturesEnabled(!controlLocked);
	            map.getUiSettings().setZoomGesturesEnabled(!controlLocked);
	            map.getUiSettings().setZoomControlsEnabled(false);
	            map.getUiSettings().setCompassEnabled(false);
				//if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
				//	LoadToMapTask = new LoadToMap().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				//} else {
		            LoadToMapTask = new LoadToMap().execute();
				//}
	            TileProvider tileProvider = new UrlTileProvider(256, 256) {
	                @Override
	                public synchronized URL getTileUrl(int x, int y, int zoom) {
	                    URL url = null;
	                    try {
		                    String s = String.format(Locale.US, HSAPI.TILES_SYNTAX , URLEncoder.encode(CommonHandler.Operator, "UTF-8"), zoom, x, y);
	                        url = new URL(s);
	                    } catch (Exception e) {
	                        throw new AssertionError(e);
	                    }
	                    return url;
	                }
	            };

	            STOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
				STOverlay.setVisible(tileViewing);
		}catch(Exception e)	{
			Log.e("SignalTracker::setUpMap", "Error: "+e.getMessage());
		}
	}
	public static class LoadToMap extends AsyncTask<String, Integer, Long> {
		/**
		 * Faz a chamada para API em modo assíncrono
		 * @param {String} params
		 * @return {Long} null
		 */
		@Override
		protected Long doInBackground(String... params) {
            int minSig = (CommonHandler.	Signals.size()-CommonHandler.MaxMapContent)>-1?CommonHandler.Signals.size()-CommonHandler.MaxMapContent:0;
           for(int i=CommonHandler.Signals.size()-1;i>minSig;i--)	{
    			SignalObject sig = CommonHandler.Signals.get(i);
    			Bundle sigdata = new Bundle();
    			Message sigmsg = new Message();
    			sigdata.putShort("signal", sig.signal);
    			sigdata.putDouble("lat", sig.latitude);
    			sigdata.putDouble("lon", sig.longitude);
    			sigmsg.what = 0;
    			sigmsg.setData(sigdata);
    			if(!MainScreenHandler.sendMessage(sigmsg))
    				Log.i("SignalTracker::MainScreen","Failed to put point on map");
    		}
			
			return null;
		}
	}
	public static int getDrawableIdentifier(Context context, String name) {
	    return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
	}
	private void InitUp()	{
		if(CommonHandler.ServiceMode <3 )	{
			SignalCallBack = new STCallBack()	{

				@Override
				public void Call(Object argument) {
					SignalObject sig = (SignalObject) argument;
					Bundle data = new Bundle();
					data.putDouble("lat", sig.latitude);
					data.putDouble("lon", sig.longitude);
					data.putShort("signal", sig.signal);
					Message msg = new Message();
					msg.setData(data);
					msg.what = 0;
					if(!MainScreenHandler.sendMessage(msg))	{
	    				Log.i("SignalTracker::MainScreen","Failed to put signal on map. Delaying 2s");
	    				MainScreenHandler.sendMessageDelayed(msg, 2000);
					}
				}
				
			};
			SignalCallBack.from = "MainScreen";

			CommonHandler.AddSignalCallback(SignalCallBack);
			if(CommonHandler.Signals != null)	{
				for(int i=0;i<CommonHandler.Signals.size();i++)
					SignalCallBack.Call(CommonHandler.Signals.get(i));
			}
		}
		MainScreenHandler.postDelayed(UpdateUI, 100);
	}
	private void CleanUp()	{
		CommonHandler.DelTowerCallback("MainScreen");
		CommonHandler.DelSignalCallback("MainScreen");
		MainScreenHandler.removeCallbacks(UpdateUI);
		MainScreenHandler.removeMessages(0);
		MainScreenHandler.removeMessages(1);
		
		List<GroundOverlay> st = signals;
		

		int lensig = signals.size();
		
		for(int i=0;i<lensig;i++)	
			st.get(i).remove();
	}
    @Override
    protected void onResume() {
        super.onResume();
    	CleanUp();
        try {
			if(STService.Opened == false)	{
	    		Intent myIntent = new Intent(MainScreen.this, STService.class);
	        	PendingIntent pendingIntent = PendingIntent.getService(MainScreen.this, 0, myIntent, 0);
	        	AlarmManager alarmManager = (AlarmManager)MainScreen.this.getSystemService(Context.ALARM_SERVICE);
	            Calendar calendar = Calendar.getInstance();
	            calendar.setTimeInMillis(System.currentTimeMillis());
	            calendar.add(Calendar.SECOND, 1);
	            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
			}
        }catch(Exception e)	{
        	Log.e("SignalTracker::onResume(MainScreen)","Erro ao iniciar serviço: "+e.getMessage());
        }
        if(!CommonHandler.ServiceRunning)	
			CommonHandler.LoadLists();
        setUpMap();
        InitUp();
    }
    @Override
    protected void onPause()	{
    	super.onPause();
    }
	@Override
	protected void onDestroy()	{
		super.onDestroy();
		CleanUp();
		if(LoadToMapTask!=null)
			LoadToMapTask.cancel(true);
	}
	
}
