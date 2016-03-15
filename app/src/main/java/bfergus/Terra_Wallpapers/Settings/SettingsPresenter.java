package bfergus.Terra_Wallpapers.Settings;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import bfergus.Terra_Wallpapers.Services.SetWallpaperService;

public class SettingsPresenter implements SettingsPresenterInterface {

    SettingsView view;

    Context appContext;

    public static final String PREFS_NAME = "MyPrefsFile";

    boolean automaticModeEnabled = false;

    boolean alarmStatusChanged = false;

    SharedPreferences settings;

    public SettingsPresenter(SettingsView view, Context context) {
        this.view = view;
        this.appContext = context;
    }

    public void onResume() {
        settings = appContext.getSharedPreferences(PREFS_NAME,0);
        automaticModeEnabled =  settings.getBoolean("automaticMode", false);
    }

    public void onStop() {
       saveSharedPreferences();
       if(alarmStatusChanged) handleAutomaticMode();
    }
    private void handleAutomaticMode() {
        AlarmManager alarmMngr = (AlarmManager)appContext.getSystemService(appContext.ALARM_SERVICE);
        Intent intent = new Intent(appContext, SetWallpaperService.class);
        PendingIntent  alarmIntent = PendingIntent.getService(appContext, 0, intent, 0);
        alarmMngr.cancel(alarmIntent);
        if(automaticModeEnabled) alarmMngr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, AlarmManager.INTERVAL_DAY, alarmIntent);
    }


    private void saveSharedPreferences() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("automaticMode", automaticModeEnabled);
        editor.apply();
    }

    public void onDestroy() {
        view = null;
    }

    public boolean getAutomaticModeStatus() {
        return automaticModeEnabled;
    }

    public void setAutomaticModeStatus(boolean status) {
        this.automaticModeEnabled = status;
        alarmStatusChanged = (alarmStatusChanged == false) ? true : false;
    }
}
