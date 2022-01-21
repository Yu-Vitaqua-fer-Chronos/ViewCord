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

package com.foc.viewcord;

import android.os.StrictMode;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {
    private WebView m_webView;
    private WebViewClient m_webViewClient;
    private CookieManager m_cookieManager;
    private OkHttpClient httpClient = new OkHttpClient();
    private Helper helper = new Helper();

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
        // Strict Mode disabled
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        // Array of client mods
        String[] clientMods = {
                "https://api.goosemod.com/inject.js",
                "https://raw.githubusercontent.com/Cumcord/Cumcord/stable/dist/build.js",
                "https://raw.githubusercontent.com/FlickerMod/dist/main/build.js"
        };

        // Find WebView
        m_webView = (WebView)findViewById(R.id.webView);

        // WebView client
        m_webViewClient = new WebViewClient() {
            // Allow only discord.com
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().getHost().contains("discord.com")) {
                    return false;
                } else if (request.getUrl().getHost().contains("discord.gg")) {
                    return false;
                } else if (request.getUrl().getHost().contains("discord.new")) {
                    return false;
                } else if (request.getUrl().getHost().contains("discordapp.com")) {
                    return false;
                }
                return true;
            }

            // Block /science and /track endpoint requests
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                if (url.contains("api")) {
                    if (url.contains("science")) {
                        Log.d("ViewCord", "Blocked /science endpoint request");
                        return new WebResourceResponse("text/javascript", "UTF-8", null);
                    } else if (url.contains("track")) {
                        Log.d("ViewCord", "Blocked /track endpoint request");
                        return new WebResourceResponse("text/javascript", "UTF-8", null);
                    }
                }

                return super.shouldInterceptRequest(view, request);
            }

            // Inject CSS
            @Override
            public void onPageFinished(WebView view, String url) {
                String vcCss = "[class^=\"sidebar-\"] { width: 25% !important; }";

                // https://stackoverflow.com/questions/30018540/inject-css-to-a-site-with-webview-in-android/30018910#30018910
                helper.injectCSS(view, vcCss);

                try {
                    String goosemod = httpClient.newCall(new Request.Builder().url(clientMods[0]).build()).execute().body().string();
                    helper.injectJS(view, goosemod);
                } catch (IOException e) {
                    Log.d("ViewCord", "Failed to load GooseMod: " + e.getMessage());
                }

                try {
                    String cumcord = httpClient.newCall(new Request.Builder().url(clientMods[1]).build()).execute().body().string();
                    helper.injectJS(view, cumcord);
                } catch (IOException e) {
                    Log.d("ViewCord", "Failed to load Cumcord: " + e.getMessage());
                }

                try {
                    String flickermod = httpClient.newCall(new Request.Builder().url(clientMods[2]).build()).execute().body().string();
                    helper.injectJS(view, flickermod);
                } catch (IOException e) {
                    Log.d("ViewCord", "Failed to load FlickerMod: " + e.getMessage());
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
