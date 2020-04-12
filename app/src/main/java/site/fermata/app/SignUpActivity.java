package site.fermata.app;

import androidx.preference.PreferenceManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

import static site.fermata.app.Constants.PREF_ID;
import static site.fermata.app.Constants.SERVER_URL;
import static site.fermata.app.PasswordGenerator.generateRandomPassword;

public class SignUpActivity extends Activity {


    public static final String MAINURL = "https://docs.google.com/document/d/e/2PACX-1vTqkxxRmJ-EIUzQb533qR_n_pDVLizbuUpfUz3UCuDv4DhAIPdy8eIIaXUa06KFnyUakha3ViFIKQdz/pub";
    private BluetoothAdapter mBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle(R.string.activity_main_title);




        WebView mWebView = (WebView) findViewById(R.id.webView);

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

        mWebView.loadUrl(MAINURL); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작


        OkHttpClient client =HttpClient.get();




        final CheckBox checkBox2 = (CheckBox) findViewById(R.id.checkBox2) ;


        View.OnClickListener checklisten = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView b = (TextView) v;
                if (b.getText().equals("보기")) {
                    b.setText("숨기기");

                   mWebView.loadUrl(
                           b.getId()==R.id.privacy?
                           "https://docs.google.com/document/d/e/2PACX-1vQqsxIOuXVMMaQzqiRIYKjfJ15z4eUySQSufKVqLB6ZamYLMDOuI2-iftSHXoFHFTU0C2s1eceyuQAn/pub":
                                   "https://docs.google.com/document/d/e/2PACX-1vSM0Ani63nLRNgG8Ov_kk8L4Iw4-c2sdlevPitt5VSpPicmJekw7VJcc6i7F6F5mWNQbobUrnxJCIN2/pub"
                           ); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작
                    mWebView.loadUrl(MAINURL); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

                    b.setText("보기");

                }


            }
        };
        findViewById(R.id.privacy).setOnClickListener(checklisten);
        findViewById(R.id.terms).setOnClickListener(checklisten);
        final Button button= (Button) findViewById(R.id.start_button);
        final CheckBox checkBox1 = (CheckBox) findViewById(R.id.checkBox1) ;

        View.OnClickListener clickListener= new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(checkBox1.isChecked() && checkBox2.isChecked());


            }
        };

        checkBox1.setOnClickListener(clickListener);
        checkBox2.setOnClickListener(clickListener);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                button.setText("잠시 기다려주세요.");
            final     String id= UUID.randomUUID().toString();
              //  UUID.randomUUID().toString();
             final   String key= generateRandomPassword(15) ;

                try{

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("path","user")
                            .put("method","put")
                            .put("id",  id)
                            .put("password", key);


                    Request request = new Request.Builder()
                            //.addHeader("x-api-key", RestTestCommon.API_KEY)
                           .url(SERVER_URL)
                            //.get()
                            .post(RequestBody.create( jsonObject.toString(),MediaType.parse("application/json")  ))
                            .build();

                    //비동기 처리 (enqueue 사용)
                    client.newCall(request).enqueue(new Callback() {
                        //비동기 처리를 위해 Callback 구현
                        @Override
                        public void onFailure(Call call, IOException e) {

                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   button.setEnabled(true);
                                   Toast.makeText(SignUpActivity.this,"네트워크연결 상태를 확인후,다시 시도해주세요"+e.toString(),Toast.LENGTH_LONG).show();

                               }
                           });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {





                            SharedPreferences prf = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            prf.edit().putString(PREF_ID,id).putString("key",key).putString("session","dsfdsdfs").commit();
                            Intent i = new Intent( SignUpActivity.this, MainActivity.class);
                            finish();  //Kill the activity from which you will go to next activity
                            startActivity(i);

                        }
                    });

                } catch (Exception e) {
                   Toast.makeText(SignUpActivity.this,"네트워크연결 상태를 확인후,다시 시도해주세요",Toast.LENGTH_LONG).show();
                }





            }
        });


    }


}
