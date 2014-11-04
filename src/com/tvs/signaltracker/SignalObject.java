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

public class SignalObject {
	public double latitude,longitude;
	public short signal;
	public short state;
	public float weight;
	public int id, mnc, mcc;
	
	public SignalObject(double latitude, double longitude, short signal)	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.signal = signal;
		this.state = 0;
		this.weight = 1f;
	}	
	public SignalObject(double latitude, double longitude, short signal, short state)	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.signal = signal;
		this.state = state;
		this.weight = 1f;
	}
	public SignalObject(double latitude, double longitude, short signal, short state, float weight)	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.signal = signal;
		this.state = state;
		this.weight = weight;
	}
	public SignalObject(double latitude, double longitude, short signal, short state, float weight, int mcc, int mnc)	{
		this.latitude = latitude;
		this.longitude = longitude;
		this.signal = signal;
		this.state = state;
		this.weight = weight;
		this.mcc = mcc;
		this.mnc = mnc;
	}
	public String toString()	{
		return "Signal ("+latitude+","+longitude+")["+signal+"] - Weight: "+weight;
	}
	public double distance(SignalObject target) {
		double dLat = ( Math.PI / 180 ) * (this.latitude-target.latitude);
		double dLon = ( Math.PI / 180 ) * (this.longitude-target.longitude);
		double lat1 = ( Math.PI / 180 ) * this.latitude;
		double lat2 = ( Math.PI / 180 ) * target.latitude;
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
		
		return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	}
	public void UpdateOperatorData(int mcc, int mnc)	{
		this.mcc = mcc;
		this.mnc = mnc;
	}
}
