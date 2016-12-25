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

        for(int i=0;i<15;i++){
            NewsCard c0 = new NewsCard("This Is The News Title", "12/23/2016", "This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. ", "THE WASHINGTON POST", Uri.parse("http://google.com/news"));
            NewsCard c1 = new NewsCard("This Is A Different News Title", "3 Hours Ago", "This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. This is the summary of the news article. ", "BUSINESS INSIDER", Uri.parse("http://google.com/news"));
            newsCards.add(c0);
            newsCards.add(c1);
        }
        return newsCards;
    }

    public static ArrayList<IndexCard> getIndexCards(){
        ArrayList<IndexCard> indexCards = new ArrayList<IndexCard>();

        return indexCards;
    }

}
