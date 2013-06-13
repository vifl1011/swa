package de.shop.ui.main;

import static de.shop.util.Constants.DEVICE_NAME;
import static de.shop.util.Constants.HOST_DEFAULT;
import static de.shop.util.Constants.LOCALHOST_DEVICE;
import static de.shop.util.Constants.LOCALHOST_EMULATOR;
import static de.shop.util.Constants.MOCK_DEFAULT;
import static de.shop.util.Constants.PATH_DEFAULT;
import static de.shop.util.Constants.PORT_DEFAULT;
import static de.shop.util.Constants.PROTOCOL_DEFAULT;
import static de.shop.util.Constants.TIMEOUT_DEFAULT;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import de.shop.R;

public class Prefs extends PreferenceFragment {
	private static final String LOG_TAG = Prefs.class.getSimpleName();
	
	public static String protocol;
	public static String host;
	public static String port;
	public static String path;
	private static String timeoutStr;
	public static long timeout;
	public static String username;
	public static String password;

	public static String hostProxy;
	public static String portProxy;
	public static String usernameProxy;
	public static String passwordProxy;
	
	public static boolean mock;
	
	private static boolean initialized = false;
	
	private static final String PROTOCOL_KEY = "protocol";
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";
	private static final String PATH_KEY = "path";
	private static final String TIMEOUT_KEY = "timeout";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";
	
	private static final String HOST_PROXY_KEY = "proxy_host";
	private static final String PORT_PROXY_KEY = "proxy_port";
	private static final String USERNAME_PROXY_KEY = "proxy_username";
	private static final String PASSWORD_PROXY_KEY = "proxy_password";
	
	private static final String MOCK_KEY = "mock";

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// Evtl. vorhandene Tabs der ACTIVITY loeschen
		getActivity().getActionBar().removeAllTabs();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		init(getActivity());
		
		setProtocolListener();
		setHostListener();
		setPortListener();
		setPathListener();
		setTimeoutListener();
		setUsernameListener();
		setPasswordListener();
		setHostProxyListener();
		setPortProxyListener();
		setUsernameProxyListener();
		setPasswordProxyListener();
		setMockListener();
	}

	public static void init(Context ctx) {
		if (initialized) {
			return;
		}
		
		// Objekt der Klasse SharedPreferences laden, das die Default-Datei
		// /data/data/de.shop/shared_prefs/de.shop_preferences.xml repraesentiert
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		protocol = prefs.getString(PROTOCOL_KEY, PROTOCOL_DEFAULT);
		host = prefs.getString(HOST_KEY, HOST_DEFAULT);
		port = prefs.getString(PORT_KEY, PORT_DEFAULT);
		path = prefs.getString(PATH_KEY, PATH_DEFAULT);
		
		timeoutStr = prefs.getString(TIMEOUT_KEY, TIMEOUT_DEFAULT);
		try {
			timeout = Long.parseLong(timeoutStr);
		}
		catch (NumberFormatException e) {
			timeout = Long.parseLong(TIMEOUT_DEFAULT);
		}
		
		prefs.edit().putString(USERNAME_KEY, "noUser").commit();
		prefs.edit().putString(USERNAME_PROXY_KEY, "noUser").commit();
		
		username = prefs.getString(USERNAME_KEY, "noUser");
		password = prefs.getString(PASSWORD_KEY, "noPassword");

		hostProxy = prefs.getString(HOST_PROXY_KEY, "notSet");
		portProxy = prefs.getString(PORT_PROXY_KEY, "notSet");
		usernameProxy = prefs.getString(USERNAME_PROXY_KEY, "notSet");
		passwordProxy = prefs.getString(PASSWORD_PROXY_KEY, "notSet");

		mock = prefs.getBoolean(MOCK_KEY, MOCK_DEFAULT);
		
		initialized = true;
		
		Log.i(LOG_TAG, "protocol=" + protocol + ", host=" + host  + ", port=" + port + ", path=" + path
				       + ", timeoutStr=" + timeoutStr + ", username=" + username + ", password=" + password
				       + ", hostProxy=" + hostProxy + ", portProxy=" + portProxy
				       + ", usernameProxy=" + usernameProxy + ", passwordProxy=" + passwordProxy
				       + ", mock=" + mock);
	}
	
	private void setProtocolListener() {
		findPreference(PROTOCOL_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				protocol = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PROTOCOL_KEY, protocol)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setHostListener() {
		findPreference(HOST_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				host = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(HOST_KEY, host)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setPortListener() {
		findPreference(PORT_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				port = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PORT_KEY, port)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setPathListener() {
		findPreference(PATH_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				path = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PATH_KEY, path)
				                 .commit();
				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setTimeoutListener() {
		findPreference(TIMEOUT_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				timeoutStr = (String) newValue;
				try {
					timeout = Long.parseLong(timeoutStr);
				}
				catch (NumberFormatException e) {
					timeout = Long.parseLong(TIMEOUT_DEFAULT);
				}
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(TIMEOUT_KEY, timeoutStr)
				                 .commit();
				
				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setUsernameListener() {
		findPreference(USERNAME_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				username = (String) newValue;
				Log.v(LOG_TAG, "Username:"+username);
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(USERNAME_KEY, username)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setPasswordListener() {
		findPreference(PASSWORD_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				password = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PASSWORD_KEY, password)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setHostProxyListener() {
		findPreference(HOST_PROXY_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				hostProxy = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(HOST_PROXY_KEY, hostProxy)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setPortProxyListener() {
		findPreference(PORT_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				portProxy = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PORT_PROXY_KEY, portProxy)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setUsernameProxyListener() {
		if (findPreference(USERNAME_PROXY_KEY) == null) 
			Log.v(LOG_TAG, "lol");
		findPreference(USERNAME_PROXY_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				usernameProxy = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(USERNAME_PROXY_KEY, usernameProxy)
				                 .commit();
					// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
				                          .replace(R.id.details, new Prefs())
				                          .addToBackStack(null)
				                          .commit();				
				return false;
			}
		});
	}
	
	private void setPasswordProxyListener() {
		findPreference(PASSWORD_PROXY_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				passwordProxy = (String) newValue;
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putString(PASSWORD_PROXY_KEY, passwordProxy)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
	
	private void setMockListener() {
		findPreference(MOCK_KEY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				mock = ((Boolean) newValue).booleanValue();
				PreferenceManager.getDefaultSharedPreferences(getActivity())
				                 .edit()
				                 .putBoolean(MOCK_KEY, mock)
				                 .commit();

				// Fragment neu laden, damit die Aenderung sofort sichtbar ist
				getFragmentManager().beginTransaction()
		                            .replace(R.id.details, new Prefs())
		                            .addToBackStack(null)
		                            .commit();				
				return false;
			}
		});
	}
}
