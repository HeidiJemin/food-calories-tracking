package com.food.response.payload;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyFoodReportResponseDto {

	private String month;

	private LocalDate date;
	
	private String day;

	private long totalCalories;

	private boolean isCaloriesLimitExceeded;

	private boolean isMonthlyPriceLimitExceed;

	private double totalAmount;

	private List<UserDetail> userDetails;
	

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public long getTotalCalories() {
		return totalCalories;
	}

	public void setTotalCalories(long totalCalories) {
		this.totalCalories = totalCalories;
	}

	public boolean isCaloriesLimitExceeded() {
		return isCaloriesLimitExceeded;
	}

	public void setCaloriesLimitExceeded(boolean isCaloriesLimitExceeded) {
		this.isCaloriesLimitExceeded = isCaloriesLimitExceeded;
	}

	public boolean isMonthlyPriceLimitExceed() {
		return isMonthlyPriceLimitExceed;
	}

	public void setMonthlyPriceLimitExceed(boolean isMonthlyPriceLimitExceed) {
		this.isMonthlyPriceLimitExceed = isMonthlyPriceLimitExceed;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<UserDetail> getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(List<UserDetail> userDetails) {
		this.userDetails = userDetails;
	}

	@Getter
	@Setter
	public static class UserDetail {

		private UserResponseDto userResponse;

		private List<FoodResponse> foodResponses;

		public UserResponseDto getUserResponse() {
			return userResponse;
		}

		public void setUserResponse(UserResponseDto userResponse) {
			this.userResponse = userResponse;
		}

		public List<FoodResponse> getFoodResponses() {
			return foodResponses;
		}

		public void setFoodResponses(List<FoodResponse> foodResponses) {
			this.foodResponses = foodResponses;
		}
		

	}

	@Getter
	@Setter
	public static class FoodResponse {

		private Long id;

		private String foodName;

		private int calorieCount;

		private float price;

		private LocalTime consumptionTime;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getFoodName() {
			return foodName;
		}

		public void setFoodName(String foodName) {
			this.foodName = foodName;
		}

		public int getCalorieCount() {
			return calorieCount;
		}

		public void setCalorieCount(int calorieCount) {
			this.calorieCount = calorieCount;
		}

		public float getPrice() {
			return price;
		}

		public void setPrice(float price) {
			this.price = price;
		}

		public LocalTime getConsumptionTime() {
			return consumptionTime;
		}

		public void setConsumptionTime(LocalTime consumptionTime) {
			this.consumptionTime = consumptionTime;
		}
		

	}

}
