package miuc.stg.com.camera;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by J on 17/11/2015.
 */
public class RetrieveInformation {

    public String getLink(String HTML){
        Document doc = parseHTML(HTML);
        Element element = parseLink(doc);

        return element.attr("href");
    }

    public String getTag(String HTML) throws JSONException {
        Document doc = parseHTML(HTML);
        Element element = parseImage(doc);
        JSONObject obj = new JSONObject(element.text());

        String s = obj.getString("s");
        String pattern = "<b>(.*?)</b>";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(s);

        System.out.println(s);

        while(m.find()){
            System.out.println("Found value: " + m.group(1) );

            if(!m.group(1).equals("...")){
                s = m.group(1);
            }

        }

        return s;
    }

    private Element parseImage(Document doc){
        Element element = doc.getElementById("rg_s");
        element = element.child(0);
        Elements elements = element.getElementsByClass("rg_meta");
        element = elements.first();

        return element;
    }

    private Element parseLink(Document doc){
        Element element = doc.getElementById("imagebox_bigimages");
        Elements elements = element.getElementsByTag("a");
        element = elements.first();

        return element;
    }

    private Document parseHTML(String HTML){
        return Jsoup.parse(HTML);
    }
}
