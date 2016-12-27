package kung.stocknews.Model;

import android.net.Uri;

import java.net.URL;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsCard {
    private String title;
    private String time;
    private String summary;
    private String source;
    private String url;

    public NewsCard(String title, String time, String summary, String source, String url){
        this.title = title;
        this.time = time;
        this.summary = summary;
        this.source = source;
        this.url = url;
    }

    public String getTitle(){
        return title;
    }

    public String getTime(){
        return time;
    }

    public String getSummary(){
        return summary;
    }

    public String getSource(){
        return source;
    }


    public String getUrl() {
        return url;
    }
}
