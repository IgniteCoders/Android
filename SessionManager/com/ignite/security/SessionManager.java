package com.ignite.networker.security;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ignite.networker.utils.ApplicationContextProvider;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences preferences;
    Editor editor;
    Context context;
     
    // Shared preferences mode
    private final int PRIVATE_MODE = 0;
    private final int MODE_WORLD_READABLE = 2;
     
    // Sharedpref file name
    private final String PREFERENCES_NAME = "session";
     
    // All Shared Preferences Keys
    private static final String IS_LOGIN = "isLogin";
    private static final String HAS_ROLE = "hasRole";
     
    // User name (make variable public to access from outside)
    public static final String KEY_IDENTIFIER = "idetifier";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    // Parámetros de GCM (GoogleCloudMessaging)
    public static final String GCM_REGISTRATION_ID = "gcm_registration_id";
    public static final String GCM_APP_VERSION = "gcm_app_version";
    public static final String GCM_EXPIRATION_TIME = "onServerExpirationTimeMs";
    public static final String GCM_USER = "gcm_user";

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
        getSession().clear();
    }

    // Constructor
    public SessionManager(Context context){
        this.context = context;
        try {
        	preferences = this.context.getSharedPreferences(PREFERENCES_NAME, PRIVATE_MODE);
     	    editor = preferences.edit();
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

    public void setGCMRegistrationId(String regId) {
        editor.putString(GCM_REGISTRATION_ID, regId);
        editor.commit();
    }

    public String getGCMRegistrationId() {
        return preferences.getString(GCM_REGISTRATION_ID, "");
    }

    public void setGCMAppVersion(int appVersion) {
        editor.putInt(GCM_APP_VERSION, appVersion);
        editor.commit();
    }

    public int getGCMAppVersion() {
        return preferences.getInt(GCM_APP_VERSION, Integer.MIN_VALUE);
    }

    public void setGCMExpirationTime(Long expirationTime) {
        editor.putLong(GCM_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }

    public Long getGCMExpirationTime() {
        return preferences.getLong(GCM_EXPIRATION_TIME, -1);
    }

    public void setGCMUser(String user) {
        editor.putString(GCM_USER, user);
        editor.commit();
    }

    public String getGCMUser() {
        return preferences.getString(GCM_USER, "");
    }
}
