package com.example.withdog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
public class WithdogApplication {

	public static void main(String[] args) {
		//LocalDateTime.now()와 DB에 저장되는 시각의 기준을 서버/컨테이너 시간대(보통 UTC)와 무관하게 KST로 고정
		//다른 클래스가 기본 시간대를 읽어가기 전이어야 하므로 run()보다 먼저 호출한다
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(WithdogApplication.class, args);
	}

}
