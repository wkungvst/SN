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

import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private ArrayList<NewsCard> newsCards;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView time;
        private TextView summary;
        private TextView source;

        public ViewHolder(View v) {
            super(v);
            title = (TextView)v.findViewById(R.id.news_title);
            time = (TextView)v.findViewById(R.id.news_time);
            summary = (TextView)v.findViewById(R.id.news_summary);
            source = (TextView)v.findViewById(R.id.news_source);
        }
    }

    public NewsAdapter(ArrayList<NewsCard> cards) {
        newsCards = cards;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(newsCards.get(position).getTitle());
        holder.summary.setText(newsCards.get(position).getSummary());
        holder.time.setText(newsCards.get(position).getTime());
        holder.source.setText(newsCards.get(position).getSource());

    }

    @Override
    public int getItemCount() {
        return newsCards.size();
    }
}
