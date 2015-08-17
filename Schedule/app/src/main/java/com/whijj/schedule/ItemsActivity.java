package com.whijj.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/8/14 0014.
 */
public class ItemsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView name;
    private Button scheduleBt;
    private Button scoreBt;
    private Button loginOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        init();

    }
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

    public void init(){
        name= (TextView) findViewById(R.id.name);
        scheduleBt= (Button) findViewById(R.id.schedule);
        scoreBt= (Button) findViewById(R.id.achievement);
        loginOut= (Button) findViewById(R.id.loginOut);
        scheduleBt.setOnClickListener(this);
        scoreBt.setOnClickListener(this);
        loginOut.setOnClickListener(this);
        SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_WORLD_READABLE);
        name.setText(sharedPreferences.getString("name", ""));
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.schedule:
                Intent intent1=new Intent(ItemsActivity.this,ScheduleActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent1);
                break;
            case R.id.achievement:
                break;
            case R.id.loginOut:
                SharedPreferences sharedPreferences=getSharedPreferences("login",MODE_WORLD_READABLE);
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("loginIn",false);
                editor.commit();
                Intent intent3=new Intent(ItemsActivity.this,MainActivity.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent3);
                finish();
                break;
            default:
                break;

        }
    }
}
