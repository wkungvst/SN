package kung.stocknews.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wkung on 12/23/16.
 */
public class MainActivity extends FragmentActivity {

    public static final int STOP_TRACKING = 1;
    public final static String TICKER_SYMBOL = "TICKER_SYMBOL";
    public final static String NO_NETWORK = "No Network Connection. Try again later.";
    public final static String PREFERENCES = "PREFERENCES";
    public final static String SAVED_STOCK_LIST = "SAVED_STOCK_LIST";
    BehaviorSubject<ArrayList<NewsCard>> newsCards = BehaviorSubject.create();
    BehaviorSubject<HashSet<String>> stockListObservable;
    CompositeSubscription mCompositeSubscription;
    TabLayout tabLayout;
    ViewPager pager;
    HashSet<String> mStockList;
    Snackbar mSnackbar;
    List<NewsCard> newsCardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    public void closeKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getTickerNames(){
        FullScreenWebFragment webFragment = FullScreenWebFragment.newInstance("http://www.reddit.com");
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, webFragment);
        fragmentTransaction.commit();
    }


    private void initialize(){
        Log.d("#@$@", " main activity initialize");
        mCompositeSubscription = new CompositeSubscription();
        tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        pager = (ViewPager)findViewById(R.id.pager);
    //    showSnackbar("HIIIIII");
        pager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(pager);

        // get stock list from saved preferences
        SharedPreferences prefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        Set<String> list = prefs.getStringSet(SAVED_STOCK_LIST, null);
        mStockList = (list == null) ? new HashSet<String>(Storage.getDefaultStockList()) : new HashSet<String>(list);

        // create observer for stock list
        stockListObservable = BehaviorSubject.create(mStockList);
        mCompositeSubscription.add(stockListObservable.subscribe(strings -> {
         Log.d("@@@", " main activity: stock list updated ");
            for(String s : stockListObservable.getValue()){
                Log.d("@@@", " stock: " + s);
            }
        }));
        stockListObservable.onNext(mStockList);
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
            if(!mStockList.contains(stock)){
                mStockList.add(stock);
                stockListObservable.onNext(mStockList);
                SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
                editor.putStringSet(SAVED_STOCK_LIST, mStockList);
                editor.commit();
            }else{
                Log.d("#@$@#$", "stock already in list");
            }
        }
    }

    public void removeStockFromList(String stock){
        Log.d("@@@", " remove stock from list");
        if(mStockList.contains(stock)){
            mStockList.remove(stock);
            stockListObservable.onNext(mStockList);
            SharedPreferences.Editor editor = getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit();
            editor.putStringSet(SAVED_STOCK_LIST, mStockList);
            editor.commit();
            if(mStockList.size() == 0){
                showSnackbar("You have no subscriptions.");
            }
        }else{
            Log.e("@@@", " unknown stock");
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

    public void showSnackbar(String message){
        mSnackbar = Snackbar
                .make(findViewById(R.id.main_frame), message, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.colorPrimary));
        View snack = mSnackbar.getView();

        TextView snackText = (TextView)snack.findViewById(R.id.snackbar_text);
        snackText.setTypeface(null, Typeface.BOLD);

        // centering text in android's snackbar is unnecessarily complex
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            snackText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        else
            snackText.setGravity(Gravity.CENTER_HORIZONTAL);

        snack.setBackgroundColor(getResources().getColor(R.color.red_primary));
        mSnackbar.show();
    }

    public void dismissSnacks(){
        if(mSnackbar != null){
            mSnackbar.dismiss();
            mSnackbar = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null){
            if(data.getStringExtra(TICKER_SYMBOL) != null){
                removeStockFromList(data.getStringExtra(TICKER_SYMBOL));
            }else{
                Log.d("@@@", " wrong!");
            }
        }
    }
}
