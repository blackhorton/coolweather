package com.example.ming.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ming.coolweather.gson.Forecast;
import com.example.ming.coolweather.gson.Weather;
import com.example.ming.coolweather.util.HttpUtil;
import com.example.ming.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private TextView titleCityTv;

    private TextView titleUpdateTimeTv;

    private TextView degreeTv;

    private TextView weatherInfoTv;

    private LinearLayout forecastLayout;

    private TextView aqiTv;

    private TextView pm25Tv;

    private TextView comfortTv;

    private TextView carWashTv;

    private TextView sportTv;

    private ImageView bingPicIv;

    public SwipeRefreshLayout swipeRefresh;

    public DrawerLayout drawerLayout;

    private Button navHomeBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.module_activity_weather);
        // 初始化各控件
        weatherLayout = findViewById(R.id.sv_weather_layout);
        titleCityTv = findViewById(R.id.tv_title_city);
        titleUpdateTimeTv = findViewById(R.id.tv_title_update_time);
        degreeTv = findViewById(R.id.tv_degree);
        weatherInfoTv = findViewById(R.id.tv_weather_info);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiTv = findViewById(R.id.tv_aqi);
        pm25Tv = findViewById(R.id.tv_pm25);
        comfortTv = findViewById(R.id.tv_comfort);
        carWashTv = findViewById(R.id.tv_car_wash);
        sportTv = findViewById(R.id.tv_sport);
        bingPicIv = findViewById(R.id.iv_bing_pic);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = findViewById(R.id.drawer_layout);
        navHomeBtn = findViewById(R.id.btn_nav_home);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;

        if(weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicIv);
        } else {
            loadBingPic();
        }

        navHomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {

//        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
//                weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";

        String weatherUrl = "http://guolin.tech/api/weather?cityid=" +
                weatherId + "&key=8aa2a50146ac4d32a34212f8c74c907c";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,
                                "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.CityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;

        titleCityTv.setText(cityName);
        titleUpdateTimeTv.setText(updateTime);
        degreeTv.setText(degree);
        weatherInfoTv.setText(weatherInfo);

        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateTv = view.findViewById(R.id.tv_date);
            TextView infoTv = view.findViewById(R.id.tv_info);
            TextView maxTv = view.findViewById(R.id.tv_max);
            TextView minTv = view.findViewById(R.id.tv_min);

            dateTv.setText(forecast.date);
            infoTv.setText(forecast.more.info);
            maxTv.setText(forecast.temperature.max);
            minTv.setText(forecast.temperature.min);

            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqiTv.setText(weather.aqi.city.aqi);
            pm25Tv.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动指数：" + weather.suggestion.sport.info;

        comfortTv.setText(comfort);
        carWashTv.setText(carWash);
        sportTv.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicIv);
                    }
                });
            }
        });
    }
}
