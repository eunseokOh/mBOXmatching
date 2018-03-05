package com.manager.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MatchingManagerApplication {
	private final int MAX_CONNECTION_USER_CNT = 15; 
	public static void main(String[] args) {
		SpringApplication.run(MatchingManagerApplication.class, args);
	}
	public int getMAX_CONNECTION_USER_CNT() {
		return MAX_CONNECTION_USER_CNT;
	}
	
	
}
