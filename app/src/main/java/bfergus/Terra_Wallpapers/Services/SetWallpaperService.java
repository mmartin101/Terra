package bfergus.Terra_Wallpapers.Services;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;


import bfergus.Terra_Wallpapers.Model.Reddit_API_Model;
import bfergus.Terra_Wallpapers.Reddit_API_Interface;
import bfergus.Terra_Wallpapers.SubReddit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SetWallpaperService extends IntentService {

    private final static String baseUrl = "https://reddit.com";

    Reddit_API_Model newsData;

    public SetWallpaperService() {
        super("SetWallpaperService()");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        retrieveImage(SubReddit.Earth);
    }

    private Point getDeviceSize() {
        Point size = new Point();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getSize(size);
        return size;
    }

    public void displayImage(final int position) {
        Point size = getDeviceSize();
        Glide.with(getApplicationContext())
                .load(newsData.data.children[position].data.url)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        displayImage(position + 1);
                        //todo: make sure we don't run into a index out of bounds.
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .centerCrop()
                .into(new SimpleTarget<Bitmap>(size.x, size.y) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        setBitmapToWallpaper(resource);

                    }
                });
    }

    public void setBitmapToWallpaper(final Bitmap bitmap) {

        WallpaperManager mWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            mWallpaperManager.setBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void retrieveImage(SubReddit subReddit) {
        Retrofit retrofit = createRetrofit();
        Call<Reddit_API_Model> mCall = selectSubreddit(subReddit, retrofit);
        mCall.enqueue(new Callback<Reddit_API_Model>() {
            @Override
            public void onResponse(Call<Reddit_API_Model> call, Response<Reddit_API_Model> response) {
                newsData = response.body();
                displayImage(0);
            }

            @Override
            public void onFailure(Call<Reddit_API_Model> call, Throwable t) {
            }
        });
    }

    public Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createCache())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private OkHttpClient createCache() {
        Cache cache = new Cache(getApplicationContext().getCacheDir(), 1024 * 1024 * 10);
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.cache(cache);
        return clientBuilder.build();
    }

    public Call<Reddit_API_Model> selectSubreddit(SubReddit subReddit, Retrofit retrofit) {
        Reddit_API_Interface redditAPI = retrofit.create(Reddit_API_Interface.class);
        Call<Reddit_API_Model> mCall;
        switch (subReddit) {
            case Earth:
                mCall = redditAPI.getEarth();
                break;
            default:
                mCall = null;
                break;
        }
        return mCall;
    }
}
