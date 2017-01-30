package kung.stocknews.Storage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wkung on 12/24/16.
 */
public class Storage {
    public static final String GOOGLE_API = "http://www.google.com/finance/info?infotype=infoquoteall";
    public static final String RED_PRIMARY = "#A92A27";
    public static final String GOOGLE_NEWS_API = "http://www.google.com/finance/company_news";
    public static final String TICKER_NAMES = "http://bulllabs.com/api/stocknames";
    public static final String VALUES = "VALUES";

    public static Set<String> getDefaultStockList(){
        Set<String> list = new HashSet<String>();
        list.add("DOW");
        list.add("NDAQ");
        return list;
    }
}
