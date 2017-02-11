package kung.stocknews.Views;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import kung.stocknews.R;
import kung.stocknews.Widgets.FullscreenWebView;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static kung.stocknews.Storage.Storage.VALUES;

/**
 * Created by wkung on 12/25/16.
 */
public class FullScreenWebFragment extends Fragment implements FullscreenWebView.IScrollInterface {

    protected FullscreenWebView mWebView;
    protected static String url;
    protected ProgressBar loader;
    protected ProgressBar webviewLoader;
    protected CompositeSubscription subscription;
    protected int headerHeightConst = 0;
    protected ImageView share;
    protected ImageView header;
    protected boolean animateUpLock = false;
    protected boolean animateDownLock = false;
    public static final String USER_VIEW_MODEL_UUID_KEY = "userViewModelUUIDKey";

    public static FullScreenWebFragment newInstance(String Url) {
        FullScreenWebFragment f = new FullScreenWebFragment();

        if(Url != null){
            url = Url;
        }else{
            Log.e("@@@", "initialization error: no url passed to webview");
        }
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen, container, false);
        subscription = new CompositeSubscription();
        mWebView = (FullscreenWebView)view.findViewById(R.id.fullscreen_webview);
        mWebView.setScrollInterface(this);
    //  mWebView.getSettings().setBuiltInZoomControls(true);
        webviewLoader = (ProgressBar)view.findViewById(R.id.webview_loader);
        header = (ImageView)view.findViewById(R.id.webview_header);
        share = (ImageView)view.findViewById(R.id.share);
        headerHeightConst = (int) getResources().getDimension(R.dimen.webview_header_height);

        initializeSharing();
        initializeWebview();
        return view;
    }

    protected void initializeSharing(){
        Bundle bundle = getArguments();
        if(bundle == null) return;
        String title = bundle.getStringArrayList(VALUES).get(0);
        String symbol = bundle.getStringArrayList(VALUES).get(1);
        String source = bundle.getStringArrayList(VALUES).get(2);
        subscription.add(RxView.clicks(share).subscribe(c->{
            String message = "[" + source + "] " + title + " \n\nLink To Article: " + url + "\n\nSent via MarketSpring.";
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(share, "Save or share this article from " + source + ":"));
        }));
    }

    protected void initializeWebview(){
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AccountFullscreenWebViewClient());

        if(url != null){
            mWebView.loadUrl(url);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mWebView != null){
            mWebView.onResume();
        }
    }

    @Override
    public void onDestroy() {
        if(mWebView != null){
            mWebView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if(mWebView != null){
            mWebView.onPause();
        }
        super.onPause();
    }

    public void onBackPressed() {
        getActivity().onBackPressed();
    }

    @Override
    public void onScrollChanged(int t, int oldt) {
        updateHeader(t,oldt);
    }

    private void updateHeader(int t, int oldt){
        // scroll down
        if(t > oldt){
            animateUpLock = false;
            if(animateDownLock)return;
            header.animate().translationY(-header.getHeight()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {animateDownLock = true;}

                @Override
                public void onAnimationEnd(Animator animator) {animateDownLock = false;}

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            });
        // scroll up
        }else{
            animateDownLock = false;
            if(animateUpLock)return;
            header.animate().translationY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {animateUpLock = true;}

                @Override
                public void onAnimationEnd(Animator animator) {animateUpLock = false;}

                @Override
                public void onAnimationCancel(Animator animator) {}

                @Override
                public void onAnimationRepeat(Animator animator) {}
            });
        }
    }

    class AccountFullscreenWebViewClient extends WebViewClient {
        @SuppressLint("LongLogTag")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url != null && url.startsWith("vbk://")) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            } else {
                Log.e("some kind of error may or may not have occured" , "unrecognized protocol " + url);
                return false;
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if(webviewLoader != null){
                webviewLoader.setPadding(20,20,0,0);
                webviewLoader.setVisibility(View.VISIBLE);
            }
            super.onPageStarted(view,url,favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(webviewLoader != null){
                webviewLoader.setVisibility(View.INVISIBLE);
            }
        }
    }
}
