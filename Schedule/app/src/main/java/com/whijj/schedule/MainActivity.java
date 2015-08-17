package com.whijj.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button loginBt;
    private EditText username;
    private EditText password;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
        setContentView(R.layout.activity_main);
        init();
    }
    public void init(){
        loginBt = (Button) findViewById(R.id.loginBt);
        username = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);
        loginBt.setOnClickListener(this);
        SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
        username.setText(sharedPreferences.getString("username", ""));
        password.setText(sharedPreferences.getString("password", ""));

    }

    public Handler handler=new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case MyApplication.LOGIN:
                    Intent intent=new Intent(MainActivity.this,ItemsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case MyApplication.LOGINERROR:
                    SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("login",MODE_WORLD_READABLE);
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("password","");
                    Toast.makeText(MainActivity.this,"重新输入！",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBt:
                getCookie(username.getText().toString(), password.getText().toString());
                break;
            default:
                break;
        }
    }

    /**
     * 登录验证
     * @param username
     * @param password
     */
    public void getCookie(final String username, final String password) {
        new Thread() {
            public void run() {
                List<Cookie> cookies = null;
                HttpClient httpClient = new DefaultHttpClient();
                HttpResponse httpResponse;
                String url = "http://jw3.ahu.cn/default2.aspx";
                HttpPost httpPost = new HttpPost(url);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("txtUserName", username));
                params.add(new BasicNameValuePair("TextBox2", password));
                params.add(new BasicNameValuePair("RadioButtonList1", "学生"));
                params.add(new BasicNameValuePair("hidPdrs", ""));
                params.add(new BasicNameValuePair("hidsc", ""));
                params.add(new BasicNameValuePair("Button1", ""));
                params.add(new BasicNameValuePair("lbLanguage", ""));
                params.add(new BasicNameValuePair("__VIEWSTATE", "dDwyODE2NTM0OTg7Oz43V12xeO03cDNjW0o9HdZA42Lo6Q=="));
                //禁止使用重定向
                httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "gb2312"));
                    httpResponse = httpClient.execute(httpPost);
                    int status = httpResponse.getStatusLine().getStatusCode();
                    if (status == 200) {
                        StringBuffer stringBuffer = new StringBuffer();
                        HttpEntity httpEntity = httpResponse.getEntity();
                        InputStream is = httpEntity.getContent();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "gb2312"));
                        String data = "";
                        while ((data = br.readLine()) != null) {
                            stringBuffer.append(data);
                        }
                        String loginHtml = stringBuffer.toString();
                        if (null == loginHtml) {
                            Message msg=new Message();
                            msg.what=MyApplication.LOGINERROR;
                            MainActivity.this.handler.sendMessage(msg);
                            return;
                        }
                        Document doc = Jsoup.parse(loginHtml);
                        Elements eles = doc.getElementsByTag("title");
                        Element ele = eles.first();
                        if (!ele.text().toString().equals("正方教务管理系统")) {
                            Message msg=new Message();
                            msg.what=MyApplication.LOGINERROR;
                            MainActivity.this.handler.sendMessage(msg);
                            return;
                        }
                        Element nameEle = doc.getElementById("xhxm");
                        String name = nameEle.text().toString();
                        name = name.substring(0, name.length() - 2);
                        Log.d("sss", name);
                        cookies = ((AbstractHttpClient) httpClient).getCookieStore().getCookies();
                        String cookie = cookies.get(0).getValue().toString();
                        Log.d("ssscookie1",cookie);
                        SharedPreferences sharedPreferences=MyApplication.
                                getContext().getSharedPreferences("login", Context.MODE_WORLD_READABLE);
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("name", name);
                        editor.putString("loginCookie", cookie);
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.putBoolean("loginIn", true);
                        editor.commit();
                        Message msg=new Message();
                        msg.what=MyApplication.LOGIN;
                        MainActivity.this.handler.sendMessage(msg);
                    } else {
                        Message msg=new Message();
                        msg.what=MyApplication.LOGINERROR;
                        MainActivity.this.handler.sendMessage(msg);
                        return;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }


}
