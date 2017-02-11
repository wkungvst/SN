package kung.stocknews.Adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import java.util.ArrayList;

import kung.stocknews.Model.AutoStockObject;
import kung.stocknews.R;
import kung.stocknews.Views.MainActivity;

/**
 * Created by wkung on 1/9/17.
 */
public class AutoAdapter extends BaseAdapter implements Filterable {

    private Filter filter = new CustomFilter();
    private ArrayList<AutoStockObject> suggestions = new ArrayList<>();
    private ArrayList<AutoStockObject> originalList;
    Context context;

    public AutoAdapter(Context context, ArrayList<AutoStockObject> list) {
        this.context = context;
        this.originalList = list;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Object getItem(int i) {
        return suggestions.get(i).getTicker();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.stock_object_layout,
                    parent,
                    false);
            holder = new ViewHolder();
            holder.nameText = (TextView) convertView.findViewById(R.id.name);
            holder.tickerText = (TextView) convertView.findViewById(R.id.ticker);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(suggestions != null && suggestions.size() > 0){
            holder.nameText.setText(Html.fromHtml(suggestions.get(position).getName()));
            holder.tickerText.setText(Html.fromHtml(suggestions.get(position).getTicker()));
        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private static class ViewHolder {
        TextView nameText;
        TextView tickerText;
    }

    private class CustomFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            suggestions.clear();
            if (originalList != null && constraint != null) { // Check if the Original List and Constraint aren't null.
                for (int i = 0; i < originalList.size(); i++) {
                    if ((originalList.get(i).getName().toLowerCase().contains(constraint))
                    ||  (originalList.get(i).getTicker().toLowerCase().contains(constraint))){ // Compare item in original list if it contains constraints.
                        suggestions.add(originalList.get(i)); // If TRUE add item in Suggestions.
                    }else{
                    //    suggestions.add(originalList.get(i));
                    }
                }
            }
            FilterResults results = new FilterResults(); // Create new Filter Results and return this to publishResults;
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
