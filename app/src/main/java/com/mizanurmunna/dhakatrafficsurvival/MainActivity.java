package com.mizanurmunna.dhakatrafficsurvival;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {
    private WebView webView;
    private AdView adView;
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(11, 12, 16));
        getWindow().setNavigationBarColor(Color.rgb(11, 12, 16));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(11, 12, 16));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 12, 16));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);
        webView.addJavascriptInterface(new GameBridge(), "AndroidBridge");
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equalsIgnoreCase(uri.getScheme())) return false;
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                if ("http".equals(scheme) || "https".equals(scheme) || "mailto".equals(scheme)) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "লিংকটি খোলা যায়নি", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return true;
            }
        });

        root.addView(webView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        FrameLayout adContainer = new FrameLayout(this);
        adContainer.setBackgroundColor(Color.rgb(11, 12, 16));
        adContainer.setMinimumHeight(dp(50));
        adContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(adContainer);

        setContentView(root);
        webView.loadUrl("file:///android_asset/index.html");

        // Initialize the Mobile Ads SDK away from the main UI thread.
        new Thread(() -> {
            MobileAds.initialize(MainActivity.this, status ->
                    runOnUiThread(() -> loadBanner(adContainer)));
        }).start();
    }

    private void loadBanner(FrameLayout container) {
        if (isFinishing()) return;
        adView = new AdView(this);
        adView.setAdUnitId(TEST_BANNER_AD_UNIT_ID);
        int widthDp = (int) (getResources().getDisplayMetrics().widthPixels /
                getResources().getDisplayMetrics().density);
        adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, widthDp));
        container.removeAllViews();
        container.addView(adView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));
        adView.loadAd(new AdRequest.Builder().build());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class GameBridge {
        @JavascriptInterface
        public void savePng(String base64, String filename) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                String safeName = (filename == null || filename.trim().isEmpty())
                        ? "dhaka-traffic-survival-result.png" : filename.replaceAll("[^a-zA-Z0-9._-]", "_");
                if (!safeName.toLowerCase().endsWith(".png")) safeName += ".png";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, safeName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/DhakaTrafficSurvival");
                    Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (uri == null) throw new Exception("Could not create image");
                    try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                        if (out == null) throw new Exception("Could not open image");
                        out.write(bytes);
                    }
                } else {
                    File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DhakaTrafficSurvival");
                    if (!dir.exists() && !dir.mkdirs()) throw new Exception("Could not create folder");
                    try (FileOutputStream out = new FileOutputStream(new File(dir, safeName))) {
                        out.write(bytes);
                    }
                }
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "PNG ছবিটি Pictures/DhakaTrafficSurvival ফোল্ডারে সেভ হয়েছে",
                        Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "ছবি সেভ করা যায়নি: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }

        @JavascriptInterface
        public void sharePng(String base64, String text) {
            try {
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                File dir = new File(getCacheDir(), "shared");
                if (!dir.exists() && !dir.mkdirs()) throw new Exception("Cache folder unavailable");
                File image = new File(dir, "dhaka-traffic-survival-result.png");
                try (FileOutputStream out = new FileOutputStream(image)) { out.write(bytes); }
                Uri uri = androidx.core.content.FileProvider.getUriForFile(
                        MainActivity.this,
                        getPackageName() + ".fileprovider",
                        image);
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("image/png");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "ফলাফল শেয়ার কর"));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "শেয়ার করা যায়নি: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }

        @JavascriptInterface
        public void shareText(String text) {
            try {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
                startActivity(Intent.createChooser(share, "চ্যালেঞ্জ শেয়ার কর"));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "শেয়ার করা যায়নি", Toast.LENGTH_SHORT).show());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            webView.evaluateJavascript(
                    "(function(){var p=document.getElementById('pauseScreen');" +
                    "var o=document.getElementById('overScreen');var s=document.getElementById('statsScreen');" +
                    "var m=document.getElementById('menu');" +
                    "if(p&&!p.hidden){document.getElementById('btnHomeP').click();return 'handled';}" +
                    "if(o&&!o.hidden){document.getElementById('btnHomeO').click();return 'handled';}" +
                    "if(s&&!s.hidden){document.getElementById('btnStatsBack').click();return 'handled';}" +
                    "if(m&&m.hidden){if(window.__androidPause){window.__androidPause();}return 'paused';}" +
                    "return 'exit';})()", value -> {
                        if (value == null || value.contains("exit")) finish();
                    });
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if (adView != null) adView.pause();
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adView != null) adView.resume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) adView.destroy();
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
        }
        super.onDestroy();
    }
}
