package kung.stocknews.Views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.jakewharton.rxbinding.view.RxView;

import org.json.JSONException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import kung.stocknews.Model.MockData;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wkung on 12/23/16.
 */
public class MainActivity extends FragmentActivity {

    final static String PREFERENCES = "PREFERENCES";
    final static String SAVED_STOCK_LIST = "SAVED_STOCK_LIST";
    BehaviorSubject<ArrayList<NewsCard>> newsCards = BehaviorSubject.create();
    BehaviorSubject<HashSet<String>> stockListObservable;
    CompositeSubscription mCompositeSubscription;
    TabLayout tabLayout;
    ViewPager pager;
    HashSet<String> mStockList;

    List<NewsCard> newsCardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //    getTickerNames();
        initialize();
    }

    private void getTickerNames(){
        FullScreenWebFragment webFragment = FullScreenWebFragment.newInstance("http://www.reddit.com");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, webFragment);
        fragmentTransaction.commit();
        /*
        try {
            URL url = new URL("http://www.androidpeople.com/wp-content/uploads/2010/06/example.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("StockName");
Log.d("#$@$", " node length: " + nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
            }
        } catch (Exception e) {
            System.out.println("XML Pasing Excpetion = " + e);
        }
        */
    }


    private void initialize(){
        Log.d("#@$@", " main activity initialize");
        mCompositeSubscription = new CompositeSubscription();
        tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);

        // get stock list from saved preferences
        SharedPreferences prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        Set<String> list = prefs.getStringSet(SAVED_STOCK_LIST, null);
        mStockList = (list == null) ? new HashSet<String>(Storage.getDefaultStockList()) : new HashSet<String>(list);

        // create observer for stock list
        stockListObservable = BehaviorSubject.create(mStockList);
        mCompositeSubscription.add(stockListObservable.subscribe(new Action1<HashSet<String>>() {
            @Override
            public void call(HashSet<String> strings) {
            // Log.d("@@@", " stock list updated ");
            }
        }));
    }

    public void addStockToList(String stock){
        // TODO - add check for valid symbols

        if(stock == null){ // clear stock list
            mStockList = new HashSet<String>();
            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
            editor.putStringSet(SAVED_STOCK_LIST, null);
            editor.commit();
            stockListObservable.onNext(mStockList);
        }else{
            mStockList.add(stock);
            stockListObservable.onNext(mStockList);
            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
            editor.putStringSet(SAVED_STOCK_LIST, mStockList);
            editor.commit();
        }
    }

    public Observable<HashSet<String>> getStockListObservable(){
        return stockListObservable;
    }

    public HashSet<String> getStockList(){ return mStockList;}

    public class SectionPagerAdapter extends FragmentPagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new NewsFragment();
                case 1:
                default:
                    return new IndexFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "LATEST NEWS";
                case 1:
                default:
                    return "SUBSCRIPTIONS";
            }
        }
    }
}
