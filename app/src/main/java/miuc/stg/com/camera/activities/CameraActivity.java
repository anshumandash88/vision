package miuc.stg.com.camera.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import miuc.stg.com.camera.R;

public class CameraActivity extends AppCompatActivity {

    //Global Variables
    ImageView mImageView;
    Button btnCapture;
    private android.hardware.Camera mCamera;
    private CameraPreview mPreview;
    private android.hardware.Camera.PictureCallback mPicture;
    private Button capture, switchCamera;
    private Context myContext;
    private LinearLayout cameraPreview;
    private boolean cameraFront = false;

    private TextToSpeech tts;
    private Vibrator mVibrator;
    int dot = 200;      // Length of a Morse Code "dot" in milliseconds
    int dash = 500;     // Length of a Morse Code "dash" in milliseconds
    int short_gap = 200;    // Length of Gap Between dots/dashes
    int medium_gap = 500;   // Length of Gap Between Letters
    int long_gap = 1000;    // Length of Gap Between Words
    long[] pattern = {
            0,  // Start immediately
            dash, short_gap, dot
    };
    long[] pPictureTaken = {
            0,
            dot,dot
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        myContext = this;
        initialize();

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        //Get instance of Vibrate Service
        mVibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVibrator.vibrate(pattern,-1);
                tts.speak("Taking picture. Please hold still.", TextToSpeech.QUEUE_FLUSH, null);
            }
        }, 2000);

        //Code to click picture automatically after 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPicture = getPictureCallback();
                mCamera.takePicture(null, null, mPicture);
            }
        }, 7000);
    }

    //Camera Functions Start

    public void initialize() {

        cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);
        //mPicture = getPictureCallback();
        mPreview = new CameraPreview(myContext, mCamera);
        cameraPreview.addView(mPreview);
    }


    View.OnClickListener switchCameraListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onClick(View v) {
            //get the number of cameras
            int camerasNumber = android.hardware.Camera.getNumberOfCameras();
            if (camerasNumber > 1) {
                //release the old camera instance
                //switch camera, from the front and the back and vice versa

                releaseCamera();
                chooseCamera();
            } else {
                Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int findFrontFacingCamera()
    {
        int cameraId = -1;
        //Search for the front facing camera
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        android.hardware.Camera.CameraInfo info = null;
        for(int i=0; i< numberOfCameras; i++)
        {
            info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info);
            if(info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private int findBackFacingCamera(){
        int cameraId = -1;
        //Search for back facing camera
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        android.hardware.Camera.CameraInfo info = null;
        for(int i=0; i<numberOfCameras; i++){
            info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info);
            if(info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void chooseCamera(){
        int cameraId = -1;
        //If camera preview is the front
        if(cameraFront)
        {
            cameraId = findBackFacingCamera();
            if(cameraId >= 0)
            {
                //Open back camera
                //set picture callback
                //refresh preview

                mCamera = android.hardware.Camera.open(cameraId);
                mPicture = getPictureCallback();
                mPreview.refreshCamera(mCamera);
            }
        }
        else
        {
            cameraId = findFrontFacingCamera();

            mCamera = android.hardware.Camera.open(cameraId);
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }

    private boolean hasCamera(Context context){
        //Check if device has camera
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;
        return false;
    }

    private android.hardware.Camera.PictureCallback getPictureCallback(){
        android.hardware.Camera.PictureCallback picture = new android.hardware.Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {

                Bitmap bm = null;

                if (data != null) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // Notice that width and height are reversed
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenHeight, screenWidth, true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        // Setting post rotate to 90
                        Matrix mtx = new Matrix();
                        mtx.postRotate(90);
                        // Rotating Bitmap
                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    }else{// LANDSCAPE MODE
                        //No need to reverse width and height
                        Bitmap scaled = Bitmap.createScaledBitmap(bm, screenWidth,screenHeight , true);
                        bm=scaled;
                    }
                }
                //Make new Picture File
                File pictureFile = getOutputMediaFile();

                if(pictureFile == null)
                    return;
                try
                {
                    //Write the file
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    bm.compress(Bitmap.CompressFormat.PNG,90,fos);
                    //fos.write(data);
                    fos.close();

                    //Send the url of the image back to main activity for post processing
                    Intent send = new Intent();
                    send.putExtra("url", pictureFile.getAbsolutePath());
                    setResult(RESULT_OK, send);

                    //Vibration Feedback
                    mVibrator.vibrate(pattern,-1);

                    //Audio Feedback
                    tts.speak("Picture taken. Processing Now.", TextToSpeech.QUEUE_FLUSH, null);

                    //Toast to show Success. COMMENT IN RELEASE
                    Toast toast = Toast.makeText(myContext, "Picture Saved: " + pictureFile.getName(), Toast.LENGTH_LONG);
                    toast.show();
                }
                catch(FileNotFoundException e){

                }catch(IOException e){

                }

                //Refresh camera to continue preview
                mPreview.refreshCamera(mCamera);
                finish();
            }
        };
        return picture;
    }

    //Make picture and save to a folder
    private static File getOutputMediaFile(){
        //Make a new file directory inside the Pictures folder
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "Vision");//new File("/","");//

        //If the folder doesnt exist
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs())
                return null;
        }

        //Take current Timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator+ "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void releaseCamera()
    {
        if(mCamera != null)
        {
            mCamera.release();
            mCamera = null;
        }
    }

    //Camera Functions End


    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void onResume(){
        super.onResume();
        if(!hasCamera(myContext)){
            Toast toast = Toast.makeText(myContext, "No Camera Detected!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if(mCamera == null)
        {
            //if the front facing camera doesn't exist
            //Release old camera
            //switch camera, from front to back
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                switchCamera.setVisibility(View.GONE);
            }
            mCamera = android.hardware.Camera.open(findBackFacingCamera());
            mPicture = getPictureCallback();
            mPreview.refreshCamera(mCamera);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        //When Paused, release camera in order to be used from other apps
        releaseCamera();
    }


}
