package com.ignite.utils.security;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ignite.utils.ApplicationContextProvider;
import com.ignite.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class SessionManager {

    SharedPreferences preferences;
    Editor editor;
    Context context;
    Map<String, String> cookiesMap;
     
    // Shared preferences mode
    private final int PRIVATE_MODE = 0;
    private final int MODE_WORLD_READABLE = 2;
     
    // Sharedpref file name
    private final String PREFERENCES_NAME = "session";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "isLogin";
    private static final String HAS_ROLE = "hasRole";

    // Shared Preferences Keys for Activity states
    private static final String ACTIVITY_HOME_SELECTION = "activityHomeSelection";
     
    // User name (make variable public to access from outside)
    public static final String KEY_IDENTIFIER = "identifier";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    // Cookie for other params
    public static final String KEY_COOKIE = "cookie";

    public static SessionManager getSession() {
        return new SessionManager(ApplicationContextProvider.getContext());
    }

    public static void createSession(String username, String password, int role){
        SessionManager session = getSession();
        session.setLoggedIn(true);
        session.setRole(role);
        session.setUsername(username);
        session.setPassword(password);
    }

    public static void removeSession(){
        SessionManager session = getSession();
        session.clear();
        session.setLoggedIn(false);
        session.setIdentifier((long)-1);
        session.setRole(0);
        session.setUsername(null);
        session.setPassword(null);
        session.setHomeActivitySelection(0);
    }

    // Constructor
    public SessionManager(Context context){
        this.context = context;
        try {
        	preferences = this.context.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE);
     	    editor = preferences.edit();
            cookiesMap = getCookies();
        } catch (NullPointerException e) {
        	e.printStackTrace();

			if (this.context != null) {
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.runFinalization();
						System.exit(0);
					}
				};
				
				/*AlertDialogManager alerta = new AlertDialogManager();
				alerta.configureDialogButtons("OK", "", listener, null);
				alerta.showAlertDialog(this.context, this.context.getString(R.string.app_name), this.context.getString(R.string.error_escritura), false);*/
			}
        }
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    /* Hay que llamar a setCookies despues */
    public void putCookie(String key, String value) {
        cookiesMap.put(key, value);
    }

    public String getCookie(String key) {
        return getCookie(key, null);
    }

    public String getCookie(String key, String defaultValue) {
        String value = cookiesMap.get(key);
        return value == null ? defaultValue : value;
    }

    public String removeCookie(String key) {
        return cookiesMap.remove(key);
    }

    public void setCookies() {
        if (cookiesMap != null) {
            String cookies = "";
            for (String cookie : cookiesMap.keySet()) {
                String value = cookiesMap.get(cookie);
                if (value != null && value.length() > 0) {
                    cookies += cookie + "=" + cookiesMap.get(cookie) + ":";
                }
            }
            Constants.Console.Log(cookies);
            editor.putString(KEY_COOKIE, cookies);
            editor.commit();
        }
    }

    public Map<String, String> getCookies() {
        if (cookiesMap == null) {
            cookiesMap = new HashMap<>();
            String cookies = preferences.getString(KEY_COOKIE, "");
            Constants.Console.Log(cookies);
            for (String cookie : cookies.split(":")) {
                if (cookie.contains("=")) {
                    String[] cookieEntry = cookie.split("=");
                    cookiesMap.put(cookieEntry[0], cookieEntry[1]);
                }
            }
        }
        return cookiesMap;
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_USERNAME, getUsername());
        user.put(KEY_PASSWORD, getPassword());
        user.put(HAS_ROLE, "" + getRole());
        return user;
    }
     
    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn() {
        return preferences.getBoolean(IS_LOGIN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(IS_LOGIN, loggedIn);
        editor.commit();
    }

    public int getRole() {
        return preferences.getInt(HAS_ROLE, -1);
    }

    public void setRole(int role) {
        editor.putInt(HAS_ROLE, role);
        editor.commit();
    }

    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.commit();
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "Anonymous");
    }

    public void setPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String getPassword() {
        return preferences.getString(KEY_PASSWORD, "");
    }

    public void setIdentifier(Long identifier) {
        editor.putLong(KEY_IDENTIFIER, identifier);
        editor.commit();
    }

    public Long getIdentifier() {
        return preferences.getLong(KEY_IDENTIFIER, -1);
    }

    public void setHomeActivitySelection(int selection) {
        editor.putInt(ACTIVITY_HOME_SELECTION, selection);
        editor.commit();
    }

    public int getHomeActivitySelection() {
        return preferences.getInt(ACTIVITY_HOME_SELECTION, 0/*HomeActivity.HOME_INDEX*/);
    }
}
