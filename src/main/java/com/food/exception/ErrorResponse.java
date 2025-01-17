package com.food.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ErrorResponse {
	
	private String message;
	
	private int code;
	
	private LocalDateTime timeStamp;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}

	public ErrorResponse(String message, int code, LocalDateTime timeStamp) {
		super();
		this.message = message;
		this.code = code;
		this.timeStamp = timeStamp;
	}
	
	

}
