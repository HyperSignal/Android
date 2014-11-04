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

import com.facebook.Session;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends Activity {
	Button	main_startbutton;
	Button	main_stopbutton;
	Button	main_configbutton;
	Button	main_exitbutton;
	Button	main_seemap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu);
		main_startbutton	=	(Button) findViewById(R.id.main_startbutton);
		main_stopbutton		=	(Button) findViewById(R.id.main_stopbutton);
		main_configbutton	=	(Button) findViewById(R.id.main_configbutton);
		main_exitbutton		=	(Button) findViewById(R.id.main_exitbutton);
		main_seemap			=	(Button) findViewById(R.id.main_seemap);
		
		main_startbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(CommonHandler.Configured){

					if(CommonHandler.ServiceMode == 1 | CommonHandler.ServiceMode == 3)	{
						if(STService.Opened == false)	{
				    		Intent myIntent = new Intent(MainMenu.this, STService.class);
				    		startService(myIntent);
						}
						CommonHandler.ServiceRunning = true;
						Toast.makeText(MainMenu.this, getResources().getString(R.string.lightmodemsg), Toast.LENGTH_SHORT).show();
						finish();
					}else{
		                Intent intent = new Intent(v.getContext(), MainScreen.class);
		                startActivity(intent);
					}
				}else{
	                Intent intent = new Intent(v.getContext(), FacebookActivity.class);
	                startActivity(intent);
				}
			}
		});
		main_stopbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CommonHandler.ServiceRunning = false;
				/*	Será que tem mais algo pra fazer aqui? */
			}
		});
		main_configbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(CommonHandler.Configured)	{
	                Intent intent = new Intent(v.getContext(), Settings.class);
	                startActivity(intent);		
				}else{
	                Intent intent = new Intent(v.getContext(), FacebookActivity.class);
	                startActivity(intent);					
				}
			}
		});
		main_exitbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		main_seemap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SeeMap.class);
                startActivity(intent);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

}
