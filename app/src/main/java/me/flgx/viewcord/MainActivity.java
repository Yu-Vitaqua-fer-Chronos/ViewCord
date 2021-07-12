/*
ViewCord
Copyright (C) 2021 Lukáš Horáček

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package me.flgx.viewcord;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    private WebView m_webView;
    private WebViewClient m_webViewClient;
    private CookieManager m_cookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();
    }

    // WebView back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && m_webView.canGoBack()) {
            m_webView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void setup() {
        // Find WebView
        m_webView = (WebView)findViewById(R.id.webView);

        // WebView client
        m_webViewClient = new WebViewClient() {
            // Allow only discord.com
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getHost().equals("discord.com")) {
                    return false;
                }

                return true;
            }

            // Block /science endpoint requests
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.contains("api") && url.contains("science")) {
                    Log.d("ViewCord","Blocked /science endpoint request");
                    return new WebResourceResponse("text/javascript", "UTF-8", null);
                }

                return super.shouldInterceptRequest(view, request);
            }

            // Inject CSS
            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    final String css = "[class^=\"sidebar-\"] { width: 25% !important; }";
                    final String encoded = Base64.encodeToString(css.getBytes("UTF-8"), Base64.NO_WRAP);

                    // https://stackoverflow.com/questions/30018540/inject-css-to-a-site-with-webview-in-android/30018910#30018910
                    view.evaluateJavascript("(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var style = document.createElement('style');" +
                            "style.type = 'text/css';" +
                            "style.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(style)" +
                            "})()", null
                    );
                } catch (Exception e) {
                    Log.e("ViewCord", "CSS injection error: " + e.getMessage());
                }

                super.onPageFinished(view, url);
            }
        };
        m_webView.setWebViewClient(m_webViewClient);

        // Cookies
        m_cookieManager = CookieManager.getInstance();
        m_cookieManager.setAcceptCookie(true);
        m_cookieManager.setAcceptThirdPartyCookies(m_webView, false);

        // Enable local storage
        m_webView.getSettings().setDomStorageEnabled(true);

        // Wide WebView
        //m_webView.getSettings().setUseWideViewPort(true);

        // Enable JavaScript
        m_webView.getSettings().setJavaScriptEnabled(true);

        // Load Discord
        m_webView.loadUrl("https://discord.com/channels/@me");
    }
}