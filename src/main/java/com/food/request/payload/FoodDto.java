package com.food.request.payload;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodDto {

	@NotBlank(message = "Food name cannot be blank")
	private String name;

	@NotNull(message = "Calories cannot be null")
	@Positive(message = "Calories must be a positive number")
	private Integer calories;

	@NotNull(message = "Price cannot be null")
	@Positive(message = "Price must be a positive number")
	private Float price;

	@NotNull(message = "Date cannot be null")
	private LocalDate date;

	@NotNull(message = "Time cannot be null")
	private LocalTime time;

	@NotNull(message = "User ID cannot be null")
	private Long userId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	

}
