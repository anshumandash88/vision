package miuc.stg.com.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.os.Build;

import java.util.List;

/**
 * Created by Anshuman on 08-12-2015.
 */
public class Helper {
    public static int getPictureSizeIndexForHeight(List<Camera.Size> sizeList, int height) {
        int chosenHeight = -1;
        for(int i=0; i<sizeList.size(); i++) {
            if(sizeList.get(i).height < height) {
                chosenHeight = i-1;
                if(chosenHeight==-1)
                    chosenHeight = 0;
                break;
            }
        }
        return chosenHeight;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static void setRotationParameter(Activity activity, int cameraId, Camera.Parameters param) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        rotation = (rotation + 45) / 90 * 90;

        int toRotate = (info.orientation + rotation) % 360;

        param.setRotation(toRotate);
    }
}
