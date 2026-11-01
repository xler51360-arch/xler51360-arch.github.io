package fr.leriche.mesheures;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    private static final String START = "https://appassets.androidplatform.net/assets/index.html";
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("appassets.androidplatform.net".equals(u.getHost())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
                return true;
            }
        });

        web.addJavascriptInterface(new Bridge(), "Android");

        if (state != null) web.restoreState(state);
        else web.loadUrl(START);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    /** Pont appelé par la page : partage du PDF de la semaine (Gmail pré-rempli). */
    private class Bridge {
        @JavascriptInterface
        public boolean sharePdf(String base64, String fileName, String dest, String subject) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                File dir = new File(getCacheDir(), "pdf");
                if (!dir.exists()) dir.mkdirs();
                File f = new File(dir, fileName);
                FileOutputStream o = new FileOutputStream(f);
                o.write(data);
                o.close();

                Uri uri = FileProvider.getUriForFile(MainActivity.this, getPackageName() + ".files", f);
                final Intent send = new Intent(Intent.ACTION_SEND);
                send.setType("application/pdf");
                send.putExtra(Intent.EXTRA_STREAM, uri);
                if (dest != null && !dest.isEmpty()) send.putExtra(Intent.EXTRA_EMAIL, new String[]{dest});
                send.putExtra(Intent.EXTRA_SUBJECT, subject);
                send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(Intent.createChooser(send, "Envoyer le relevé"));
                    }
                });
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
