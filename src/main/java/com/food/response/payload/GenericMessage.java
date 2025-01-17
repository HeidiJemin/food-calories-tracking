package com.food.response.payload;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class GenericMessage {

	private String message;

	private LocalDateTime timeStamp;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(LocalDateTime timeStamp) {
		this.timeStamp = timeStamp;
	}

	public GenericMessage(String message, LocalDateTime timeStamp) {
		super();
		this.message = message;
		this.timeStamp = timeStamp;
	}
	

}
