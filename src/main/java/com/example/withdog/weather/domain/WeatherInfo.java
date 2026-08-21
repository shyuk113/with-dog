package com.example.withdog.weather.domain;

public record WeatherInfo (Condition condition,double temperature){

    public static WeatherInfo unknown(){
        return new WeatherInfo(Condition.CLEAR,0.0);
    }
}
