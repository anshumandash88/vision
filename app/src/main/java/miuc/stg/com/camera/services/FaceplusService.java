package miuc.stg.com.camera.services;

import android.content.Context;

import java.lang.ref.WeakReference;

import miuc.stg.com.camera.Constants;
import miuc.stg.com.camera.faceplusmodel.FacePlusResponse;
import miuc.stg.com.camera.faceplusmodel.faceplusAPI;
import miuc.stg.com.camera.helpers.NotificationHelper;
import miuc.stg.com.camera.utils.NetworkUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Anshuman on 27-01-2016.
 */
public class FaceplusService {
    //public final static String TAG = UploadService.class.getSimpleName();

    private WeakReference<Context> mContext;

    public FaceplusService(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public void Execute(String url, Callback<FacePlusResponse> callback) {
        final Callback<FacePlusResponse> cb = callback;

        if (!NetworkUtils.isConnected(mContext.get())) {
            //Callback will be called, so we prevent a unnecessary notification
            cb.failure(null);
            return;
        }

        RestAdapter restAdapter = buildRestAdapter();

        try {
            restAdapter.create(faceplusAPI.class).detectFace(
                    "SoCinbYTMCmshD5Ez14oiSeDpi49p1LhWOQjsnWwlnVIjlpNaC",
                    "application/json",
                    "glass,gender,age,smiling",
                    url,
                    new Callback<FacePlusResponse>() {
                        @Override
                        public void success(FacePlusResponse facePlusResponse, Response response) {
                            if (cb != null)
                            {
                                cb.success(facePlusResponse, response);
                            }
                            if (response == null) {
                            /*
                             Notify image was NOT uploaded successfully
                            */
                                //notificationHelper.createFailedUploadNotification();
                                return;
                            }
                        /*
                        Notify image was uploaded successfully
                        */
                            else {
                                //notificationHelper.createUploadedNotification(facePlusResponse);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (cb != null) cb.failure(error);
                            //notificationHelper.createFailedUploadNotification();
                        }
                    });
        }
        catch (Exception ex)
        {
            String str = ex.getMessage();
        }
    }

    private RestAdapter buildRestAdapter() {
        RestAdapter faceplusAdapter = new RestAdapter.Builder()
                .setEndpoint("https://faceplusplus-faceplusplus.p.mashape.com")
                .build();

        /*
        Set rest adapter logging if we're already logging
        */
        if (Constants.LOGGING)
            faceplusAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return faceplusAdapter;
    }

}
