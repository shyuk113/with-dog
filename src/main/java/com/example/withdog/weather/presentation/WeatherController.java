package com.example.withdog.weather.presentation;

import com.example.withdog.weather.application.WeatherService;
import com.example.withdog.weather.domain.WeatherInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<WeatherInfo> getWeather(@RequestParam double lan, @RequestParam double lon){
        return ResponseEntity.ok(weatherService.getWeather(lan,lon));
    }
}
