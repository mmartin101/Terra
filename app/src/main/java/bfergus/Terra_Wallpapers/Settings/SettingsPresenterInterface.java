package bfergus.Terra_Wallpapers.Settings;

public interface SettingsPresenterInterface {

    void onResume();

    void onStop();

    boolean getAutomaticModeStatus();

    void setAutomaticModeStatus(boolean status);

    void onDestroy();
}
