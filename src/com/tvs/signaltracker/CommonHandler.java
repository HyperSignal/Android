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

import com.facebook.model.GraphLocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CommonHandler {
	/*	Constantes	*/
	public static final String[] FB_permissions =	{	"publish_stream"	};	//	Permissões padrões do Facebook 
													 
	public static final String[] FB_read_perm	=	{	"email",				//	Permissões de Leitura no Face
														"photo_upload"	};

	public static final int	MaxMapContent		=	40;							//	
	
	/*	Variáveis de funcionamento	*/
	
	public static DatabaseManager dbman;
	public static Boolean	PreferencesLoaded	=	false;			//	Se as preferências foram carregadas
	public static Boolean	ServiceRunning		=	false;			//	Se o serviço está rodando
	public static Boolean	KillService			=	false;			//	Use para matar o serviço
	public static Boolean	GPSFix				=	false;			//	Se o GPS está com uma posição
	public static Boolean	GPSEnabled			=	false;			//	Se o GPS está ativado
	public static Boolean	WifiConnected		=	false;			//	Se o Wifi está conectado
	
	public static Location 	GPSLocation;							//	Localização pelo GPS
	public static Location 	NetLocation;							//	Localização pela Rede
	public static int		NumSattelites;							//	Número de Satélites
	public static int		NumConSattelites;						//	Número de Satélites Conectados
	public static short		Signal;									//	Sinal do celular
	
	public static float		Weight				=	1f;				//	Peso do sinal (para média)
	
	/*	Listas	*/
	public static List<SignalObject>	Signals;
	
	/*	Callbacks	*/
	private static List<STCallBack>		SignalCallbacks;
	private static List<STCallBack>		TowerCallbacks;
	
	/*	Preferências	*/
	public static Boolean	Configured 				= 	false;			//	Se o cliente foi configurado
	public static Boolean	WakeLock				=	false;			//	A tela permanecerá ativa até fechar o aplicativo
	public static Boolean	WifiSend				=	false;			//	Somente enviar com WiFi Ligado
	public static String	FacebookUID				= 	"0";			//	UID do Facebook, caso logado
	public static String	FacebookName			=	"Anônimo";		//	Nome no Facebook, caso logado
	public static String	FacebookEmail			=	"";				//	Email no Facebook, caso logado
	public static String	LastOperator			=	"";				//	Ultima operadora
	public static String	Operator				=	"";				//	Operadora atual
	public static short		ServiceMode				=	0;				//	0 => Sem Rodar, 1 => Modo Light, 2 => Modo Full, 3 => Modo Offline Light, 4 => Modo Offline Full
	public static int		MinimumDistance			=	50;				//	Distancia Mínima entre pontos em Metros
	public static int		MinimumTime				=	0;				//	Tempo mínimo entre procuras do GPS em Segundos
	public static int		LightModeDelayTime		=	30;				//	Tempo de espera do modo Light
	public static double	SentSignals				=	0;				//	Sinais Enviados
	public static int		SentTowers				=	0;				//	Torres Enviadas
	public static int		MCC						=	0;				//	Mobile Country Code
	public static int		MNC						=	0;				//	Mobile Network Code
	
	public static Operator[]	OperatorList;							//	List of Operators
	
	public static GraphLocation FacebookLocation;						//	Localização no Facebook, caso logado
	

	
	/*	Métodos	*/
	/**
	 * Initializes the Lists of Tower and Signals
	 */
	public static void InitLists()	{
		if(Signals == null)
			Signals = new ArrayList<SignalObject>();
	}
	
	/**
	 * Loads the operator data
	 */
	public static void InitOperatorData(Context ctx)	{
		TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String netoperator = manager.getSimOperator ();
		MCC = Integer.parseInt(netoperator.substring(0, 3));
		MNC = Integer.parseInt(netoperator.substring(3));
	}
	/**
	 * Loads the database data into Towers and Signal Lists
	 */
	public static void LoadLists()	{
		if(dbman != null)	{
			Log.i("SignalTracker::LoadLists","Cleaning sent signals.");
			dbman.CleanDoneSignals();
			Log.i("SignalTracker::LoadLists","Cleaning sent towers.");
			dbman.CleanDoneTowers();
			Signals = dbman.getSignals();
		}else
			Log.e("SignalTracker::LoadLists","DatabaseManager is null! ");
	}
	/**
	 * 	Initializes the Callback Lists
	 */
	public static void InitCallbacks()	{
		if(SignalCallbacks == null)
			SignalCallbacks = new ArrayList<STCallBack>();
		if(TowerCallbacks == null)
			TowerCallbacks = new ArrayList<STCallBack>();
	}
	
	/**
	 * Adds a SignalTracker Signal Callback
	 * @param cb	STCallBack for Signal
	 * @see STCallBack
	 */
	public static void AddSignalCallback(STCallBack cb)	{
		if(SignalCallbacks != null)
			SignalCallbacks.add(cb);
		else
			Log.e("SignalTracker::AddSignalCallback","Callbacks list of signals is null!");
	}	
	
	/**
	 * Adds a SignalTracker Tower Callback
	 * @param cb	STCallback for Tower
	 * @see STCallBack
	 */
	public static void AddTowerCallback(STCallBack cb)	{
		if(TowerCallbacks != null)
			TowerCallbacks.add(cb);
		else
			Log.e("SignalTracker::AddTowerCallback","Callbacks list of towers is null!");
	}
	
	/**
	 * Deletes a Signal Callback with the ID
	 * @param id	The id of Signal Callback
	 */
	public static void DelSignalCallback(int id)	{
		try	{
			if(SignalCallbacks != null)	{
				SignalCallbacks.remove(id);
				Log.i("SignalTracker::DelSignalCallback","Removing("+id+")");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelSignalCallback", "Error on remove ("+id+"): "+e.getMessage());
		}
	}
	
	/**
	 * Deletes a Signal Callback
	 * @param cb	The Signal Callback
	 */
	public static void DelSignalCallback(STCallBack cb)	{
		try	{
			if(SignalCallbacks != null)	{
				SignalCallbacks.remove(cb);
				Log.i("SignalTracker::DelSignalCallback","Removing callback");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelSignalCallback", "Error on remove callback: "+e.getMessage());
		}
	}
	/**
	 * Deletes a Signal Callback by from
	 * @param cb	The Signal Callback
	 */
	public static void DelSignalCallback(String from)	{
		try	{
			if(SignalCallbacks != null)	{
				List<STCallBack> remove = new ArrayList<STCallBack>();
				for(int i=0; i<SignalCallbacks.size();i++)
					if(SignalCallbacks.get(i).from.contentEquals(from))	
							remove.add(SignalCallbacks.get(i));
				for(int i=0;i<remove.size();i++)	
					SignalCallbacks.remove(remove.get(i));
				remove = null;
				Log.i("SignalTracker::DelSignalCallback","Removing callback ("+from+")");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelSignalCallback", "Error on remove callback ("+from+"): "+e.getMessage());
		}
	}

	/**
	 * Deletes a Tower Callback with the ID
	 * @param id	The id of Tower Callback
	 */
	public static void DelTowerCallback(int id)	{
		try{
			if(TowerCallbacks != null)	{
					SignalCallbacks.remove(id);
				Log.i("SignalTracker::DelTowerCallback","Removing ("+id+")");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelTowerCallback", "Error on remove ("+id+"): "+e.getMessage());
		}
			
	}
	
	/**
	 * Deletes a Tower Callback
	 * @param cb	The Tower Callback
	 */
	public static void DelTowerCallback(STCallBack cb)	{
		try{
			if(TowerCallbacks != null)	{
				TowerCallbacks.remove(cb);
				Log.i("SignalTracker::DelTowerCallback","Removing callback");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelTowerCallback", "Erro ao remover callback: "+e.getMessage());
		}
			
	}
	
	/**
	 * Deletes a Tower Callback by from
	 * @param id	The id of Tower Callback
	 */
	public static void DelTowerCallback(String from)	{
		try{
			if(TowerCallbacks != null)	{
				List<STCallBack> remove = new ArrayList<STCallBack>();
				for(int i=0; i<TowerCallbacks.size();i++)
					if(TowerCallbacks.get(i).from.contentEquals(from))	
							remove.add(TowerCallbacks.get(i));
				for(int i=0;i<remove.size();i++)	
					TowerCallbacks.remove(remove.get(i));
				remove = null;
				Log.i("SignalTracker::DelTowerCallback","Removing ("+from+")");
			}
		}catch(Exception e)	{
			Log.e("SignalTracker::DelTowerCallback", "Error on remove ("+from+"): "+e.getMessage());
		}
			
	}
	
	@SuppressLint("NewApi")
	/**
	 * Resends the Tower and Signal data from memory
	 */
	public static void DoResend()	{
		if( (WifiSend & WifiConnected) | !WifiSend)	{
			int count = 0, rawcount = 0;
			for(int i=0;i<Signals.size();i++)	{
				SignalObject sig = Signals.get(i);
				if(sig == null)
					Log.i("ST","NULL ERROR");
				if(sig.state == 0)	{
					//	new HSAPI.SendSignal().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sig);
					//} else {
						new HSAPI.SendSignal().execute(sig);
					//}
					count++;
					rawcount++;
				}else if (Signals.get(i).state == 1)
					count++;
				else if (Signals.get(i).state == 2 & CommonHandler.dbman != null)
					CommonHandler.dbman.UpdateSignal(Signals.get(i).latitude, Signals.get(i).longitude, Signals.get(i).signal, (short) 2);
					
				if(count == 10)
					break;
			}
			if(rawcount > 0)
				Log.i("SignalTracker::DoResend","Reseding "+rawcount+" signals. ("+count+")");
			
			count = 0;
		}
	}
	@SuppressLint("NewApi")
	/**
	 * Adds a Signal to the database, and start the task to send
	 * it, if not in OnlyWireless Mode. This can execute the 
	 * callbacks in signal callback list.
	 * @param lat	Latitude of Signal
	 * @param lon	Longitude of Signal
	 * @param signal	The Strength of Signal
	 * @param weight	The Weight of Signal
	 * @param doCallback	If Signal Callbacks will be executed
	 */
	public static void AddSignal(double lat, double lon, short signal, float weight, boolean doCallback)	{
		if(Signals != null)	{
			SignalObject tmp	=	new SignalObject(lat,lon,signal,(short)0,weight,MCC,MNC);
			boolean add = true;
			for(int i=0; i<Signals.size();i++)	{
				if(tmp.distance(Signals.get(i)) < MinimumDistance-(MinimumDistance/5f))	{
					add = false;
					Signals.get(i).signal = (short) ((Signals.get(i).signal + signal) / 2);
					break;
				}
			}
			if(ServiceMode < 3)	{
				if((WifiSend & WifiConnected) | !WifiSend)	{
					//if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
					//	new HSAPI.SendSignal().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tmp);
					//} else {
						new HSAPI.SendSignal().execute(tmp);
					//}
				}
			}
			if(add)	{
				SentSignals += MinimumDistance;
				dbman.setPreference("sentsignals", Double.toString(SentSignals));
				Signals.add(tmp);
				dbman.insertSignal(lat, lon, signal);
				Log.i("SignalTracker::AddSignal",tmp.toString());
				if(SignalCallbacks != null && doCallback)	{
					boolean[]	removeItens = new boolean[SignalCallbacks.size()];
					for(int i=0;i<SignalCallbacks.size();i++)	{
						try{
							SignalCallbacks.get(i).Call(tmp);
							removeItens[i] = false;
						}catch(Exception e)	{
							Log.i("SignalTracker::AddSignal","Error processing callback("+i+"): "+e.getMessage());
							removeItens[i] = true;
						}
					}
					for(int i=removeItens.length-1;i>=0;i--)	
						if(removeItens[i] == true)
							DelSignalCallback(i);
				}
			}
		}else
			Log.e("SignalTracker::AddSignal","The Signal List is null!");
	}
	
	/**
	 * Initializes the database
	 * @param ctx	The context that will initialize the database
	 */
	public static void InitDB(Context ctx)	{
		if(CommonHandler.dbman != null)	{
			if(!CommonHandler.dbman.isOpen())
				CommonHandler.dbman.Open(ctx);
		}else{
			CommonHandler.dbman = new DatabaseManager(ctx);
		}
	}
	
	/**
	 * 	Loads the preferences from database
	 */
	public static void LoadPreferences()	{
		/*	Carregar Preferências	*/
		if(dbman != null)	{
			
			String fbid				=	dbman.getPreference("fbid");
			String fbname			=	dbman.getPreference("fbname");	
			String fbemail			=	dbman.getPreference("fbemail");	
			String configured		=	dbman.getPreference("configured");
			String servicemode		=	dbman.getPreference("servicemode");
			String mindistance		=	dbman.getPreference("mindistance");
			String mintime			=	dbman.getPreference("mintime");
			String wakelock			=	dbman.getPreference("wakelock");
			String lightmodet		=	dbman.getPreference("lightmodedelay");
			String wifisend			=	dbman.getPreference("wifisend");
			String senttower		=	dbman.getPreference("senttowers");
			String sentsignal		=	dbman.getPreference("sentsignals");
			OperatorList 			=	dbman.getOperatorList();
			
			if(fbid != null)
				FacebookUID			=	fbid;
			if(fbname != null)
				FacebookName		=	fbname;
			if(fbemail != null)
				FacebookEmail		=	fbemail;
			if(configured != null)
				Configured			=	(configured.contains("True")?true:false);
			if(servicemode != null)
				ServiceMode			=	Short.parseShort(servicemode);
			if(mindistance != null)
				MinimumDistance		=	Integer.parseInt(mindistance);
			else
				dbman.setPreference("mindistance", Integer.toString(MinimumDistance));
			if(mintime != null)
				MinimumTime			=	Integer.parseInt(mintime);
			else
				dbman.setPreference("mintime", Integer.toString(MinimumTime));
			if(wakelock != null)	
				WakeLock			=	(wakelock.contains("True")?true:false);
			if(lightmodet != null)
				LightModeDelayTime	=	Integer.parseInt(lightmodet);
			if(wifisend != null)
				WifiSend			=	(wifisend.contains("True")?true:false);
			if(senttower != null)
				SentTowers			=	Integer.parseInt(senttower);
			if(sentsignal != null)
				SentSignals			=	Double.parseDouble(sentsignal);
			
			PreferencesLoaded = true;
			Log.i("SignalTracker::LoadPreferences", "Preferences loaded.");
		}else
			Log.e("SignalTracker::LoadPreferences", "Database not initialized!");
	}
}
