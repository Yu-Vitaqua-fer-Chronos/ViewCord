package com.foc.viewcord;

import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import java.io.UnsupportedEncodingException;

public class Helper {
    public void injectJS(WebView view, String inject) {
        String js = "const el = document.createElement(\"script\");" +
                 "el.appendChild(document.createTextNode( "+ inject + "));" +
                 "document.body.appendChild(el);";
        view.evaluateJavascript(js, null);
    }

    public void injectCSS(WebView view, String inject) {
        String encoded = null;
        try {
            encoded = Base64.encodeToString(inject.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            Log.e("ViewCord", "CSS injection error: " + e.getMessage());
        }
        view.evaluateJavascript("(function() {" +
                "var parent = document.getElementsByTagName('head').item(0);" +
                "var style = document.createElement('style');" +
                "style.type = 'text/css';" +
                "style.innerHTML = window.atob('" + encoded + "');" +
                "parent.appendChild(style)" +
                "})()", null);
    }
}
