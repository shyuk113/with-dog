package com.example.withdog.weather.infrastructure;

import com.example.withdog.weather.domain.Condition;
import com.example.withdog.weather.domain.GridConverter;
import com.example.withdog.weather.domain.WeatherInfo;
import com.example.withdog.weather.infrastructure.dto.KmaResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KmaWeatherClient implements WeatherClient {

    private static final int[] BASE_HOURS = {2,5,8,11,14,17,20,23};

    private final WebClient weatherWebClient;

    @Value("${weather.service-key}")
    private String serviceKey;

    @Override
    public WeatherInfo getWeather(double lat, double lon){
        GridConverter.Grid grid = GridConverter.toGrid(lat,lon);
        String[] baseDateTime = resolveBaseDateTime(LocalDateTime.now());

        try{
            KmaResponse response = weatherWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/getVilageFcst")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("dataType", "JSON")
                    .queryParam("numOfRows", 100)
                    .queryParam("base_date",baseDateTime[0])
                    .queryParam("base_time", baseDateTime[1])
                    .queryParam("nx", grid.nx())
                    .queryParam("ny", grid.ny())
                    .build())
                    .retrieve()
                    .bodyToMono(KmaResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            if(response == null){
                return WeatherInfo.unknown();
            }

            List<KmaResponse.Item> items = response.response().body().items().item();

            String ptyCode = items.stream()
                    .filter(i-> "PTY".equals(i.category()))
                    .findFirst()
                    .map(KmaResponse.Item::fcstValue)
                    .orElse("0");

            Condition condition;
            if(!"0".equals(ptyCode)) {
                condition = switch (ptyCode) {
                    case "1" -> Condition.RAIN;
                    case "2" -> Condition.RAIN_SNOW;
                    case "3" -> Condition.SNOW;
                    case "4" -> Condition.SHOWER;
                    default -> Condition.CLEAR;
                };
            } else {
                String skyCode = items.stream()
                        .filter(i->"SKY".equals(i.category()))
                        .findFirst()
                        .map(KmaResponse.Item::fcstValue)
                        .orElse("1");
                condition ="1".equals(skyCode) ? Condition.CLEAR : Condition.CLOUDY;
            }

            double temp = items.stream()
                    .filter(i-> "TMP".equals(i.category()))
                    .findFirst()
                    .map(i-> Double.parseDouble(i.fcstValue()))
                    .orElse(0.0);

            return new WeatherInfo(condition, temp);
        } catch (Exception e){
            log.error("날씨 조회 실패:{}", e.getMessage(), e);
            return WeatherInfo.unknown();
        }
    }

    private String[] resolveBaseDateTime(LocalDateTime now){
        LocalDateTime target = now.minusMinutes(10);
        int hour = target.getHour();
        int baseHour = BASE_HOURS[0];

        for(int h : BASE_HOURS){
            if(h <= hour){
                baseHour = h;
            }
        }

        LocalDate date = target.toLocalDate();
        if(hour<BASE_HOURS[0]) {
            date = date.minusDays(1);
        }

        return new String[]{date.format(DateTimeFormatter.BASIC_ISO_DATE), String.format("%02d00", baseHour)};
    }

}
