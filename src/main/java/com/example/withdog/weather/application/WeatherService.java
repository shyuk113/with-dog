package com.example.withdog.weather.application;

import com.example.withdog.weather.domain.WeatherInfo;
import com.example.withdog.weather.infrastructure.WeatherClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;

    public WeatherInfo getWeather(double lat, double lon){
        return weatherClient.getWeather(lat,lon);
    }
}
