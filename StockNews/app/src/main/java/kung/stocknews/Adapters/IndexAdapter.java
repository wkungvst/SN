package kung.stocknews.Adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import kung.stocknews.Model.IndexCard;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;

/**
 * Created by wkung on 12/23/16.
 */
public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    private ArrayList<IndexCard> indexCards;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView value;

        public ViewHolder(View v) {
            super(v);
            name = (TextView)v.findViewById(R.id.index_name);
            value = (TextView)v.findViewById(R.id.index_value);
        }
    }

    public IndexAdapter(ArrayList<IndexCard> cards) {
        indexCards = cards;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public IndexAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.index_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(indexCards.get(position).getName());
        holder.value.setText(indexCards.get(position).getValue());
    }

    @Override
    public int getItemCount() {
        return indexCards.size();
    }
}
