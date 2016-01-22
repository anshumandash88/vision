package miuc.stg.com.camera;

/**
 * Created by Anshuman on 19-01-2016.
 */
public class Constants {
    /*
      Logging flag
     */
    public static final boolean LOGGING = false;

    /*
      Your imgur client id. You need this to upload to imgur.

      More here: https://api.imgur.com/
     */
    public static final String MY_IMGUR_CLIENT_ID = "0359b21c204c9aa";//"0359b21c204c9aa";
    public static final String MY_IMGUR_CLIENT_SECRET = "0754a89b9ce0b793b940b621f95276750c301d23";//"a9229ed8f06c77ae1566ff281716ea1a7bfad204";

    /*
      Redirect URL for android.
     */
    public static final String MY_IMGUR_REDIRECT_URL = "http://android";

    /*
      Client Auth
     */
    public static String getClientAuth() {
        return "Client-ID " + MY_IMGUR_CLIENT_ID;
    }

}

