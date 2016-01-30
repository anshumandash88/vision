package miuc.stg.com.camera.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Locale;

import miuc.stg.com.camera.GetRequest;
import miuc.stg.com.camera.R;
import miuc.stg.com.camera.RetrieveInformation;
import miuc.stg.com.camera.faceplusmodel.FacePlusResponse;
import miuc.stg.com.camera.imgurmodel.ImageResponse;
import miuc.stg.com.camera.imgurmodel.Upload;
import miuc.stg.com.camera.services.FaceplusService;
import miuc.stg.com.camera.services.UploadService;
import retrofit.Callback;
import retrofit.RetrofitError;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener  {

    private GestureDetectorCompat gDetector;
    private GestureDetector gestureDetector;
    private Vibrator mVibrator;
    private TextToSpeech tts;
    private int CAM_ACTIVITY =  1;

    String imgUrl;
    boolean detectFace = true;

    //imgur stuff
    private Upload upload; // Upload object containging image and meta data
    private File chosenFile; //chosen file from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.gDetector = new GestureDetectorCompat(this,this);
        gDetector.setOnDoubleTapListener(this);

        gestureDetector = new GestureDetector(
                new SwipeGestureDetector());

        //Get instance of Vibrate Service
        mVibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.ENGLISH);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mVibrator.vibrate(700);
                tts.speak("Ready", TextToSpeech.QUEUE_FLUSH, null);
            }
        }, 1200);

    }

    public void sendMessage() {
        Intent intent = new Intent(this, CameraActivity.class);
        //intent.putExtra("requestCode", CAM_ACTIVITY);
        startActivityForResult(intent, CAM_ACTIVITY);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CAM_ACTIVITY) {
            if (data.hasExtra("url")) {
                /*Toast.makeText(this, data.getExtras().getString("url"),
                        Toast.LENGTH_SHORT).show();*/

                /*Upload the Picture to imgur.com*/
                Uri uri = Uri.parse(data.getExtras().getString("url"));

                try {
                    String filePath = uri.toString();
                    if (filePath == null || filePath.isEmpty()) return;

                    chosenFile = new File(filePath);
                    uploadImage();

                } catch (Exception e) {
                    Toast.makeText(this,
                            "Unable to get the file from the given URI.  See error log for details",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //imgur stuff
    public void uploadImage() {
    /*
      Create the @Upload object
     */
        if (chosenFile == null) return;
        createUpload(chosenFile);

    /*
      Start upload
     */
        new UploadService(this).Execute(upload, new ImgurCallback());
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
        upload.title = "vision-test-title";
        upload.description = "vision-test-desc";
    }

    private class ImgurCallback implements Callback<ImageResponse> {
        @Override
        public void success(final ImageResponse imageResponse, retrofit.client.Response response) {
            //Snackbar.make(findViewById(R.id.rootView),imageResponse.data.link.toString(), Snackbar.LENGTH_LONG).show();

            //Set teh global variable
            imgUrl = imageResponse.data.link.toString();
            //Call method to detect fa//Uri.parse()ce
            if(detectFace)
                detectFace(imgUrl);
            else
                detectObject(imgUrl);
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Snackbar.make(findViewById(R.id.rootView), "No internet connection", Snackbar.LENGTH_SHORT).show();            }
        }
    }

    private void detectObject(final String imgUrl){
        //TODO::Call method to detect object
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {

                GetRequest getRequest = new GetRequest();
                RetrieveInformation retrieve = new RetrieveInformation();

                String html = null;
                String tag  = null;

                try {
                    //Your code goes here
                    html = getRequest.sendGet(imgUrl);
                    String url = retrieve.getLink(html);
                    html = getRequest.getPageContent("http://www.google.com" + url);
                    tag = retrieve.getTag(html);
                    mVibrator.vibrate(500);
                    if(tag.length() != 0)
                        tts.speak("Object Detected  " + tag, TextToSpeech.QUEUE_FLUSH, null);
                    else
                        tts.speak("Sorry! No Objects Detected  " + tag, TextToSpeech.QUEUE_FLUSH, null);
                    //System.out.println(tag);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void detectFace(String imgUrl) {
        new FaceplusService(this).Execute(imgUrl, new FacePlusCallback());
    }

    private class FacePlusCallback implements Callback<FacePlusResponse> {
        @Override
        public void success(FacePlusResponse faceplusResponse, retrofit.client.Response response) {

            List<FacePlusResponse.Face> faces = faceplusResponse.face;
            if(faces == null || faces.size() == 0) {
                tts.speak("Sorry!  No Face Detected!", TextToSpeech.QUEUE_FLUSH, null);
            }
            else{
                //Snackbar.make(findViewById(R.id.rootView),faceplusResponse.face.get(0).attribute.gender.value, Snackbar.LENGTH_LONG).show();
                mVibrator.vibrate(500);
                tts.speak(faceplusResponse.toString(), TextToSpeech.QUEUE_FLUSH, null);
            }

        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Snackbar.make(findViewById(R.id.rootView), "No internet connection", Snackbar.LENGTH_SHORT).show();            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.gDetector.onTouchEvent(event);

        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }

        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    private void onLeftSwipe() {
        // Do something
        detectFace = false;
        sendMessage();
    }

    private void onRightSwipe() {
        // Do something
        detectFace = true;
        sendMessage();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        sendMessage();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onLongPress(MotionEvent e) {

        mVibrator.vibrate(700);
        tts.speak("May the force, be with you!", TextToSpeech.QUEUE_FLUSH, null);
        this.finishAffinity();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    // Private class for gestures
    private class SwipeGestureDetector
            extends GestureDetector.SimpleOnGestureListener {
        // Swipe properties, you can change it to make the swipe
        // longer or shorter and speed
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    MainActivity.this.onLeftSwipe();

                    // Right swipe
                } else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    MainActivity.this.onRightSwipe();
                }
            } catch (Exception e) {
                Log.e("YourActivity", "Error on gestures");
            }
            return false;
        }
    }
}
