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
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.*;
import com.facebook.Session.*;
import com.facebook.model.*;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;


public class FacebookActivity extends Activity {	
	Button fblogin, fb_next;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_activity);
		
		/*	Inicializa botões e deixa eles invisíveis até receber a reposta do Facebook	*/
		fb_next	=	(Button)	findViewById(R.id.fb_nextbtn);
		fblogin = (Button) findViewById(R.id.fb_login);
		fblogin.setVisibility(View.INVISIBLE);
		fb_next.setVisibility(View.INVISIBLE);
		
		/*	Checa se existe uma sessão inicializada, se sim, checa se as permissões estão corretas e também mostra os botões	*/
		final Session session = Session.openActiveSessionFromCache(this);
		if(session != null)	{
			if (session.isOpened()) {
				Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						ProgressBar prg	=	(ProgressBar) findViewById(R.id.fb_loadspin);
						prg.setVisibility(View.INVISIBLE);
						if (user != null) {
							TextView welcome = (TextView) findViewById(R.id.fbauthorized);
							String welcomeText = String.format(Locale.getDefault(), getResources().getString(R.string.welcometext) , user.getName());
							welcome.setText(welcomeText);
							CommonHandler.FacebookUID = user.getId();
							CommonHandler.FacebookLocation = user.getLocation();
							CommonHandler.FacebookName = user.getName();
							CommonHandler.FacebookEmail = user.getProperty("email").toString();
							CommonHandler.InitDB(FacebookActivity.this);
							CommonHandler.dbman.setPreference("fbid", CommonHandler.FacebookUID);
							CommonHandler.dbman.setPreference("fbname", CommonHandler.FacebookName);
							String city = "None", country = "None";
							String username = user.getUsername();
							if(username == null)
								username = CommonHandler.FacebookUID;
							if(CommonHandler.FacebookLocation != null)	{
								city = CommonHandler.FacebookLocation.getCity();
								country = CommonHandler.FacebookLocation.getCountry();
							}
							HSAPI.AddUser(username, CommonHandler.FacebookName, CommonHandler.FacebookEmail, city, country);	
							CheckFBPerms(session);
							fblogin.setVisibility(View.INVISIBLE);
						}else
							fblogin.setVisibility(View.VISIBLE);
						fb_next.setVisibility(View.VISIBLE);
					}
				});
			}
		}else{
			ProgressBar prg	=	(ProgressBar) findViewById(R.id.fb_loadspin);
			prg.setVisibility(View.INVISIBLE);
			fblogin.setVisibility(View.VISIBLE);
			fb_next.setVisibility(View.VISIBLE);			
		}
		
		/*	Função de Login do Facebook	- Mesmo que o acima*/
		fblogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Session.openActiveSession(FacebookActivity.this, true, new Session.StatusCallback() {
					@Override
					public void call(final Session session, SessionState state, Exception exception) {
						if (session.isOpened()) {
							Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
								@Override
								public void onCompleted(GraphUser user, Response response) {
									if (user != null) {
										TextView welcome = (TextView) findViewById(R.id.fbauthorized);
										String welcomeText = String.format(Locale.getDefault(), getResources().getString(R.string.welcometext) , user.getName());
										welcome.setText(welcomeText);
										CommonHandler.FacebookUID = user.getId();
										CommonHandler.FacebookName = user.getName();
										CommonHandler.InitDB(FacebookActivity.this);
										CommonHandler.dbman.setPreference("fbid", CommonHandler.FacebookUID);
										CommonHandler.dbman.setPreference("fbname", CommonHandler.FacebookName);
										CheckFBPerms(session);
										fblogin.setVisibility(View.INVISIBLE);
									}else
										fblogin.setVisibility(View.VISIBLE);
									fb_next.setVisibility(View.VISIBLE);
								}
							});
						}
					}
				});
			}
		});

		fb_next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Settings.backtoSettings){
					Settings.backtoSettings = false;
					finish();
				}else{
					Intent intent = new Intent(v.getContext(), FirstConfig.class);
					startActivity(intent);
				}
			}
		});
	}
	/**
	 * Checa se as permissões foram dadas, e pede elas caso não.
	 * @param {Session} session
	 */
	public void CheckFBPerms(Session session)	{
		if(session != null)	{
		  	List<String> perms		=	session.getPermissions();
		  	List<String> pendingw	=	new ArrayList<String>();

	  		for(int p=0;p<CommonHandler.FB_permissions.length;p++)	{
	  			if(perms.indexOf(CommonHandler.FB_permissions[p]) == -1)
	  				pendingw.add(CommonHandler.FB_permissions[p]);
	  		}
	  		if(pendingw.size() > 0)
	  			session.requestNewPublishPermissions(new NewPermissionsRequest(FacebookActivity.this, pendingw));
	  		else{
		  		for(int p=0;p<CommonHandler.FB_read_perm.length;p++)	{
		  			if(perms.indexOf(CommonHandler.FB_read_perm[p]) == -1)
		  				pendingw.add(CommonHandler.FB_read_perm[p]);
		  		}	  			
		  		if(pendingw.size() > 0)
		  			session.requestNewReadPermissions(new NewPermissionsRequest(FacebookActivity.this, pendingw));
		  		else
		  			Log.i("SignalTracker::FBPerms", "Permissions OK");
	  		}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	  	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
}
