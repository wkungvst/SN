package kung.stocknews.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by wkung on 12/25/16.
 */
public class FullscreenWebView extends WebView{

    private Context mContext;

    public FullscreenWebView(Context context) {
        super(context);
    }

    public FullscreenWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
}
