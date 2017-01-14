package kung.stocknews.Views;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import kung.stocknews.Adapters.AutoAdapter;
import kung.stocknews.Adapters.IndexAdapter;
import kung.stocknews.Adapters.NewsAdapter;
import kung.stocknews.Model.IndexCard;
import kung.stocknews.Model.MockData;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.Model.AutoStockObject;
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
public class IndexFragment extends Fragment {

    private int MAX_LOG_LENGTH = 250;
    private CompositeSubscription mCompositeSubscription;
    RecyclerView recyclerView;
    IndexAdapter adapter;
    ArrayList<IndexCard> indexCardList;
    AutoCompleteTextView search;
    HashSet<String> stockList;
    BehaviorSubject<ArrayList<AutoStockObject>> stocksAutoList = BehaviorSubject.create();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_index, container, false);
        mCompositeSubscription = new CompositeSubscription();
        search = (AutoCompleteTextView)v.findViewById(R.id.index_search);
        recyclerView = (RecyclerView)v.findViewById(R.id.index_recycler);
        recyclerView.setHasFixedSize(true);

        RxView.longClicks(v.findViewById(R.id.search_go)).subscribe(c->{
            ((MainActivity)getActivity()).addStockToList(null);
            initialize();
        });

        RxView.clicks(v.findViewById(R.id.search_go)).subscribe(aVoid -> {
            Log.d("@@@", " add" );
            String symbol = ((AutoCompleteTextView)v.findViewById(R.id.index_search)).getText().toString();
            if(symbol != null){
                ((MainActivity)getActivity()).addStockToList(symbol);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initialize();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        mCompositeSubscription.add(stocksAutoList.subscribe((Action1<ArrayList<AutoStockObject>>) strings -> {
            Log.d("@#$_@$", " do the autocopmlete");
            AutoAdapter adapter1 = new AutoAdapter(
                    getActivity(), strings);
            search.setAdapter(adapter1);
            search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        ((MainActivity)getActivity()).closeKeyboard();
                }
            });
        }));

        setupSearch();
        initialize();
        return v;
    }

    private void initialize(){
        stockList = ((MainActivity)getActivity()).getStockList();
        if(stockList == null || stockList.size() == 0) return;
        String stockParams = "";
        for(String s : stockList){
            stockParams += s + ",";
        }
        stockParams = stockParams.substring(0,stockParams.length()-1);
        Log.d("@#$@$", " stockParams: " + stockParams);

        OkHttpClient client = new OkHttpClient();
        //http://www.google.com/finance/info?infotype=infoquoteall&q=GE,IBM,GOOG,AAPL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.GOOGLE_API).newBuilder();
        urlBuilder.addQueryParameter("q", stockParams);
        String url = urlBuilder.build().toString();
        // http://www.google.com/finance/company_news?q=IBM&start=0&num=30&output=json
        
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
                    responseData = responseData.substring(4,responseData.length());
                    parseJSONArray(responseData);
                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });
    }


    public void parseJSONArray(String array) throws JSONException {
        JSONArray jsonarray = new JSONArray(array);
        indexCardList = new ArrayList<IndexCard>();
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jObject = jsonarray.getJSONObject(i);
            createIndexCard(jObject);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new IndexAdapter(indexCardList);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(llm);
            }
        });
    }


    private void createIndexCard(JSONObject jObject) throws JSONException {
        IndexCard card = new IndexCard(jObject.getString("name"), jObject.getString("t"), "$" + jObject.getString("l"), jObject.getString("c"), jObject.getString("cp"));
        indexCardList.add(card);
    }


    private void setupSearch(){
        if(search == null){
            Log.e("@@@", " error: autocomplete search bar not initialized");
            return;
        }
       // http://bulllabs.com/api/stocknames

        OkHttpClient client = new OkHttpClient();
        //http://www.google.com/finance/info?infotype=infoquoteall&q=GE,IBM,GOOG,AAPL
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.TICKER_NAMES).newBuilder();
        String url = urlBuilder.build().toString();
        // http://www.google.com/finance/company_news?q=IBM&start=0&num=30&output=json

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
                    ArrayList<AutoStockObject> stocks = new ArrayList<AutoStockObject>();
                    String responseData = response.body().string();
                //    Log.d("$#@$"," response: " + responseData.toString());
                    JSONArray names = new JSONArray(responseData);
                    for(int i=0;i<names.length();i++){
                        JSONObject o = names.getJSONObject(i);
                    //    stocks.add(ticker);
                        stocks.add(new AutoStockObject(names.getJSONObject(i).getString("n"),names.getJSONObject(i).getString("t")));
                    }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stocksAutoList.onNext(stocks);
                    }
                });


                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });

        search.setThreshold(1);
        String[] stocks = getResources().getStringArray(R.array.stock_names);
       /*

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line, stocks);
        search.setAdapter(adapter);
        */

    }


}
