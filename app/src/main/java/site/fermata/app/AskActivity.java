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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
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
import site.fermata.app.db.SignalLog;

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


                                    JSONArray jlist = json.getJSONArray("contacts");

                                    JSONArray array = new JSONArray(jlist);
                                    if(array.length()==0) {
                                        return Single.just("근접 기록이 없습니다.");
                                    }
                                    String[] arr=new String[array.length()];
                                    for(int i=0; i<arr.length; i++) {
                                        arr[i]=array.optString(i);
                                    }

                                    SignalLog[] jjj = AppDatabase.getInstance(getApplicationContext())
                                            .getSignalLogDao().find(arr);

                                    if(jjj.length==0) {
                                        return Single.just("근접 기록이 없습니다.");
                                    }

                                    String htmllist="<table>\n" +
                                            "  <tr>\n" +
                                            "    <th>날짜</th>\n" +
                                            "    <th>신호연결시간</th>\n" +
                                            "    <th>평균신호세기(RSSI)</th>\n" +
                                            "  </tr>";

                                    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
                                    Calendar cal = Calendar.getInstance();
                                    TimeZone tz = cal.getTimeZone();
                                    formatter.setTimeZone(tz);
                                    SignalLog log;
                                    String time;
                                    float span;
                                    for(int i=0; i<jjj.length; i++) {
                                          log  = jjj[i];
                                         time = formatter.format(new Date(log.timestamp*1000));
                                        span =Math.round (( (float) log.timespan)/ ((float) 60)*100 )/100;
                                          htmllist+="<tr>\n" +
                                                "    <td>"+time +"</td>\n" +
                                                "    <td>"+ span +"분간</td>\n" +
                                                "    <td>"+  log.rssi +"db</td>\n" +
                                                "  </tr>";
                                    }





                                    return Single.just(htmllist );
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
                mWebView.loadDataWithBaseURL(null, "<html><h2 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">확인중입니다..</h2></html>", "text/html", "utf-8", null);

                disposables.add(single.subscribe(end -> {

                 //   mWebView .loadData("<h2 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">여기에 결과를 표시합니다.여기는 html로 렌더링..</h2>", "text/html", "UTF-8");



                    mWebView.loadDataWithBaseURL(null, "<html><h2 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">조회결과</h2>"+ end+"<h3 >이 정보는 확진자와 물리적인 근접사실에 대한 것으로 감염여부를 결정하는 정보가 아닙니다." +
                            "마스크착용 여부나 환기여부, 현재 증상등을 고려하여 검사나 자가휴식등의 의사결정하시기 바랍니다.  </h3></html>", "text/html", "utf-8", null);


                    button.setEnabled(true);

                }, e -> {

                  //  mWebView .loadData("<h2 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">오류가 있습니다. 잠시후 다시 시도해주세요.</h2>", "text/html", "UTF-8");

                    mWebView.loadDataWithBaseURL(null, "<html><h3 style=\"padding-top:40px;color: #5e9ca0; text-align: center;\">조회를 하지 못했습니다. 잠시 후 다시 시작해주세요.</h3></html>", "text/html", "utf-8", null);

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

                Toast.makeText(getApplicationContext(),"이 기능 곧 구현예정입니다.",
                        Toast.LENGTH_SHORT).show();


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
