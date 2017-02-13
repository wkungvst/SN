package kung.stocknews.Views;

import android.animation.Animator;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import kung.stocknews.Adapters.IndexAdapter;
import kung.stocknews.Adapters.NewsAdapter;
import kung.stocknews.Model.MockData;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import kung.stocknews.Widgets.ControlRecyclerView;
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

import static kung.stocknews.Storage.Storage.SCROLL_THRESHOLD;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsFragment extends Fragment {

    ControlRecyclerView recyclerView;
    NewsAdapter adapter;
    ArrayList<NewsCard> newsCardList;
    CompositeSubscription mCompositeSubscription;
    HashSet<String> stockList;
    private SwipeRefreshLayout swipeContainer;
    private BehaviorSubject<Boolean> isError = BehaviorSubject.create(false);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        mCompositeSubscription = new CompositeSubscription();

        mCompositeSubscription.add(isError.subscribe(e->{
            if(getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if(adapter == null){
                    view.findViewById(R.id.warning).setVisibility(e? View.VISIBLE : View.GONE);
                }
                if(e){
                    ((MainActivity)getActivity()).showSnackbar(MainActivity.NO_NETWORK);
                }else{
                    ((MainActivity)getActivity()).dismissSnacks();
                }
            });
        }));

        swipeContainer = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);

        swipeContainer.setOnRefreshListener(() -> {
            initialize();
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.red_primary,
                R.color.green_primary,
                R.color.secondary,
                R.color.green_primary);

        recyclerView = (ControlRecyclerView)view.findViewById(R.id.news_recycler);
        recyclerView.setCustomScrollListener((MainActivity)getActivity());
        recyclerView.setHasFixedSize(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Observable stockListObservable = ((MainActivity)getActivity()).getStockListObservable();
        mCompositeSubscription.add(stockListObservable.subscribe(list->{
            initialize();
        }));
    }

    private void initialize(){
        newsCardList = new ArrayList<NewsCard>();
        stockList = ((MainActivity)getActivity()).getStockList();
        if((stockList == null) || stockList.size() == 0){
            if(swipeContainer != null){
                swipeContainer.setRefreshing(false);
            }
            recyclerView.removeAllViews();
            recyclerView.setAdapter(null);
            return;
        }

        for(final String stock : stockList){
            OkHttpClient client = new OkHttpClient();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.GOOGLE_NEWS_API).newBuilder();
            urlBuilder.addQueryParameter("q", stock);
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
                    if(swipeContainer != null){
                        getActivity().runOnUiThread(()->{swipeContainer.setRefreshing(false);});
                    }
                    isError.onNext(true);
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
                           //     Log.d("@#$@#$", "cluster obj: " + clusterObj);
                            if(clusterObj.has("a")){
                                JSONArray array = clusterObj.getJSONArray("a");
                             //       Log.d("@#$@)&*#$", " array: " + array);
                                for(int i=0;i<array.length();i++){
                                    JSONObject item = array.getJSONObject(i);
                                 //   Log.d("@$@)$", " json item: " + item);
                                    NewsCard n = new NewsCard(stock, item.get("t").toString(), item.get("d").toString(), item.get("sp").toString(), item.get("s").toString(), item.get("u").toString());

                                //    Log.d("$@$@#", " [stock = " + stock + "] TITLE: " + item.get("t") + " DATE TIME: " + item.get("d") + "\n");
                                    newsCardList.add(n);
                                }
                            }
                        }
                        isError.onNext(false);
                        if(getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            sort();
                            adapter = new NewsAdapter(newsCardList);
                            adapter.notifyDataSetChanged();

                            recyclerView.setAdapter(adapter);
                            LinearLayoutManager llm = new LinearLayoutManager(getActivity());
                            recyclerView.setLayoutManager(llm);
                            swipeContainer.setRefreshing(false);
                        });

                    } catch (JSONException e) {
                        Log.d("@@@", "Exception " + e);
                    }
                }
            });
        }
    }


    public void sort() {
        if(newsCardList == null || newsCardList.size() < 2) Log.d("@@@", " this could be trouble");
        Collections.sort(newsCardList, new Comparator<NewsCard>() {
            @Override
            public int compare(NewsCard n1, NewsCard n2) {
                return compareSort(n1.getTime(),n2.getTime());
            }
        });
    }

    private int compareSort(String t1, String t2){

        if(t1.contains("ago") && (!t2.contains("ago"))){
            return -1;
        }
        if(!t1.contains("ago") && (t2.contains("ago"))){
            return 1;
        }
        // both within 24 hours
        if (t1.contains("ago") && (t2.contains("ago"))){

            if(t1.contains("minute") && (!t2.contains("minute"))){
                return -1;
            }
            if(!t1.contains("minute") && (t2.contains("minute"))){
                return 1;
            }
            int r1 = Integer.parseInt(t1.substring(0,2).trim());
            int r2 = Integer.parseInt(t2.substring(0,2).trim());
            return r1 > r2 ? 1 : -1;
        }

        if (!t1.contains("ago") && (!t2.contains("ago"))){
            SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy");
            try {
                return df.parse(t2).compareTo(df.parse(t1));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }
}
