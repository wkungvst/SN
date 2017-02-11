package kung.stocknews.Adapters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;

import java.util.ArrayList;

import kung.stocknews.Model.IDetailNews;
import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Views.DetailActivity;
import kung.stocknews.Views.FullScreenWebFragment;
import kung.stocknews.Views.MainActivity;

import static kung.stocknews.Storage.Storage.VALUES;

/**
 * Created by wkung on 1/16/17.
 */
public class DetailNewsAdapter extends NewsAdapter {

    IDetailNews detailActivity;
    ArrayList<NewsCard> cards;

    public DetailNewsAdapter(ArrayList<NewsCard> cards, IDetailNews detailActivity) {
        super(cards);
        this.cards = cards;
        this.detailActivity = detailActivity;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        holder.itemView.setOnClickListener(view -> {
            if(holder.url == null)return;
            detailActivity.hideRemoveButton();
            detailActivity.removeElevation();
            Bundle bundle = new Bundle();
            ArrayList<String> bundleList = new ArrayList<>();
            bundleList.add(0, cards.get(position).getTitle());
            bundleList.add(1, cards.get(position).getSymbol());
            bundleList.add(2, cards.get(position).getSource());
            bundle.putStringArrayList(VALUES, bundleList);
            FullScreenWebFragment webFragment = FullScreenWebFragment.newInstance(holder.url);
            FragmentManager fm = ((DetailActivity)context).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.detail_main_container, webFragment);
            fragmentTransaction.commit();
        });
    }
}
