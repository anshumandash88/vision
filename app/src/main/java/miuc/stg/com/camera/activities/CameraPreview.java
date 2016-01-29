package miuc.stg.com.camera.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import miuc.stg.com.camera.helpers.Helper;

/**
 * Created by Anshuman on 29-11-2015.
 */
@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Activity mActivity;

    public CameraPreview(Context context, Camera camera){
        super(context);
        mActivity = (Activity)context;
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        //Deprecated settings but required for Android < 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    public void refreshCamera(Camera camera){
        if(mHolder.getSurface() == null){
            //Preview Surface doesn't exist
            return;
        }

        //Stop Preview before making changes
        try{
            mCamera.stopPreview();
        }
        catch (Exception e)
        {
            //Ignore: Tried to stop a non-existent preview
        }

        //Set preview Size and make any resize, rotate or
        // reformatting changes here

        //start preview with new settings
        setCamera(camera);

        try{

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);

            /*Change orientation of Surface view to Portrait*/
            parameters.set("orientation", "portrait");
            if (Build.VERSION.SDK_INT >= 8)
                mCamera.setDisplayOrientation(90);

            /*Auto Focus camera, so you need not do it manually*/
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            /*Change size of saved picture, because by default the size and quality was crap*/
            List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
            int chosenSize = Helper.getPictureSizeIndexForHeight(sizeList, 800);
            parameters.setPictureSize(sizeList.get(chosenSize).width, sizeList.get(chosenSize).height);

            /*Set those fucking parameters*/
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e){
            Log.d(VIEW_LOG_TAG, "Error Starting Camera Preview: " + e.getMessage());
        }
    }

    public void setCamera(Camera camera){
        //Method to set a camera instance
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            //Create the surface and start camera
            if(mCamera == null){
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        }
        catch(IOException e){
            Log.d(VIEW_LOG_TAG, "Error Setting Camera Preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //If the preview can change/rotate then take care of that here
        //Make sure to stop the preview before resize
        refreshCamera(mCamera);


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
