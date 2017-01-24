package kung.stocknews.Views;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

    private SwipeRefreshLayout swipeContainer;
    private int MAX_LOG_LENGTH = 250;
    private CompositeSubscription mCompositeSubscription;
    RecyclerView recyclerView;
    IndexAdapter adapter;
    ArrayList<IndexCard> indexCardList;
    AutoCompleteTextView search;
    HashSet<String> stockList;
    BehaviorSubject<ArrayList<AutoStockObject>> stocksAutoList = BehaviorSubject.create();
    BehaviorSubject<String> lastStock = BehaviorSubject.create();
    BehaviorSubject<Boolean> isError = BehaviorSubject.create(false);
    Activity mActivity;
    ArrayList<String> stocksCheckList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_index, container, false);
        swipeContainer = (SwipeRefreshLayout)v.findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(() -> initialize());
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.red_primary,
                R.color.green_primary,
                R.color.secondary,
                R.color.green_primary);
        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(isError.subscribe(e->{
            if(e){
                ((MainActivity)getActivity()).showSnackbar(MainActivity.NO_NETWORK);
            }
        }));

        search = (AutoCompleteTextView)v.findViewById(R.id.index_search);
        recyclerView = (RecyclerView)v.findViewById(R.id.index_recycler);
        recyclerView.setHasFixedSize(true);

        RxView.longClicks(v.findViewById(R.id.search_go)).subscribe(c->{
            ((MainActivity)getActivity()).addStockToList(null);
         //   initialize();
        });

        RxView.clicks(v.findViewById(R.id.search_go)).subscribe(aVoid -> {
            Log.d("@@@", " add" );
            String symbol = ((AutoCompleteTextView)v.findViewById(R.id.index_search)).getText().toString();
            if(symbol != null){ // TODO - minimum symbol length? valid symbol criteria?
                if(checkStock(symbol)){
                    lastStock.onNext(symbol);
                    ((MainActivity)getActivity()).addStockToList(symbol);
                }else{
                    Log.d("@@@", " stock not found in this bitch");
                }
            }
        });

        // autocomplete textview
        mCompositeSubscription.add(stocksAutoList.subscribe(strings -> {
            getActivity().runOnUiThread(()->{
                AutoAdapter autoAdapter = new AutoAdapter(
                        getActivity(), strings);
                search.setAdapter(autoAdapter);
                search.setOnItemClickListener((adapterView, view, i, l) -> ((MainActivity)getActivity()).closeKeyboard());
            });
        }));
        return v;
    }

    private Boolean checkStock(String symbol){
        Log.d("@@@", " we have " + stocksCheckList.size() + " symbols. checking for match");
        if(stocksCheckList.contains(symbol)){
            Log.d("@@@", " we found a match for " + symbol);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCompositeSubscription.add(((MainActivity)getActivity()).getStockListObservable().subscribe(l->{
            Log.d("@@@@@", " index fragment: get stock list observable updated");
            initialize();
            if(adapter != null){
                adapter.notifyDataSetChanged();
            }
        }));
        setupSearch();
        initialize();
    }


    public void initialize(){
        recyclerView.removeAllViews();
        if(swipeContainer != null){
            getActivity().runOnUiThread(() -> swipeContainer.setRefreshing(false));
        }
        stockList = ((MainActivity)getActivity()).getStockList();

        if(stockList == null || stockList.size() == 0){
            recyclerView.removeAllViews();
            recyclerView.setAdapter(null);
            return;
        }
        String stockParams = "";
        for(String s : stockList){
            stockParams += s + ",";
        }
        stockParams = stockParams.substring(0,stockParams.length()-1);

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
                isError.onNext(true);
                if(swipeContainer != null){
                    getActivity().runOnUiThread(() -> swipeContainer.setRefreshing(false));
                }
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if(swipeContainer != null){
                    getActivity().runOnUiThread(() -> swipeContainer.setRefreshing(false));
                }
                try {
                    String responseData = response.body().string();
                    responseData = responseData.substring(4,responseData.length());
                    parseJSONArray(responseData);
                } catch (JSONException e) {
                    if(lastStock.getValue() != null){
                        getActivity().runOnUiThread(()->{
                            ((MainActivity)getActivity()).removeStockFromList(lastStock.getValue());
                        });
                        Log.d("@@@", "Exception. Let's remove " + lastStock.getValue() + " from the list");
                    }
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
        getActivity().runOnUiThread(() -> {
            adapter = new IndexAdapter(indexCardList);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(llm);
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
                isError.onNext(true);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    stocksCheckList = new ArrayList<String>();
                    ArrayList<AutoStockObject> stocks = new ArrayList<AutoStockObject>();
                    String responseData = response.body().string();
                    JSONArray names = new JSONArray(responseData);
                    for(int i=0;i<names.length();i++){
                        JSONObject o = names.getJSONObject(i);
                        stocksCheckList.add(names.getJSONObject(i).getString("t"));
                        stocks.add(new AutoStockObject(names.getJSONObject(i).getString("n"),names.getJSONObject(i).getString("t")));
                    }

                    if(getActivity() != null){
                        getActivity().runOnUiThread(() -> stocksAutoList.onNext(stocks));
                    }else{
                        Log.d("$@#$", " response cancelled");
                    }

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
