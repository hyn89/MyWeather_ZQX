package com.smart.hyn.myweather;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.hyn.myweather.com.smart.hyn.bean.TodayWeather;
import com.smart.hyn.myweather.util.NetUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final String CITY_NAME_KEY = "city_name";
    private static final String CITY_CODE_KEY = "main_city_code";
    private ImageView mUpdateBtn;
    private ImageView mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv;
    private ImageView weatherImg, pmImg;
    private ProgressBar titleUpdateProgress;
    private String currCityCode;
    private String currCityName;

    private FutureWeatherAdapter fwAdapter;
    private ViewPager futureWeathers;
    private List<View> fwViews;

    private SharedPreferences prefGuid;
    private SharedPreferences.Editor prefGuidEditor;
    private SharedPreferences prefConfig;
    private SharedPreferences.Editor prefConfigEditor;

    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView)findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mCitySelect= (ImageView)findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);
        titleUpdateProgress = (ProgressBar)findViewById(R.id.progress_bar);

        if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
            Log.d("myWeather", "Net OK");
            Toast.makeText(MainActivity.this, getString(R.string.net_ok), Toast.LENGTH_LONG).show();
        }else{
            Log.d("myWeather","Net Fail");
            Toast.makeText(MainActivity.this, getString(R.string.net_fail), Toast.LENGTH_LONG).show();
        }

        initView();
        showGuid();

        prefConfig = getSharedPreferences("config", MODE_PRIVATE);
        currCityCode = prefConfig.getString(CITY_CODE_KEY, "101010100");
        currCityName = "北京";
        Toast.makeText(this, currCityCode, Toast.LENGTH_LONG).show();

        updateDataFromNet();
    }

    private void initView(){
        city_name_Tv = (TextView)findViewById(R.id.title_city_name);
        cityTv = (TextView)findViewById(R.id.city);
        timeTv = (TextView)findViewById(R.id.time);
        humidityTv = (TextView)findViewById(R.id.humidity);
        weekTv = (TextView)findViewById(R.id.week_today);
        pmDataTv = (TextView)findViewById(R.id.pm_data);
        pmQualityTv = (TextView)findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView)findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView)findViewById(R.id.temperature);
        climateTv = (TextView)findViewById(R.id.climate);
        windTv = (TextView)findViewById(R.id.wind);
        weatherImg = (ImageView)findViewById(R.id.weather_img);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        weekTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");

        LayoutInflater inflater = LayoutInflater.from(this);
        fwViews = new ArrayList<View>();
        fwViews.add(inflater.inflate(R.layout.future_weathers_1,null));
        fwViews.add(inflater.inflate(R.layout.future_weathers_2,null));
        fwAdapter = new FutureWeatherAdapter(fwViews, this);
        futureWeathers = (ViewPager)findViewById(R.id.future_weathers);
        futureWeathers.setAdapter(fwAdapter);
    }

    private void showGuid(){
        prefGuid = getSharedPreferences("guid", MODE_PRIVATE);
        if(prefGuid.getBoolean("firstLaunch", false)){
            prefGuidEditor = prefGuid.edit();
            prefGuidEditor.putBoolean("firstLaunch", true);
            prefGuidEditor.commit();
            Intent i = new Intent(this, Guid.class);
            startActivity(i);
        }
    }

    private void queryWeatherCode(String cityCode){
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myWeater", address);
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;
                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);
                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while((str = reader.readLine()) != null){
                        response.append(str);
                        Log.d("myWeather", str);
                    }
                    String responseStr = response.toString();
                    Log.d("myWeather", responseStr);

                    todayWeather = parseXML(responseStr);
                    if(todayWeather != null){
                        Log.d("myWeather", todayWeather.toString());

                        currCityName = todayWeather.getCity();

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(con != null){
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata){
        TodayWeather todayWeather = null;
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try{
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));
            int eventType = xmlPullParser.getEventType();
            Log.d("myWWheater","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if(xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            if(xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdateTime(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            }else if(xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            }else if(xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            }else if(xmlPullParser.getName().equals("date") && dateCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            }else if(xmlPullParser.getName().equals("high") && highCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            }else if(xmlPullParser.getName().equals("low") && lowCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            }else if(xmlPullParser.getName().equals("type") && typeCount == 0){
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return todayWeather;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateClimateImage(){
        switch (climateTv.toString()){
            case "晴":
                weatherImg.setImageDrawable(getDrawable(R.drawable.biz_plugin_weather_qing));
                break;
            case "暴雪":
                weatherImg.setImageDrawable(getDrawable(R.drawable.biz_plugin_weather_baoxue));
                break;
            default:
                weatherImg.setImageDrawable(getDrawable(R.drawable.biz_plugin_weather_qing));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){
            currCityCode = data.getStringExtra("cityCode");
            Log.d("myWeather", "当前选择的城市代码：" + currCityCode);

            if(NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE){
                Log.d("myWeather", "Net is OK");
                queryWeatherCode(currCityCode);
            }else{
                Log.d("myWeather", "Net is Failed");
                Toast.makeText(MainActivity.this, getString(R.string.net_fail), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void updateTodayWeather(TodayWeather todayWeather){
        city_name_Tv.setText(todayWeather.getCity() + getString(R.string.wheather) );
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdateTime() + getString(R.string.publish));
        humidityTv.setText(getString(R.string.humidity) + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText(getString(R.string.wind) + todayWeather.getFengli());
        Toast.makeText(MainActivity.this, getString(R.string.updateSuccess), Toast.LENGTH_SHORT).show();

        updateClimateImage();
        mUpdateBtn.setVisibility(View.VISIBLE);
        titleUpdateProgress.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_city_manager){
            Intent i = new Intent(this, SelectCity.class);
            Bundle bundle = new Bundle();
            bundle.putString("city_name", currCityName);
            bundle.putString("city_code", currCityCode);
            i.putExtras(bundle);
//            startActivity(i);
            startActivityForResult(i,1);
        }

        if (view.getId() == R.id.title_update_btn) {
            //change view visibility
            mUpdateBtn.setVisibility(View.INVISIBLE);
            titleUpdateProgress.setVisibility(View.VISIBLE);
            //update function
            updateDataFromNet();
        }
    }

    private void updateDataFromNet(){
        Log.d("myWeather", currCityCode);

        if (NetUtil.getNetworkState(this) != NetUtil.NETWORN_NONE) {
            Log.d("myWeather", "Net OK");
            queryWeatherCode(currCityCode);
//                Toast.makeText(MainActivity.this, "Net OK", Toast.LENGTH_LONG).show();
        } else {
            Log.d("myWeather", "Net Fail");
            Toast.makeText(MainActivity.this, "Net Fail", Toast.LENGTH_LONG).show();
        }
    }

}
