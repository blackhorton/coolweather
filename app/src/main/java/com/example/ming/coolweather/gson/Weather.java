package com.example.ming.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    //daily包含一个数组，因此使用List集合来引用Forecast类
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
