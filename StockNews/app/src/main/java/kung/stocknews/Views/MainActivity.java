package kung.stocknews.Views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kung.stocknews.Model.MockData;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
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
        initialize();
    }

    private void initialize(){

        mCompositeSubscription = new CompositeSubscription();

        tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        pager = (ViewPager)findViewById(R.id.pager);

        pager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);

        SharedPreferences prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        Set<String> list = prefs.getStringSet(SAVED_STOCK_LIST, null);

        mStockList = (list == null) ? new HashSet<String>(Storage.getDefaultStockList()) : new HashSet<String>(list);
        for(String s : mStockList){
            Log.d("#@$@#", " tracked stock: " + s);
        }
        stockListObservable = BehaviorSubject.create(mStockList);

        mCompositeSubscription.add(stockListObservable.subscribe(new Action1<HashSet<String>>() {
            @Override
            public void call(HashSet<String> strings) {
                Log.d("@#$@$#", " woah there");
            }
        }));
/*
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
        editor.putString("name", "Elena");
        editor.putInt("idName", 12);
        editor.commit();
        */
    }

    public void addStockToList(String stock){
        // TODO - add check for valid symbols
        mStockList.add(stock);
        stockListObservable.onNext(mStockList);
        SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
        editor.putStringSet(SAVED_STOCK_LIST, mStockList);
        editor.commit();
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
                    return "SOURCES";
            }
        }
    }
}
