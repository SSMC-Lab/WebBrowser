package com.fruitbasket.webbrowser;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

/**
 * 已经弃用
 */
public class WebPage extends Activity {
    private static final String TAG="WebPage";

    private Button goTo;
    private EditText url;
    private EditText fontSize;
    private Button ok;
    private WebView webView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews(){
        View.OnClickListener listener=new MyOnclickListener();

        goTo=(Button)findViewById(R.id.go_to);
        goTo.setOnClickListener(listener);
        url=(EditText)findViewById(R.id.url);
        fontSize=(EditText)findViewById(R.id.font_size);
        ok=(Button)findViewById(R.id.font_size_ok);
        ok.setOnClickListener(listener);

        webView=(WebView)findViewById(R.id.web_view);
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            { //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
        });
    }


    /**
     *
     */
    private class MyOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.go_to:
                    Log.i(TAG,"Button go to has been clicked");
                    String urlString =url.getText().toString().trim();
                    if(webView!=null){
                        if(TextUtils.isEmpty(urlString)==false){
                            webView.loadUrl(urlString);
                        }
                        else{
                            StringBuilder sb=new StringBuilder();
                            sb.append("<html>");
                            sb.append("<head>");
                            sb.append("<title>Welcome !</title>");
                            sb.append("</head>");
                            sb.append("<body>");

                            sb.append("<p>");
                            sb.append("The Bible (from Koine Greek τὰ βιβλία, tà biblía, \"the books\"[1]) is a collection of sacred texts or scriptures that Jews and Christians consider to be a product of divine inspiration and a record of the relationship between God and humans.\n" +
                                    "Many different authors contributed to the Bible. And what is regarded as canonical text differs depending on traditions and groups; a number of Bible canons have evolved, with overlapping and diverging contents.[2] The Christian Old Testament overlaps with the Hebrew Bible and the Greek Septuagint; the Hebrew Bible is known in Judaism as the Tanakh. The New Testament is a collection of writings by early Christians, believed to be mostly Jewish disciples of Christ, written in first-century Koine Greek. These early Christian Greek writings consist of narratives, letters, and apocalyptic writings. Among Christian denominations there is some disagreement about the contents of the canon, primarily the Apocrypha, a list of works that are regarded with varying levels of respect.\n" +
                                    "Attitudes towards the Bible also differ amongst Christian groups. Roman Catholics, Anglicans and Eastern Orthodox Christians stress the harmony and importance of the Bible and sacred tradition, while Protestant churches focus on the idea of sola scriptura, or scripture alone. This concept arose during the Protestant Reformation, and many denominations today support the use of the Bible as the only source of Christian teaching.\n" +
                                    "With estimated total sales of over 5 billion copies, the Bible is widely considered to be the best-selling book of all time.[3][4] It has estimated annual sales of 100 million copies,[5][6] and has been a major influence on literature and history, especially in the West where the Gutenberg Bible was the first mass-printed book. The Bible was the first book ever printed using movable type.");
                            sb.append("</p>");

                            sb.append("</body>");
                            sb.append("</html>");
                            webView.loadData(sb.toString(),"text/html","utf-8");
                        }
                    }
                    else{
                        Log.e(TAG,"Go ot error");
                    }
                    break;

                case R.id.font_size_ok:
                    Log.i(TAG,"Button ok has been clicked");

                    String fontSizeString=fontSize.getText().toString().trim();
                    if(fontSizeString!=null
                            &&TextUtils.isEmpty(fontSizeString)==false
                            &&webView!=null){
                        WebSettings settings=webView.getSettings();
                        settings.setTextZoom(Integer.parseInt(fontSizeString));
                    }
                    else{
                        Log.e(TAG,"zoom error");
                    }
                    break;

                default:

            }
        }
    }
}
