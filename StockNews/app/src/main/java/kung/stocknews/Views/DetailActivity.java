package kung.stocknews.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import kung.stocknews.Adapters.DetailNewsAdapter;
import kung.stocknews.Adapters.NewsAdapter;
import kung.stocknews.Helpers.LoadImageTask;
import kung.stocknews.Model.IDetailNews;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by wkung on 12/23/16.
 */
public class DetailActivity extends FragmentActivity implements LoadImageTask.Listener, IDetailNews{

    String symbol;
    DetailObject stockObject;
    BehaviorSubject<DetailObject> stockObjectObservable = BehaviorSubject.create();
    CompositeSubscription mCompositeSubscription;
    TextView symbolTextView;
    TextView nameTextView;
    TextView exchangeTextView;
    TextView lpriceTextView;
    TextView pchangeTextView;
    TextView pchangepTextView;
    TextView ltradetTextView;
    TextView ltradedTextView;
    TextView openTextView;
    TextView highTextView;
    TextView lowTextView;
    TextView yearHighTextView;
    TextView yearLowTextView;
    TextView marketCapTextView;
    TextView tsoTextView;
    ArrayList<NewsCard> newsCardList;
    RecyclerView recyclerView;
    DetailNewsAdapter adapter;

    static ImageView chartImageView;
    private static final String img_url = "http://ichart.finance.yahoo.com/instrument/1.0/GOOG/chart;range=1d/image;size=239x110";
    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent i = getIntent();
        symbol = i.getStringExtra("SYMBOL");
        if(symbol == null){
            Log.e("@@@", " no symbol specified");
            return;
        }
        recyclerView = (RecyclerView)findViewById(R.id.detail_news_recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);
        chartImageView = (ImageView)findViewById(R.id.chart);
        symbolTextView = (TextView)findViewById(R.id.detail_symbol);
        nameTextView = (TextView)findViewById(R.id.detail_name);
        exchangeTextView = (TextView)findViewById(R.id.detail_exchange);
        lpriceTextView = (TextView)findViewById(R.id.detail_lprice);
        pchangeTextView = (TextView)findViewById(R.id.detail_pchange);
        pchangepTextView = (TextView)findViewById(R.id.detail_pchangep);
        ltradedTextView = (TextView)findViewById(R.id.detail_ltraded);
        ltradetTextView = (TextView)findViewById(R.id.detail_ltradet);
        openTextView = (TextView)findViewById(R.id.detail_open);
        highTextView = (TextView)findViewById(R.id.detail_high);
        lowTextView = (TextView)findViewById(R.id.detail_low);
        yearHighTextView = (TextView)findViewById(R.id.detail_year_high);
        yearLowTextView = (TextView)findViewById(R.id.detail_year_low);
        tsoTextView = (TextView)findViewById(R.id.detail_tso);
        marketCapTextView = (TextView)findViewById(R.id.detail_market_cap);

        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(stockObjectObservable.subscribe(new Action1<DetailObject>() {
            @Override
            public void call(DetailObject detailObject) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        populateFields();
                    }
                });
            }
        }));

        mCompositeSubscription.add(RxView.clicks(findViewById(R.id.remove_stock)).subscribe(click -> {
            Log.d("$@#$", " remove stock");
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(MainActivity.TICKER_SYMBOL,symbol);
            setResult(1,intent);
            finish();//finishing activity
        }));

        initialize();
        new LoadImageTask(this).execute(img_url);
    }

    private void populateFields(){
        symbolTextView.setText(stockObject.symbol);
        nameTextView.setText(stockObject.name);
        exchangeTextView.setText("(" +stockObject.exchange + ")");
        if((stockObject.pchange.substring(0,1)).equals("-")){
            pchangepTextView.setTextColor(Color.parseColor(Storage.RED_PRIMARY));
            pchangeTextView.setTextColor(Color.parseColor(Storage.RED_PRIMARY));
        }
        lpriceTextView.setText(stockObject.lprice);
        pchangeTextView.setText(" " + stockObject.pchange);
        pchangepTextView.setText(" (" + stockObject.pchangep + "%)");
        ltradedTextView.setText(stockObject.ltraded);
        lowTextView.setText(Html.fromHtml("<b>Low: </b>" + stockObject.low));
        highTextView.setText(Html.fromHtml("<b>High: </b>" + stockObject.high));
        openTextView.setText(Html.fromHtml("<b>Open: </b>" + stockObject.open));
        yearHighTextView.setText(Html.fromHtml("<b>52-Week High: </b>" + stockObject.high52));
        yearLowTextView.setText(Html.fromHtml("<b>52-Week Low: </b>" + stockObject.low52));
        tsoTextView.setText(Html.fromHtml("<b>TSO: </b>" + stockObject.tso));
        marketCapTextView.setText(Html.fromHtml("<b>Market Cap: </b>" + stockObject.marketCap));
    }

    private void initialize(){
        getMainContent();
        getNews();
    }

    private void getNews(){
        if(symbol == null){return;}
        newsCardList = new ArrayList<NewsCard>();
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.GOOGLE_NEWS_API).newBuilder();
        urlBuilder.addQueryParameter("q", symbol);
        urlBuilder.addQueryParameter("output", "json");
        urlBuilder.addQueryParameter("start", "0");
        urlBuilder.addQueryParameter("num", "10");
        String url = urlBuilder.build().toString();
        // http://www.google.com/finance/company_news?q=IBM&start=0&num=30&output=json

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    //    Log.d("@#$@#", "obj: " + jsonObject);
                    JSONArray cluster = jsonObject.getJSONArray("clusters");
                    //    Log.d("#$)@#$", " a : " + cluster);

                    for(int j=0;j<cluster.length();j++){
                        JSONObject clusterObj = cluster.getJSONObject(j);
                        //    Log.d("@#$@#$", "cluster obj: " + clusterObj);
                        if(clusterObj.has("a")){
                            JSONArray array = clusterObj.getJSONArray("a");
                            //    Log.d("@#$@)&*#$", " array: " + array);
                            for(int i=0;i<array.length();i++){
                                JSONObject item = array.getJSONObject(i);
                                NewsCard n = new NewsCard(symbol, item.get("t").toString(), item.get("d").toString(), item.get("sp").toString(), item.get("s").toString(), item.get("u").toString());
                            //    Log.d("$@$@#", " [stock = " + stock + "] TITLE: " + item.get("t") + " DATE TIME: " + item.get("d") + "\n");
                                newsCardList.add(n);
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new DetailNewsAdapter(newsCardList, DetailActivity.this);
                            adapter.notifyDataSetChanged();
                            recyclerView.setAdapter(adapter);
                            LinearLayoutManager llm = new LinearLayoutManager(DetailActivity.this);
                            recyclerView.setLayoutManager(llm);
                        }
                    });

                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });

    }

    private void getMainContent(){
        OkHttpClient client = new OkHttpClient();
        //http://www.google.com/finance/info?infotype=infoquoteall&q=GE,IBM,GOOG,AAPL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.GOOGLE_API).newBuilder();
        urlBuilder.addQueryParameter("q", symbol);
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("@$)@$", " fail!");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject obj = new JSONObject(responseData);
                    stockObject = new DetailObject(
                            obj.getString("t"),
                            obj.getString("name"),
                            obj.getString("e"),
                            obj.getString("l"),
                            obj.getString("c"),
                            obj.getString("cp"),
                            obj.getString("lt"),
                            obj.getString("op"),
                            obj.getString("hi"),
                            obj.getString("lo"),
                            obj.getString("hi52"),
                            obj.getString("lo52"),
                            obj.getString("shares"),
                            obj.getString("mc")

                    );

                    stockObjectObservable.onNext(stockObject);
                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });
    }


    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        chartImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onError() {

    }

    @Override
    public void hideRemoveButton() {
        findViewById(R.id.remove_stock).setVisibility(View.INVISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void removeElevation() {
        findViewById(R.id.detail_header).setElevation(0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        findViewById(R.id.remove_stock).setVisibility(View.VISIBLE);
    }

    class DetailObject{

        String symbol;
        String name;
        String exchange;
        String lprice;
        String pchange;
        String pchangep;
        String ltraded;
        String open;
        String high;
        String low;
        String high52;
        String low52;
        String tso;
        String marketCap;

        DetailObject(
                String symbol,
                String name,
                String exchange,
                String lprice,
                String pchange,
                String pchangep,
                String ltraded,
                String open,
                String high,
                String low,
                String high52,
                String low52,
                String tso,
                String marketCap
        ){
            this.symbol = symbol;
            this.name = name;
            this.exchange = exchange;
            this.lprice = lprice;
            this.pchange = pchange;
            this.pchangep = pchangep;
            this.ltraded = ltraded;
            this.open = open.length() == 0 ? "N/A" : open;
            this.high = high.length() == 0 ? "N/A" : high;
            this.low = low.length() == 0 ? "N/A" : low;
            this.high52 = high52.length() == 0 ? "N/A" : high52;
            this.low52 = low52.length() == 0 ? "N/A" : low52;
            this.marketCap = marketCap.length() == 0 ? "N/A" : marketCap;
            this.tso = tso.length() == 0 ? "N/A" : tso;
        }
    }
}
