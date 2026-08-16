package com.example.withdog.weather.domain;

public record WeatherInfo (boolean isRaining, double temperature, String condition){

    public static WeatherInfo unknown(){
        return new WeatherInfo(false, 0.0, "UNKNOWN");
    }
}
