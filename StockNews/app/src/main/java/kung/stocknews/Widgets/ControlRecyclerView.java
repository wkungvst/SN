package kung.stocknews.Widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by wkung on 2/10/17.
 */

public class ControlRecyclerView extends RecyclerView {

    private RecyclerScrollListener mListener;

    public ControlRecyclerView(Context context) {
        super(context);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCustomScrollListener(RecyclerScrollListener listener){
        mListener = listener;
    }

    public interface RecyclerScrollListener{
        void onScroll(int dy);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if(mListener != null){
            mListener.onScroll(dy);
        }
    }
}
