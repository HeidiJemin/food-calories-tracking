package com.food.response.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodCaloriesResponseDto {

	private UserResponseDto user;

	private float calories;

	public UserResponseDto getUser() {
		return user;
	}

	public void setUser(UserResponseDto user) {
		this.user = user;
	}

	public float getCalories() {
		return calories;
	}

	public void setCalories(float calories) {
		this.calories = calories;
	}

	public FoodCaloriesResponseDto(UserResponseDto user, float calories) {
		super();
		this.user = user;
		this.calories = calories;
	}

	public FoodCaloriesResponseDto() {
		super();
	}

	

}
