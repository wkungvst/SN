package kung.stocknews.Model;

import android.net.Uri;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsCard {
    private String title;
    private String time;
    private String summary;
    private String source;
    private Uri link;

    public NewsCard(String title, String time, String summary, String source, Uri link){
        this.title = title;
        this.time = time;
        this.summary = summary;
        this.source = source;
        this.link = link;
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


}
