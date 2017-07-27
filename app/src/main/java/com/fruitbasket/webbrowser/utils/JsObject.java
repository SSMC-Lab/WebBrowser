package com.fruitbasket.webbrowser.utils;

import android.app.Activity;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.fruitbasket.webbrowser.MainActivity;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by zzbpc on 2017/4/17.
 */

public class JsObject  {
    private Activity activity;
    public JsObject(Activity activity){
        this.activity = activity;
    }
    @JavascriptInterface
    public void tip(final String str){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
