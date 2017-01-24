package kung.stocknews.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import kung.stocknews.Model.IndexCard;
import kung.stocknews.R;
import kung.stocknews.Storage.Storage;
import kung.stocknews.Views.DetailActivity;
import kung.stocknews.Views.MainActivity;

/**
 * Created by wkung on 12/23/16.
 */
public class IndexAdapter extends RecyclerView.Adapter<IndexAdapter.ViewHolder> {

    private ArrayList<IndexCard> indexCards;
    private static Context activity;
    private String symbol;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView symbol;
        private TextView lprice;
        private TextView pchange;
        private TextView pchangep;

        public ViewHolder(View v) {
            super(v);
            activity = v.getContext();
            name = (TextView)v.findViewById(R.id.index_name);
            symbol = (TextView)v.findViewById(R.id.index_symbol);
            lprice = (TextView)v.findViewById(R.id.index_lprice);
            pchange = (TextView)v.findViewById(R.id.index_pchange);
            pchangep = (TextView)v.findViewById(R.id.index_pchangep);
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
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(activity, DetailActivity.class);
                i.putExtra("SYMBOL", indexCards.get(position).getSymbol());
                Bundle b = new Bundle();
                b.putString(MainActivity.TICKER_SYMBOL, indexCards.get(position).getSymbol());
                ((Activity)activity).startActivityForResult(i, MainActivity.STOP_TRACKING, b);
            }
        });
        holder.name.setText(indexCards.get(position).getName());
        holder.symbol.setText(indexCards.get(position).getSymbol());
        holder.lprice.setText(indexCards.get(position).getLastPrice());
        holder.pchange.setText(indexCards.get(position).getPriceChange());
        holder.pchangep.setText(indexCards.get(position).getPriceChangePercentage());
        if((indexCards.get(position).getPriceChange().substring(0,1)).equals("-")){
            holder.lprice.setTextColor(Color.parseColor(Storage.RED_PRIMARY));
        }
    }

    @Override
    public int getItemCount() {
        return indexCards.size();
    }
}
