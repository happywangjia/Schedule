package com.whijj.schedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/8/14 0014.
 */
public class ScheduleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private TextView schedule;
    public final static int KEBIAO = 10002;
    public final static int WUKE=10003;
    public final static int QITA=10004;

    private Spinner xueNian = null;
    private ArrayAdapter<String> adapterNian = null;
    private static String[] xueNianInfo = null;
    private Spinner xueQi = null;
    private ArrayAdapter<String> adapterQi=null;
    private static String[] xueQiInfo=null;

    private Button findKeBiao = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shedule);
        schedule = (TextView) findViewById(R.id.scheduletv);
        xueNian = (Spinner) findViewById(R.id.xuenian);
        xueQi = (Spinner) findViewById(R.id.xueqi);
        findKeBiao = (Button) findViewById(R.id.findKebiao);
        findKeBiao.setOnClickListener(this);
        getHtml();
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEBIAO:
                    SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    Bundle bundle = (Bundle) msg.obj;
                    String kebiao = bundle.getString("kebiao");
                    editor.putString("toHtml",kebiao);
                    schedule.setText(kebiao);
                    String xuenian = bundle.getString("xuenian");
                    String xueqi = bundle.getString("xueqi");
                    editor.putString("selectedNian", xuenian);
                    editor.putString("selectedQi", xueqi);
                    editor.commit();


                    ArrayList<String> arrayNian = bundle.getStringArrayList("nian");
                    xueNianInfo = new String[arrayNian.size()];
                    for (int i = 0; i < arrayNian.size(); i++) {
                        xueNianInfo[i] = arrayNian.get(i);
                    }
                    adapterNian = new ArrayAdapter<String>(ScheduleActivity.this, android.R.layout.simple_spinner_dropdown_item, xueNianInfo);
                    xueNian.setAdapter(adapterNian);
                    xueNian.setOnItemSelectedListener(ScheduleActivity.this);

                    ArrayList<String> arrayQi=bundle.getStringArrayList("qi");
                    xueQiInfo=new String[arrayQi.size()];
                    for(int i=0;i<arrayQi.size();i++){
                        xueQiInfo[i]=arrayQi.get(i);
                    }
                    adapterQi=new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_spinner_dropdown_item,xueQiInfo);
                    xueQi.setAdapter(adapterQi);
                    xueQi.setOnItemSelectedListener(ScheduleActivity.this);

                    break;
                case WUKE:
                    Toast.makeText(ScheduleActivity.this,"无课程安排！",Toast.LENGTH_SHORT).show();
                    break;
                case QITA:
                    schedule.setText(msg.obj.toString());
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

    public void getHtml() {
        new Thread() {
            public void run() {
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
                String username = sharedPreferences.getString("username", "");
                String name = sharedPreferences.getString("name", "");
                String cookie = sharedPreferences.getString("loginCookie", "");
                Log.d("sss", "1:" + username + " 2: " + name + " 3: " + cookie);
                HttpResponse httpResponse;
                HttpClient httpClient = new DefaultHttpClient();
                URL go_url = null;
                try {
                    go_url = new URL("http://jw3.ahu.cn/xskbcx.aspx?xh=" + username + "&xm=" + java.net.URLEncoder.encode(name) + "&gnmkdm=N121603");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) go_url.openConnection();
                    String toUrl = go_url.toString();
                    String url = "http://jw3.ahu.cn/xs_main.aspx?xh=" + username;
                    HttpGet httpGet = new HttpGet(toUrl);
                    httpGet.addHeader("Cookie", "ASP.NET_SessionId=" + cookie);
                    httpGet.addHeader("Referer", url);
                    httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
                    httpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
                    httpResponse = httpClient.execute(httpGet);
                    int state = httpResponse.getStatusLine().getStatusCode();
                    if (state == 200) {
                        StringBuffer sb = new StringBuffer();
                        String data = "";
                        InputStream is = httpResponse.getEntity().getContent();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "gb2312"));
                        while ((data = br.readLine()) != null) {
                            sb.append(data);
                        }
                        String html = sb.toString();
                        if (null == html) return;
                        Document doc = Jsoup.parse(html);
                        Elements elements = doc.getElementsByAttributeValue("name", "__VIEWSTATE");
                        Element element = elements.first();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("viewState", element.attr("value").toString());
                        editor.commit();
                        Bundle kebiao = getKeBiao(html);
                        Message msg = new Message();
                        msg.what = KEBIAO;
                        msg.obj = kebiao;
                        handler.sendMessage(msg);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public Bundle getKeBiao(String html) {
        Bundle bundle = new Bundle();
        Document doc = Jsoup.parse(html);
        StringBuffer sb = new StringBuffer();
        Elements options = doc.getElementsByTag("option");
        ArrayList<String> arrayNian=new ArrayList<String>();
        ArrayList<String> arrayQi=new ArrayList<String>();


        for (int i = 0; i < options.size(); i++) {
            Element option = options.get(i);
            String str = option.text();
            if (str.length() > 1) {
                arrayNian.add(str);
            }else{
                arrayQi.add(str);
            }
            //sb.append(option.text()+"\n");
            if (option.hasAttr("selected")) {
                if (option.text().length() > 1) {
                    bundle.putString("xuenian", option.text());
                } else {
                    bundle.putString("xueqi", option.text());
                }
            }
        }
        bundle.putStringArrayList("nian", arrayNian);
        bundle.putStringArrayList("qi",arrayQi);
        Element table = doc.getElementById("Table1");
        Log.d("ssst", table.children().size() + "");
        Document doc1 = Jsoup.parse(table.toString());
        Elements elements = doc1.getElementsByTag("tr");
        for (int i = 0; i < elements.size(); i++) {
            Element ele = elements.get(i);
            for (int j = 0; j < ele.children().size(); j++) {
                //Log.d("ssst",ele.children().get(j).text());
                sb.append(ele.children().get(j).text() + " ");
            }
            sb.append("\n");
        }
        bundle.putString("kebiao", sb.toString());
        return bundle;

    }
    public String getKe(String html){
        Document doc=Jsoup.parse(html);
        Element table=doc.getElementById("Table1");
        Document doc1=Jsoup.parse(table.toString());
        Elements elements = doc1.getElementsByTag("tr");
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i < elements.size(); i++) {
            Element ele = elements.get(i);
            for (int j = 0; j < ele.children().size(); j++) {
                //Log.d("ssst",ele.children().get(j).text());
                sb.append(ele.children().get(j).text() + " ");
            }
            sb.append("\n");
        }
        //Log.d("ssschi",sb.toString());
        return sb.toString();
    }


    /**
     *
     * @param nianshu
     * @param qishu
     */
    public void getLove(final String nianshu, final String qishu) {
        new Thread() {
            public void run() {
                HttpResponse httpResponse;
                HttpClient httpClient = new DefaultHttpClient();

                //httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 200000);
                //httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 200000);

                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
                String username = sharedPreferences.getString("username", "");
                String name = sharedPreferences.getString("name", "");
                String viewState=sharedPreferences.getString("viewState","");
                //Log.d("sssvie",viewState);
                String cookie = sharedPreferences.getString("loginCookie", "");
                URL go_url=null;
                try {
                    go_url = new URL("http://jw3.ahu.cn/xskbcx.aspx?xh=" + username + "&xm=" + java.net.URLEncoder.encode(name) + "&gnmkdm=N121603");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) go_url.openConnection();
                    String toUrl = go_url.toString();
                    HttpPost httpPost=new HttpPost(toUrl);
                    //HttpPost httpPost = new HttpPost("http://jw3.ahu.cn/xskbcx.aspx?xh=E21314018&xm=%CD%F5%BC%D1&gnmkdm=N121603");
                    httpPost.addHeader("Cookie", "ASP.NET_SessionId=" + cookie);
                    httpPost.addHeader("Referer", toUrl);
                    httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
                    params.add(new BasicNameValuePair("__EVENTTARGET", "xqd"));
                    params.add(new BasicNameValuePair("xnd", nianshu));
                    params.add(new BasicNameValuePair("xqd", qishu));
                    params.add(new BasicNameValuePair("__VIEWSTATE", viewState));

                    httpPost.setEntity(new UrlEncodedFormEntity(params, "gb2312"));
                    httpResponse = httpClient.execute(httpPost);
                    int state = httpResponse.getStatusLine().getStatusCode();
                    if (state == 200) {
                        String wj = "";
                        InputStream jj = httpResponse.getEntity().getContent();
                        StringBuffer sb = new StringBuffer();
                        BufferedReader ws = new BufferedReader(new InputStreamReader(jj, "gb2312"));
                        while ((wj = ws.readLine()) != null) {
                            //Log.d("sss", wj);
                            sb.append(wj);
                        }
                        String html=sb.toString();
                        String kebiao = getKe(html);
                        Message msg = new Message();
                        msg.what = QITA;
                        msg.obj = kebiao;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {}
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.findKebiao:
                SharedPreferences sharedPreferences = getSharedPreferences("login", MODE_WORLD_READABLE);
                String selectedNian = sharedPreferences.getString("selectedNian", "");
                String selectedQi = sharedPreferences.getString("selectedQi", "");
                String selectingNian = xueNian.getSelectedItem().toString();
                String selectingQi = xueQi.getSelectedItem().toString();
                if (selectedNian.equals(selectingNian) && selectedQi.equals(selectingQi)) {
                    String keHtml=sharedPreferences.getString("toHtml","");
                    Message msg=new Message();
                    msg.what=QITA;
                    msg.obj=keHtml;
                    ScheduleActivity.this.handler.sendMessage(msg);
                    return;
                }
                getLove(selectingNian,selectingQi);
                break;
            default:
                break;


        }
    }
}
