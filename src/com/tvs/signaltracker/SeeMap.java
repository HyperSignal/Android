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
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
public class SeeMap extends FragmentActivity {
	public MapView mapView;
	public GoogleMap map;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.seemap);
			TelephonyManager Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			CommonHandler.Operator	=	Utils.DoOperator(Tel.getNetworkOperatorName());
			Log.i("SignalTracker::SeeMap","Operator: "+CommonHandler.Operator);
			setUpMap();
	}
	
	private void setUpMap() {

		try {
	        if (map == null) {
	            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.seemap))
	                    .getMap();
	            map.setMyLocationEnabled(true);
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

	             map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
	        }
		}catch(Exception e)	{
			Log.e("SignalTracker::setUpMap", "Error: "+e.getMessage());
		}
	}
    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }
}
