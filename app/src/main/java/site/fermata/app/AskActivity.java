package site.fermata.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.fermata.app.db.AppDatabase;

import static site.fermata.app.Constants.SERVER_URL;

public class AskActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_query);
        setTitle("접촉정보 조회");

        WebView mWebView = (WebView) findViewById(R.id.webViewquery);

        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        WebSettings mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(false); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(false); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 9; SM-G960U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.116 Mobile Safari/537.36");
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT  ); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부
       // mWebView .loadData(, "text/html", "UTF-8");

       // mWebView.loadDataWithBaseURL(null, "<html><h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">확인중입니다..</h1></html>", "text/html", "utf-8", null);
        //mWebView.loadUrl("https://google.com"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
        SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        JSONObject jsonObject=new JSONObject();





        Single single=  HttpClient.login(prf)

                .flatMap(

                        new Function<String, SingleSource<String>>() {
                            @Override
                            public SingleSource<String> apply(String ss) throws Exception {

                                List<String> list = AppDatabase
                                        .getInstance(getApplicationContext())
                                        .getTempSignalDao()
                                        .getAllSignal();


                                jsonObject.put("myID",  prf.getString("id",""));

                                jsonObject.put("sessionID",ss);
                                Request request = new Request.Builder()
                                        .url(SERVER_URL+"/api/records")
                                        //.get()
                                        .post(RequestBody.create( jsonObject.toString(), MediaType.parse("application/json")))
                                        .build();
                                if(BuildConfig.DEBUG){
                                    Log.d("d", jsonObject.toString());

                                }

                                Response responses = HttpClient.get().newCall(request).execute();
                                if(responses.code()!=200){
                                    return Single.error( new Exception("ServerError"));
                                }

                                String jsonData = responses.body().string();

                                JSONObject json = new JSONObject(jsonData);
                                String code= json.getString("code");
                                if(code.equals("success")) {

                                    String session= json.getString("sessionID");

                                    prf.edit().putString("session",session).apply();

                                    return Single.just(jsonData );
                                } else {
                                    return  Single.timer(3, TimeUnit.SECONDS).flatMap(s->HttpClient.login(prf))  . flatMap(this);

                                }

                            }
                        }

                ) .subscribeOn(Schedulers.io())


                .observeOn(AndroidSchedulers.mainThread());



        final Button button= (Button)  findViewById(R.id.ask_btn);

        View.OnClickListener ask = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
              //mWebView .loadData("<h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">확인중입니다.</h1>", "text/html", "UTF-8");
                mWebView.loadDataWithBaseURL(null, "<html><h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">확인중입니다..</h1></html>", "text/html", "utf-8", null);

                disposables.add(single.subscribe(end -> {

                 //   mWebView .loadData("<h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">여기에 결과를 표시합니다.여기는 html로 렌더링..</h1>", "text/html", "UTF-8");

                    mWebView.loadDataWithBaseURL(null, "<html><h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">여기에결과를 표시할예정.html로."+ end+"</h1></html>", "text/html", "utf-8", null);


                    button.setEnabled(true);

                }, e -> {

                  //  mWebView .loadData("<h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">오류가 있습니다. 잠시후 다시 시도해주세요.</h1>", "text/html", "UTF-8");

                    mWebView.loadDataWithBaseURL(null, "<html><h1 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">조회를 하지 못했습니다. 잠시 후 다시 시작해주세요.</h1></html>", "text/html", "utf-8", null);

                    button.setEnabled(true);

                }));
            }
        };
        button .setOnClickListener(ask);

        ask.onClick(null);

        final CheckBox checkBox1 = (CheckBox) findViewById(R.id.autoask) ;

        View.OnClickListener clickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        };

        checkBox1.setOnClickListener(clickListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
