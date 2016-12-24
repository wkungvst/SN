package kung.stocknews.Views;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

import kung.stocknews.Adapters.NewsAdapter;
import kung.stocknews.Model.IndexCard;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;

/**
 * Created by wkung on 12/23/16.
 */
public class IndexFragment extends Fragment {

    RecyclerView recyclerView;
    NewsAdapter adapter;
    ArrayList<IndexCard> indexCardList;
    AutoCompleteTextView search;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_index, container, false);
        search = (AutoCompleteTextView)v.findViewById(R.id.index_search);
        setupSearch();
        initialize();
        return v;
    }

    private void initialize(){

    }

    private void setupSearch(){
        if(search == null){
            Log.e("@@@", " error: autocomplete search bar not initialized");
            return;
        }
        search.setThreshold(1);
        String[] stocks = getResources().getStringArray(R.array.stock_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_dropdown_item_1line, stocks);
        search.setAdapter(adapter);
    }
}
