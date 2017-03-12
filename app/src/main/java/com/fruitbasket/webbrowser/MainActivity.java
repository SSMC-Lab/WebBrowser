package com.fruitbasket.webbrowser;

import java.text.DecimalFormat;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.fruitbasket.webbrowser.messages.MeasurementStepMessage;
import com.fruitbasket.webbrowser.messages.MessageHUB;
import com.fruitbasket.webbrowser.messages.MessageListener;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends Activity implements MessageListener {
    private static final String TAG =MainActivity.class.toString();

    public static final String CAM_SIZE_WIDTH = "intent_cam_size_width";
    public static final String CAM_SIZE_HEIGHT = "intent_cam_size_height";
    public static final String AVG_NUM = "intent_avg_num";
    public static final String PROBANT_NAME = "intent_probant_name";
    private static final int BRIGHTNESS_FACTOR_DEFAULT=1;
    private static final int FONT_SIZE_FACTOR_DEFAULT=1;

    private SensorManager sensorManager;

    private RelativeLayout part1;
    private LinearLayout part2;

    private CameraSurfaceView _mySurfaceView;
    Camera _cam;
    TextView _currentDistanceView;
    Button _calibrateButton;
    private Button goTo;
    private EditText url;
    private EditText fontSize;
    private Button fontSizeOk;
    private EditText sizeFactor;
    private Button sizeFactorOk;
    private TextView sizeView;
    private EditText brightness;
    private Button brightnessOk;
    private EditText brightnessFactor;
    private Button brightnessFactorOk;
    private TextView backgroundBrightness;
    private TextView brightnessView;
    private WebView webView;

    private float _currentDevicePosition;
    private int _cameraHeight;
    private int _cameraWidth;
    private int _avgNum;
    private float distToFace;
    private float brightnessValue;///环境光亮度值
    private int fontSizeFactor =FONT_SIZE_FACTOR_DEFAULT;///后期可取消这两个值
    private int bFactor =BRIGHTNESS_FACTOR_DEFAULT;///

    private final static DecimalFormat _decimalFormater = new DecimalFormat("0.0");
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    /**
     * Abusing the media controls to create a remote control
     */
    // ComponentName _headSetButtonReceiver;
    // AudioManager _audioManager;
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MessageHUB.get().registerListener(this);
        // _audioManager.registerMediaButtonEventReceiver(_headSetButtonReceiver);

        // 1 for front cam. No front cam ? Not my fault!
        _cam = Camera.open(1);
        Camera.Parameters param = _cam.getParameters();

        // Find the best suitable camera picture size for your device. Competent
        // research has shown that a smaller size gets better results up to a
        // certain point.
        // http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6825217&url=http%3A%2F%2Fieeexplore.ieee.org%2Fiel7%2F6816619%2F6825201%2F06825217.pdf%3Farnumber%3D6825217
        List<Size> pSize = param.getSupportedPictureSizes();
        double deviceRatio = (double) this.getResources().getDisplayMetrics().widthPixels
                / (double) this.getResources().getDisplayMetrics().heightPixels;

        Size bestSize = pSize.get(0);
        double bestRation = (double) bestSize.width / (double) bestSize.height;

        for (Size size : pSize) {
            double sizeRatio = (double) size.width / (double) size.height;
            if (Math.abs(deviceRatio - bestRation) > Math.abs(deviceRatio
                    - sizeRatio)) {
                bestSize = size;
                bestRation = sizeRatio;
            }
        }
        _cameraHeight = bestSize.height;
        _cameraWidth = bestSize.width;

        Log.d("PInfo", _cameraWidth + " x " + _cameraHeight);

        param.setPreviewSize(_cameraWidth, _cameraHeight);
        _cam.setParameters(param);
        _mySurfaceView.setCamera(_cam);

        //注册光传感器
        sensorManager.registerListener(new MySensorEventListener(),
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageHUB.get().unregisterListener(this);
        resetCam();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onMessage(final int messageID, final Object message) {

        switch (messageID) {
            case MessageHUB.MEASUREMENT_STEP:
                updateUI((MeasurementStepMessage) message);
                break;

            case MessageHUB.DONE_CALIBRATION:
                _calibrateButton.setBackgroundResource(R.drawable.green_button);
                //part1.setVisibility(View.INVISIBLE);
                //part2.setVisibility(View.INVISIBLE);
                break;

            default:
                break;
        }

    }

    private void initViews() {
        part1=(RelativeLayout)findViewById(R.id.part1);
        part2=(LinearLayout)findViewById(R.id.part2);

        _mySurfaceView = (CameraSurfaceView) findViewById(R.id.surface_camera);
        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
                (int) (0.95 * this.getResources().getDisplayMetrics().widthPixels),
                (int) (0.6 * this.getResources().getDisplayMetrics().heightPixels));

        layout.setMargins(0, (int) (0.05 * this.getResources()
                .getDisplayMetrics().heightPixels), 0, 0);

        _mySurfaceView.setLayoutParams(layout);
        _currentDistanceView = (TextView) findViewById(R.id.currentDistance);
        _calibrateButton = (Button) findViewById(R.id.calibrateButton);

        View.OnClickListener listener = new MyOnclickListener();

        goTo = (Button) findViewById(R.id.go_to);
        goTo.setOnClickListener(listener);
        url = (EditText) findViewById(R.id.url);
        fontSize = (EditText) findViewById(R.id.font_size);
        fontSizeOk = (Button) findViewById(R.id.font_size_ok);
        fontSizeOk.setOnClickListener(listener);

        sizeFactor=(EditText)findViewById(R.id.size_factor);
        sizeFactorOk=(Button)findViewById(R.id.size_factor_ok);
        sizeFactorOk.setOnClickListener(listener);

        sizeView =(TextView)findViewById(R.id.size_view);

        brightness=(EditText)findViewById(R.id.brightness);
        brightnessOk=(Button)findViewById(R.id.brightness_ok);
        brightnessOk.setOnClickListener(listener);

        brightnessFactor=(EditText)findViewById(R.id.brightness_factor);
        brightnessFactorOk=(Button)findViewById(R.id.brightness_ok);
        brightnessFactorOk.setOnClickListener(listener);///写处理

        backgroundBrightness=(TextView)findViewById(R.id.background_brightness);
        brightnessView =(TextView)findViewById(R.id.brightness_view);

        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
        });
    }

    /**
     *
     * @param brightness
     */
    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp);
    }

    /**
     * Sets the current eye distance to the calibration point.
     *
     * @param v
     */
    public void pressedCalibrate(final View v) {
        if (!_mySurfaceView.isCalibrated()) {
            _calibrateButton.setBackgroundResource(R.drawable.yellow_button);
            _mySurfaceView.calibrate();
        }
    }

    public void pressedReset(final View v) {
        if (_mySurfaceView.isCalibrated()) {
            _calibrateButton.setBackgroundResource(R.drawable.red_button);
            _mySurfaceView.reset();
        }
    }

    public void onShowMiddlePoint(final View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();
        _mySurfaceView.showMiddleEye(on);
    }

    public void onShowEyePoints(final View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();
        _mySurfaceView.showEyePoints(on);
    }

    /**
     * Update the UI values.
     * @param message
     */
    public void updateUI(final MeasurementStepMessage message) {
        _currentDistanceView.setText(_decimalFormater.format(message.getDistToFace()) + " cm");
        float fontRatio = message.getDistToFace() / 29.7f;
        _currentDistanceView.setTextSize(fontRatio * 20);


        int size;
        double ratio1;
        double ratio2;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        //自动变化屏幕的亮度
        final double ds=Math.sqrt(
                Math.pow(metrics.widthPixels/metrics.xdpi,2)+
                        Math.pow(metrics.heightPixels/metrics.ydpi,2)
        );
        Log.i(TAG,"screen inch+="+ds);

        final int cvr=1;///应更改
        final double vd0=ds/(
                Math.sqrt(
                    Math.pow((double)metrics.widthPixels/(double)metrics.heightPixels,2)+1
                )
                        *cvr
                        *Math.tan(1D/60D)
        );
        Log.i(TAG,"vd0=="+vd0);

        final double vdp=message.getDistToFace();
        final int p=1;
        double lb=brightnessValue;
        double l0=lb*10D/7D;
        double lp=lb+Math.pow(vdp/(p*vd0),2)*(l0-lb);
        Log.i(TAG,"lp=="+lp);

        if(backgroundBrightness!=null){
            backgroundBrightness.setText(String.valueOf(lb));
        }

        if(brightnessView!=null){
            brightnessView.setText(String.valueOf(lp*bFactor));
        }

        setBrightness((float)(lp* bFactor));

        //更新网页浏览器的字体大小
        ratio1=0.47;//calibri
        ratio2=96.0/(double)(metrics.densityDpi);
        size=(int)(((1/Math.sqrt(3))*message.getDistToFace())/(ratio1*ratio2));
        Log.d(TAG,"dpi="+metrics.densityDpi);
        Log.d(TAG,"ratio2="+ratio2);
        Log.d(TAG,"size="+size);

        if(sizeView!=null){
            sizeView.setText(String.valueOf(size));
        }

        if (webView != null) {
            WebSettings settings = webView.getSettings();
            settings.setTextZoom(size* fontSizeFactor);
        }
        else{
            Log.d(TAG,"webView is null");
        }
    }

    private void resetCam() {
        _mySurfaceView.reset();

        _cam.stopPreview();
        _cam.setPreviewCallback(null);
        _cam.release();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }



    /**
     *
     */
    private class MyOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.go_to:
                    Log.i(TAG, "Button go to has been clicked");
                    String urlString = url.getText().toString().trim();
                    if (webView != null) {
                        if (TextUtils.isEmpty(urlString) == false) {
                            webView.loadUrl(urlString);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("<html>");
                            sb.append("<head>");
                            sb.append("<title>Welcome !</title>");
                            sb.append("</head>");
                            sb.append("<body>");

                            sb.append("<p style=\"font-family:Calibri\">");
                            sb.append("The Bible (from Koine Greek τὰ βιβλία, tà biblía, \"the books\"[1]) is a collection of sacred texts or scriptures that Jews and Christians consider to be a product of divine inspiration and a record of the relationship between God and humans.\n" +
                                    "Many different authors contributed to the Bible. And what is regarded as canonical text differs depending on traditions and groups; a number of Bible canons have evolved, with overlapping and diverging contents.[2] The Christian Old Testament overlaps with the Hebrew Bible and the Greek Septuagint; the Hebrew Bible is known in Judaism as the Tanakh. The New Testament is a collection of writings by early Christians, believed to be mostly Jewish disciples of Christ, written in first-century Koine Greek. These early Christian Greek writings consist of narratives, letters, and apocalyptic writings. Among Christian denominations there is some disagreement about the contents of the canon, primarily the Apocrypha, a list of works that are regarded with varying levels of respect.\n" +
                                    "Attitudes towards the Bible also differ amongst Christian groups. Roman Catholics, Anglicans and Eastern Orthodox Christians stress the harmony and importance of the Bible and sacred tradition, while Protestant churches focus on the idea of sola scriptura, or scripture alone. This concept arose during the Protestant Reformation, and many denominations today support the use of the Bible as the only source of Christian teaching.\n" +
                                    "With estimated total sales of over 5 billion copies, the Bible is widely considered to be the best-selling book of all time.[3][4] It has estimated annual sales of 100 million copies,[5][6] and has been a major influence on literature and history, especially in the West where the Gutenberg Bible was the first mass-printed book. The Bible was the first book ever printed using movable type.");
                            sb.append("</p>");

                            sb.append("</body>");
                            sb.append("</html>");
                            webView.loadData(sb.toString(), "text/html", "utf-8");
                        }
                    } else {
                        Log.e(TAG, "Go ot error");
                    }
                    break;

                case R.id.font_size_ok:
                    Log.i(TAG, "Button fontSizeOk has been clicked");

                    String fontSizeString = fontSize.getText().toString().trim();
                    if (fontSizeString != null
                            && TextUtils.isEmpty(fontSizeString) == false
                            && webView != null) {
                        WebSettings settings = webView.getSettings();
                        settings.setTextZoom(Integer.parseInt(fontSizeString));
                    } else {
                        Log.e(TAG, "zoom error");
                    }
                    break;

                case R.id.size_factor_ok:
                    Log.i(TAG,"size_factor_ok has been clicked");
                    String sizeFactorString=sizeFactor.getText().toString().trim();
                    if(sizeFactorString!=null
                            && TextUtils.isEmpty(sizeFactorString)==false){
                        fontSizeFactor =Integer.parseInt(sizeFactorString);
                    }
                    break;

                case R.id.brightness_ok:
                    Log.i(TAG,"brightness_ok has been clicked.");
                    String brightnessString=brightness.getText().toString().trim();
                    if(TextUtils.isEmpty(brightnessString)==false){
                        setBrightness(Float.parseFloat(brightnessString));
                    }
                    else{
                    }
                    break;

                case R.id.brightness_factor_ok:
                    Log.i(TAG,"brightness_factor_ok: has been clicked");
                    String bFactorString=brightnessFactor.getText().toString().trim();
                    if(TextUtils.isEmpty(bFactorString)==false){
                        bFactor=Integer.parseInt(bFactorString);
                    }
                    break;

                default:

            }
        }
    }

    //动态改变网页的背景和前景。bg和fg代表背景和前景色，可以是英文颜色名，也可以是rgb值（#RRGGBB）
    private void changeFgAndBg(String bg,String fg ){
        webView.getSettings().setJavaScriptEnabled(true);
        String js ="javascript:"+"var sheet = document.getElementsByTagName('style');if(sheet.length==0) sheet =document.createElement('style');else sheet = document.getElementsByTagName('style')[0];sheet.innerHTML='* { color : "+fg+" !important;background: "+bg+"!important}';document.body.appendChild(sheet);";
        webView.loadUrl(js);
    }

    private class MySensorEventListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values=sensorEvent.values;
            switch(sensorEvent.sensor.getType()){
                case Sensor.TYPE_LIGHT:
                    brightnessValue=values[0];
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    }
}