package bfergus.Terra_Wallpapers.Main;



import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;

import java.util.Date;

import bfergus.Terra_Wallpapers.Model.Reddit_API_Model;
import bfergus.Terra_Wallpapers.R;
import bfergus.Terra_Wallpapers.Reddit_API_Interface;
import bfergus.Terra_Wallpapers.SubReddit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainPresenter implements MainPresenterInterface {
    private MainView view;

    private final static String baseUrl = "https://reddit.com";

    Reddit_API_Model newsData;

    Bitmap wallPaperBitmap;

    Context appContext;


    public MainPresenter(MainView view, Context context) {
        this.view = view;
        this.appContext = context;
    }

    public void onResume() {
        if(wallPaperBitmap != null) {
            view.setImageViewPicture(wallPaperBitmap);
            view.showButtons();
        }
        else {
            retrieveImage(SubReddit.Earth);}
    }

    public void onDestroy() {
        view = null;
    }





    public void retrieveImage(SubReddit subReddit) {
        view.showProgressViews(appContext.getString(R.string.fetching_images));
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
                view.displayAlertDialog(appContext.getString(R.string.alert_dialog_title),
                        appContext.getString(R.string.alert_dialog_message),
                        appContext.getString(R.string.alert_dialog_positive_bt),
                        appContext.getString(R.string.alert_dialog_negative_bt));
            }
        });
    }

    public void displayImage(final int position) {
        Glide.with(appContext)
                .load(newsData.data.children[position].data.url)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        if(position == 24) view.showMessage(appContext.getString(R.string.Out_Of_Images));
                        else displayImage(position + 1);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .centerCrop()
                .into(new BitmapImageViewTarget(view.getImageView()) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        super.setResource(resource);
                        view.setImageBitmap(resource);
                        wallPaperBitmap = resource;

                        /***************
                         * This is needed because for some reason, Glide freezes the dummy imageview
                         * returned in .into to a .error screen. However, it still returns the correct
                         * bitmap, so I just passed that into an actual imageview.
                         */
                        setActualImageView();
                        showButtons();
                    }
                });
    }
    public void showButtons() {
        view.removeProgressViews();
        view.showButtons();
    }

    public void setActualImageView() {
        view.setImageViewPicture(wallPaperBitmap);
    }

    public  void addImageToGallery(){
        ContentResolver cr = appContext.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, getFileNameForTodaysImage());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, getFileNameForTodaysImage());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Terra Wallpaper");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (wallPaperBitmap != null) {
                OutputStream imageOut;
                imageOut = cr.openOutputStream(url);
                try {
                    wallPaperBitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                } finally {
                    imageOut.close();
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id, 50F, 50F, MediaStore.Images.Thumbnails.MICRO_KIND);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
            view.showMessage(appContext.getString(R.string.Wallpaper_Saved));
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
            }
            view.showMessage(appContext.getString(R.string.Wallpaper_save_error));
        }
    }

    private static  Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND,kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH,thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
            return thumb;
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }


    public String getFileNameForTodaysImage() {
        DateFormat df = DateFormat.getDateTimeInstance();
        String timeStamp = df.format(new Date().getTime());
        return "Terra_Wallpaper_" + timeStamp;
    }

    private Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createCache())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private OkHttpClient createCache() {
        Cache cache = new Cache(appContext.getCacheDir(), 1024 * 1024 * 10);
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

    public void startWallpaperLoader() {
        view.startWallpaperLoader(wallPaperBitmap);
    }
}
