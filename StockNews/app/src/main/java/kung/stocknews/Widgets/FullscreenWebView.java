package kung.stocknews.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by wkung on 12/25/16.
 */
public class FullscreenWebView extends WebView{

    private Context mContext;
    private static final int MAX_HEIGHT = 50;
    private IScrollInterface scrollInterface;
    private CompositeSubscription mCompositeSubscription;

    public FullscreenWebView(Context context) {
        super(context);
    }

    public FullscreenWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    //    Log.d("@@@, ", " t: " + t + " old t " + oldt);
        scrollInterface.onScrollChanged(t, oldt);
    }

    public void setScrollInterface(IScrollInterface i){
        scrollInterface = i;
    }

    public interface IScrollInterface{
        void onScrollChanged(int t, int oldt);
    }
}
