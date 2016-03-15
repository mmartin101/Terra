package bfergus.Terra_Wallpapers.Main;

import bfergus.Terra_Wallpapers.SubReddit;
import retrofit.Call;
import retrofit.Retrofit;


public interface MainPresenterInterface {
     void retrieveImage(SubReddit subReddit);

     Call selectSubreddit(SubReddit subReddit, Retrofit retrofit);

        void addImageToGallery();

     void onResume();

     void onDestroy();

    void startWallpaperLoader();
}
