package com.example.nativebjnexample.nativebjnexample;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
    Simple WebView code that loads an HTML page containing WebRTC code

    Original code reference:
    http://www.flapjacksandcode.com/blog/2015/2/17/android-webviews-getusermedia-putting-it-all-together

 */


//************************We need Camera and Microphone run time permissions for this code to work************
// Set them manually, until we do it programmatically
// https://developer.android.com/training/permissions/requesting.html


public class MainActivity extends Activity {
    private String TAG = "MainActivity";
    private WebView mWebView;
    private Button fillInFormButton;
    private Button toggleVideoButton;
    private Button joinMtgButton;
    private TextView tvMtgId;

    private String BJNCallHeader = "javascript:BJN.RTCClient" + ".";



    // HTML file in app/src/main/assets folder - this WORKS
    //private static String URL = "file:///android_asset/test.html";

    // HTML file on the server
    private static String URL =
              "https://bluejeans.github.io/sdk-webrtc-meetings";
            // "https://glenninn.github.io/xyzzy/index.html";
            // "https://bluejeans.github.io/sdk-webrtc-onvideo/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main2);

        mWebView = (WebView) findViewById(R.id.webview);
        fillInFormButton =(Button)findViewById(R.id.btnFillInForm);
        toggleVideoButton =(Button)findViewById(R.id.btnToggleVideo);
        joinMtgButton =(Button)findViewById(R.id.btnJoinMtg);
        tvMtgId =(TextView)findViewById(R.id.mtgIdText);

        // Button handlers
        fillInFormButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlButtonFillInForm();
                }
            }
        );

        toggleVideoButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlButtonToggleVideo();
                }
            }
        );

        joinMtgButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hdlJoinMeeting();
                }
            }
        );

        // Enable JavaScript
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //////////////// these require API level 16 /////////////////////////////
        // These are required to allow webkitGetUserMedia() for work from a local URL file:
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        //////////////// these require API level 16 /////////////////////////////

        // Add JavaScript interface to allow calls from WebView -> Android
        mWebView.addJavascriptInterface(new MyFromJavascriptInterface(this), "Android");

        // Set a web view client
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        // Set a chrome client
        mWebView.setWebChromeClient(new WebChromeClient() {
            // Need to accept permissions to use the camera and audio
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                MainActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        // Make sure the request is coming from our file
                        // Warning: This check may fail for local files
                        //if(request.getOrigin().toString().equals(URL)) {
                            request.grant(request.getResources());
                        //}
                        //else {
                          //  request.deny();
                        //}
                    }
                });
            }

            // Output the WebView console logs
            // https://developer.android.com/guide/webapps/debugging.html#WebView
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(TAG, "WEBVIEW [" + cm.sourceId() + ", line " + cm.lineNumber() + "] "+cm.message());
                return true;
            }
        });

        // load the page
        mWebView.loadUrl(URL);
    }

    //
    // Interface from JavaScript webpage --> Android
    //
    // *****Note these methods are called on a thread called "JavaBridge" not the main thread
    //
    private class MyFromJavascriptInterface {
        //Context mContext;
        Activity mContext;

        MyFromJavascriptInterface(/*Context c*/Activity c) {
            mContext = c;
        }

        // This function can be called from JavaScript
        @JavascriptInterface
        public void hdlEvent1fromJson(String s) {
            Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        }

        // This function can be called from JavaScript
        @JavascriptInterface
        public void hdlEvent2fromJson(String s) {
            //Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();

            // We're not on the UI thread, so we need runOnUiThread()
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    count++;
                    String s = "Got event from JavaScript, f="+count;
                    setWebpageText(s);
                }
            });
        }
    }


    private static final String MEETING_ID = "id";
    private static final String MEETING_PASS_CODE = "passCode";
    private static final String MEETING_YOUR_NAME = "yourName";


    private static final String meetingId = "4159908751";
    private static final String passCode = "";
    private static final String yourName = "Neal Coleman";


    private void hdlButtonFillInForm() {
        setWebpageFormValue(MEETING_ID, meetingId);
        setWebpageFormValue(MEETING_PASS_CODE, passCode);
        setWebpageFormValue(MEETING_YOUR_NAME, yourName);
    }

    private void hdlButtonToggleVideo() {
        //
        String js = BJNCallHeader + "toggleVideoMute();";
        //String js = "" + "nealToggleVideo();";

        executeJavaScript(js);
    }

    private static boolean inMeeting = false;
    private void hdlJoinMeeting() {
        if(inMeeting){
            leaveMeeting();
            joinMtgButton.setText(R.string.join_mtg);
            inMeeting = false;
        } else {
            joinMeeting();
            joinMtgButton.setText(R.string.leave_mtg);
            inMeeting = true;
        }
    }

    int count = 0;
    private static final String FIELD0 = "yourName";


    // All WebView access must be done on the UI thread
    private void setWebpageText(String s) {
        String js = "javascript:document.getElementById('"+FIELD0+"').innerHTML = '"+s+"';";
        executeJavaScript(js);
    }

    private void setWebpageFormValue(String field, String val) {
        String js = "javascript:document.getElementById('"+field+"').value = '"+val+"';";
        executeJavaScript(js);
    }

    private void executeJavaScript(String js) {
        // system version check, API 19 = OS 4.4
        if (android.os.Build.VERSION.SDK_INT < 19) {
            // ....................this path has not been tested
            mWebView.loadUrl(js);
        }
        else {
            mWebView.evaluateJavascript(js, null);
        }
    }

    private void leaveMeeting() {
        String js = BJNCallHeader + "leaveMeeting()";
        executeJavaScript(js);
    }

    private void joinMeeting() {
         /*
        String s = "Joining Meeting";

        joinMeeting();
        setWebpageFormValue(MEETING_YOUR_NAME,s);
        */
        String ourId = tvMtgId.getText().toString();

        setWebpageFormValue(MEETING_ID,ourId);
        String mt  = "numericMeetingId : " + "\"" + ourId + "\"";
        String apc = "attendeePasscode : " + "\"\"";
        String dn  = "displayName : "      + "\"" + "SDK Button" + "\"";

        String param = "{ " + mt + "," + apc + "," + dn + " }";

        String js = BJNCallHeader +"joinMeeting(" + param + ")";
        executeJavaScript(js);
    }

}