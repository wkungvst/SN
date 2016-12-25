package kung.stocknews.Views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wkung on 12/23/16.
 */
public class DetailActivity extends Activity {

    String symbol;
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
        initialize();
    }

    private void initialize(){
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
                    Log.d("@#$&*)@#$", " obj: " + obj);
                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });
    }

    class DetailObject{

        String symbol;
        String name;
        String lprice;
        String pchange;
        String pchangep;

        DetailObject(
                String symbol,
                String name,
                String lprice,
                String pchange,
                String pchangep
        ){

        }
    }
}
