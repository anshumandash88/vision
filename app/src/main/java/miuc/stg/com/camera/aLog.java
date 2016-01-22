package miuc.stg.com.camera;

import android.util.Log;

/**
 * Created by Anshuman on 19-01-2016.
 */
public class aLog {
    public static void w (String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.w(TAG, msg);
        }
    }

}

