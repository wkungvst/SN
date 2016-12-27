package kung.stocknews.Model;

import android.net.Uri;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by wkung on 12/23/16.
 */
public class MockData {

    private MockData(){}

    public static ArrayList<NewsCard> getNewsCards(){
        ArrayList<NewsCard> newsCards = new ArrayList<NewsCard>();
        return newsCards;
    }

    public static ArrayList<IndexCard> getIndexCards(){
        ArrayList<IndexCard> indexCards = new ArrayList<IndexCard>();
        return indexCards;
    }
}
