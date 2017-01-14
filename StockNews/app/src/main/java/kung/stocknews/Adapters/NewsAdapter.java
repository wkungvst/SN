package kung.stocknews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Views.DetailActivity;
import kung.stocknews.Views.FullScreenWebFragment;
import kung.stocknews.Views.MainActivity;

/**
 * Created by wkung on 12/23/16.
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private ArrayList<NewsCard> newsCards;
    Activity context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView time;
        private TextView summary;
        private TextView source;
        private String url;
        private TextView symbol;

        public ViewHolder(final View v) {
            super(v);
            symbol = (TextView)v.findViewById(R.id.news_symbol);
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
        context = (Activity)parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.title.setText(Html.fromHtml(newsCards.get(position).getTitle()));
        holder.summary.setText(Html.fromHtml(newsCards.get(position).getSummary()));
        holder.time.setText(newsCards.get(position).getTime());
        holder.source.setText(newsCards.get(position).getSource());
        holder.symbol.setText(newsCards.get(position).getSymbol());
        holder.url = newsCards.get(position).getUrl();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.url == null)return;
                FullScreenWebFragment webFragment = FullScreenWebFragment.newInstance(holder.url);
                FragmentManager fm = ((MainActivity)context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.main_frame, webFragment);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsCards.size();
    }
}
