package site.fermata.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import site.fermata.app.db.AppDatabase;
import site.fermata.app.db.TempSignal;

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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static site.fermata.app.Constants.SERVER_URL;

public class InsertActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_insert);
        setTitle("확진정보 확인절차안내");

        WebView mWebView = (WebView) findViewById(R.id.webView1);

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
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl("https://docs.google.com/document/d/e/2PACX-1vQ2-5qqHXDkMOcMu6NHGItEcsSE_SMZA_yC8XaCBGYpvMCrp8yDkOYJPV_Ny4XHkInD8bHUOssTzcPC/pub"); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

        SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        JSONObject jsonObject=new JSONObject();
        final TextInputEditText email = (TextInputEditText) findViewById(R.id.email);
        final TextInputEditText numstr = (TextInputEditText) findViewById(R.id.numstr);
        final TextInputEditText pnumstr = (TextInputEditText) findViewById(R.id.pnumstr);



        Single single=  HttpClient.login(prf)

                .flatMap(

                        new Function<String, SingleSource<String>>() {
                            @Override
                            public SingleSource<String> apply(String ss) throws Exception {

                                jsonObject.put("email", email.getText().toString());
                                jsonObject.put("pnumstr", pnumstr.getText().toString());

                                jsonObject.put("numstr", numstr.getText().toString());

                                List<String> list = AppDatabase
                                        .getInstance(getApplicationContext())
                                        .getTempSignalDao()
                                        .getAllSignal();


                                jsonObject.put("record",  new JSONArray(list));

                                jsonObject.put("sessionID",ss);

                                jsonObject.put("path","records/infection")
                                        .put("method","put");

                                Request request = new Request.Builder()
                                        .url(SERVER_URL)
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

                                    return Single.just("");
                                } else {

                                    if(code.equals("fail_auth")) {

                                        prf.edit().remove("session").apply();
                                    }

                                    return  HttpClient.login(prf).flatMap(this);

                                }

                            }
                        }

                ) .subscribeOn(Schedulers.io())


                .observeOn(AndroidSchedulers.mainThread());



        final Button button= (Button)  findViewById(R.id.insert_btn);

        button .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                disposables.add(single.subscribe(end->{


                    Toast.makeText(getApplicationContext(),"감사합니다.",
                            Toast.LENGTH_SHORT).show();

                   finish();

                }, e->{

                    Toast.makeText(getApplicationContext(),"오류가 있습니다.:"+e.toString(),
                            Toast.LENGTH_SHORT).show();
                    v.setEnabled(true);

                }));
            }
        });



        final CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkbeforinsert) ;

        View.OnClickListener clickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(checkBox1.isChecked());


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
