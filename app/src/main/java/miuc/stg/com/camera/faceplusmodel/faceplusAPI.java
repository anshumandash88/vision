package miuc.stg.com.camera.faceplusmodel;

import miuc.stg.com.camera.imgurmodel.ImageResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Created by Anshuman on 27-01-2016.
 */
public interface faceplusAPI {

    @GET("/detection/detect")
    void detectFace(
            @Header("X-Mashape-Key") String auth,
            @Header("Accept") String accept,
            @Query("attribute") String attribute,
            @Query("url") String url,
            Callback<FacePlusResponse> cb
    );
}
