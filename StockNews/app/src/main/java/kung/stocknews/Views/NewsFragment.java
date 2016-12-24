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

import java.util.ArrayList;
import java.util.List;

import kung.stocknews.Adapters.NewsAdapter;
import kung.stocknews.Model.MockData;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsFragment extends Fragment {

    RecyclerView recyclerView;
    NewsAdapter adapter;
    ArrayList<NewsCard> newsCardList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newsCardList = MockData.getNewsCards();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.news_recycler);
        adapter = new NewsAdapter(newsCardList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        return view;
    }
}
