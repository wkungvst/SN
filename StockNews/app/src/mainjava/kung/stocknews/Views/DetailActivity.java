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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import kung.stocknews.Helpers.LoadImageTask;
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
public class DetailActivity extends Activity implements LoadImageTask.Listener{

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
        chartImageView = (ImageView)findViewById(R.id.chart);
        symbolTextView = (TextView)findViewById(R.id.detail_symbol);
        nameTextView = (TextView)findViewById(R.id.detail_name);
        exchangeTextView = (TextView)findViewById(R.id.detail_exchange);
        lpriceTextView = (TextView)findViewById(R.id.detail_lprice);
        pchangeTextView = (TextView)findViewById(R.id.detail_pchange);
        pchangepTextView = (TextView)findViewById(R.id.detail_pchangep);
        ltradedTextView = (TextView)findViewById(R.id.detail_ltraded);
        ltradetTextView = (TextView)findViewById(R.id.detail_ltradet);

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
    }

    private void initialize(){
        getMainContent();
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
                            obj.getString("lt")
                    );

                    Log.d("@#$@#$", " Stock object " + obj);
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


    class DetailObject{

        String symbol;
        String name;
        String exchange;
        String lprice;
        String pchange;
        String pchangep;
        String ltraded;

        DetailObject(
                String symbol,
                String name,
                String exchange,
                String lprice,
                String pchange,
                String pchangep,
                String ltraded
        ){
            this.symbol = symbol;
            this.name = name;
            this.exchange = exchange;
            this.lprice = lprice;
            this.pchange = pchange;
            this.pchangep = pchangep;
            this.ltraded = ltraded;
        }
    }
}
