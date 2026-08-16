package com.example.withdog.weather.infrastructure;

import com.example.withdog.weather.domain.WeatherInfo;

public interface WeatherClient {

    WeatherInfo getWeather(double lat, double lon);
}
