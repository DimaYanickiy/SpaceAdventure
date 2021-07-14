package com.adventure.space;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.File;

public class SiteActivity extends AppCompatActivity {

    private ValueCallback<Uri[]> callback;
    private String photoPathString;
    SharedPreferences spref;

    private WebView siteView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);
        spref = getSharedPreferences("Settings", MODE_PRIVATE);
        siteView = (WebView)findViewById(R.id.siteView);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        setViewSettings();
        siteView.setWebViewClient(new WebViewClient() {
            public boolean overrideUrl(WebView view, String url) {
                if (url.startsWith("mailto:")) {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                } else if (url.startsWith("tg:") || url.startsWith("https://t.me") || url.startsWith("https://telegram.me")) {
                    try {
                        WebView.HitTestResult result = view.getHitTestResult();
                        String data = result.getExtra();
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                        view.getContext().startActivity(intent);
                    } catch (Exception ex) {
                    }
                    return true;
                } else {
                    return false;
                }
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return overrideUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return overrideUrl(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isFirstRef()) {
                    setGameUrl(url);
                    setFirstRef(false);
                    CookieManager.getInstance().flush();
                }
                CookieManager.getInstance().flush();
            }
        });

        siteView.setWebChromeClient(new WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void checkPermission() {
                ActivityCompat.requestPermissions(
                        SiteActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        1);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                             WebChromeClient.FileChooserParams fileChooserParams) {
                int permissionStatus = ContextCompat.checkSelfPermission(SiteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    if (callback != null) {
                        callback.onReceiveValue(null);
                    }
                    callback = filePathCallback;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", photoPathString);
                        if (photoFile != null) {
                            photoPathString = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("image/*");
                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }
                    Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                    chooser.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooser.putExtra(Intent.EXTRA_TITLE, "Photo");
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooser, 1);
                    return true;
                } else
                    checkPermission();
                return false;
            }

            private File createImageFile() {
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DirectoryNameHere");
                if (!imageStorageDir.exists())
                    imageStorageDir.mkdirs();
                imageStorageDir = new File(imageStorageDir + File.separator + "Photo_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                return imageStorageDir;
            }

            public void onProgressChanged(WebView view, int newProgress) {
                changeProgressBar(newProgress);
            }
        });

        progressBar.setFitsSystemWindows(true);

        loadSite();
    }

    public void loadSite(){
        siteView.loadUrl(getGameUrl());
    }

    private void changeProgressBar(int num){
        progressBar.setActivated(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(num);
        if (num == 100) {
            progressBar.setVisibility(View.GONE);
            progressBar.setActivated(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1 || callback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null || data.getData() == null) {
                if (photoPathString != null) {
                    results = new Uri[]{Uri.parse(photoPathString)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }
        }
        callback.onReceiveValue(results);
        callback = null;
    }


    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieManager.getInstance().flush();
    }

    public void setViewSettings() {
        siteView.getSettings().setLoadWithOverviewMode(true);
        siteView.getSettings().setUseWideViewPort(true);
        siteView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        siteView.getSettings().setPluginState(WebSettings.PluginState.ON);
        siteView.getSettings().setSavePassword(true);
        siteView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        siteView.requestFocus(View.FOCUS_DOWN);
        siteView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        siteView.getSettings().setUserAgentString(siteView.getSettings().getUserAgentString());
        siteView.getSettings().setJavaScriptEnabled(true);
        siteView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        siteView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        siteView.getSettings().setAppCacheEnabled(true);
        siteView.getSettings().setDomStorageEnabled(true);
        siteView.getSettings().setDatabaseEnabled(true);
        siteView.getSettings().setSupportZoom(false);
        siteView.getSettings().setAllowFileAccess(true);
        siteView.getSettings().setAllowFileAccess(true);
        siteView.getSettings().setAllowContentAccess(true);
        allowCookeis();
    }

    public void allowCookeis(){
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.acceptCookie();
        cookieManager.setAcceptThirdPartyCookies(siteView, true);
        cookieManager.flush();
    }

    @Override
    public void onBackPressed() {
        if(siteView.canGoBack()){
            siteView.goBack();
        } else{
            CookieManager.getInstance().flush();
            finish();
        }
    }

    public boolean isFirstRef() {
        return spref.getBoolean("first_ref", true);
    }

    public void setFirstRef(boolean firstRef) {
        spref.edit().putBoolean("first_ref", firstRef).apply();
    }

    public String getGameUrl() {
        return spref.getString("url", "");
    }

    public void setGameUrl(String gameUrl) {
        spref.edit().putString("url", gameUrl).apply();
    }
}