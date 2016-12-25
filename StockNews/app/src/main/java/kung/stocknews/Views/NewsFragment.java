package kung.stocknews.Views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import kung.stocknews.Adapters.NewsAdapter;
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
public class NewsFragment extends Fragment {

    RecyclerView recyclerView;
    NewsAdapter adapter;
    ArrayList<NewsCard> newsCardList;
    CompositeSubscription mCompositeSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsCardList = MockData.getNewsCards();

        mCompositeSubscription = new CompositeSubscription();
        Observable stockListObservable = ((MainActivity)getActivity()).getStockListObservable();
        mCompositeSubscription.add(stockListObservable.subscribe(new Action1<HashSet<String>>() {
            @Override
            public void call(HashSet<String> strings) {
                for(String s : strings){
                    Log.d("@#$@#$", " s : " + s);
                }
            }
        }));

        initialize();
    }

    private void initialize(){
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(Storage.GOOGLE_NEWS_API).newBuilder();
        urlBuilder.addQueryParameter("q", "IBM");
        urlBuilder.addQueryParameter("output", "json");
        urlBuilder.addQueryParameter("start", "0");
        urlBuilder.addQueryParameter("num", "2");
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
                    JSONObject jsonObject = new JSONObject(responseData);
                 //   Log.d("@#$@#", "obj: " + jsonObject);
                    JSONArray cluster = jsonObject.getJSONArray("clusters");
                 //   Log.d("#$)@#$", " a : " + cluster);
                    for(int j=0;j<cluster.length();j++){
                        JSONObject clusterObj = cluster.getJSONObject(j);
                     //   Log.d("@#$@#$", "cluster obj: " + clusterObj);
                        JSONArray array = clusterObj.getJSONArray("a");
                      //  Log.d("@#$@)&*#$", " array: " + array);
                        for(int i=0;i<array.length();i++){
                            JSONObject item = array.getJSONObject(i);
                         //   Log.d("$@$@#", " TITLEs: " + item.get("t") + " DATE TIME: " + item.get("d") + " BRIEF " + item.get("sp"));
                        }
                    }

                } catch (JSONException e) {
                    Log.d("@@@", "Exception " + e);
                }
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
    /*
        recyclerView = (RecyclerView)view.findViewById(R.id.news_recycler);
        adapter = new NewsAdapter(newsCardList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
    */
        return view;
    }
}
