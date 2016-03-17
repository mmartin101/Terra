package bfergus.Terra_Wallpapers;

import bfergus.Terra_Wallpapers.Model.Reddit_API_Model;
import retrofit2.Call;
import retrofit2.http.GET;


public interface Reddit_API_Interface {
    @GET("/r/earthporn/top/.json?sort=top&t=day")
    Call<Reddit_API_Model> getEarth();
}
