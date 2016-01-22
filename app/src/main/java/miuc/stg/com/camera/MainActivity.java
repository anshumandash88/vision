package miuc.stg.com.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RetrofitError;


@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener  {

    private GestureDetectorCompat gDetector;
    private Vibrator mVibrator;
    private TextToSpeech tts;
    private int CAM_ACTIVITY =  1;

   String filePath;

    //imgur stuff
    private Upload upload; // Upload object containging image and meta data
    private File chosenFile; //chosen file from intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        this.gDetector = new GestureDetectorCompat(this,this);
        gDetector.setOnDoubleTapListener(this);

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
                tts.speak("Ready, Vision is. Double tap, to take picture, you must!", TextToSpeech.QUEUE_FLUSH, null);
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
                Toast.makeText(this, data.getExtras().getString("url"),
                        Toast.LENGTH_SHORT).show();

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
        new UploadService(this).Execute(upload, new UiCallback());
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
        upload.title = "vision-test-title";//uploadTitle.getText().toString();
        upload.description = "vision-test-desc";//uploadDesc.getText().toString();
        //upload.albumId="Vision";
    }

    private class UiCallback implements Callback<ImageResponse> {
        @Override
        public void success(ImageResponse imageResponse, retrofit.client.Response response) {
            Snackbar.make(findViewById(R.id.rootView),imageResponse.data.link.toString(), Snackbar.LENGTH_LONG).show();
            //Uri.parse()
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
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
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
        tts.speak("May the force be with you!", TextToSpeech.QUEUE_FLUSH, null);
        this.finishAffinity();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
