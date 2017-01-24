package kung.stocknews.Adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;

import java.util.ArrayList;

import kung.stocknews.Model.NewsCard;
import kung.stocknews.R;
import kung.stocknews.Views.DetailActivity;
import kung.stocknews.Views.FullScreenWebFragment;
import kung.stocknews.Views.MainActivity;

/**
 * Created by wkung on 1/16/17.
 */
public class DetailNewsAdapter extends NewsAdapter {
    public DetailNewsAdapter(ArrayList<NewsCard> cards) {
        super(cards);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.url == null)return;
                FullScreenWebFragment webFragment = FullScreenWebFragment.newInstance(holder.url);
                FragmentManager fm = ((DetailActivity)context).getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.detail_main_container, webFragment);
                fragmentTransaction.commit();
            }
        });
    }
}
