package com.food.response.payload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodResponseDto {

    private long totalCaloriesConsumedInMonth;
    private double totalAmountSpentInMonth;
    private boolean isMonthlyAmountLimitReached;
    private List<DailyFoodConsumption> dailyFoodConsumptions;
    
    public long getTotalCaloriesConsumedInMonth() {
		return totalCaloriesConsumedInMonth;
	}

	public void setTotalCaloriesConsumedInMonth(long totalCaloriesConsumedInMonth) {
		this.totalCaloriesConsumedInMonth = totalCaloriesConsumedInMonth;
	}

	public double getTotalAmountSpentInMonth() {
		return totalAmountSpentInMonth;
	}

	public void setTotalAmountSpentInMonth(double totalAmountSpentInMonth) {
		this.totalAmountSpentInMonth = totalAmountSpentInMonth;
	}

	public boolean isMonthlyAmountLimitReached() {
		return isMonthlyAmountLimitReached;
	}

	public void setMonthlyAmountLimitReached(boolean isMonthlyAmountLimitReached) {
		this.isMonthlyAmountLimitReached = isMonthlyAmountLimitReached;
	}

	public List<DailyFoodConsumption> getDailyFoodConsumptions() {
		return dailyFoodConsumptions;
	}

	public void setDailyFoodConsumptions(List<DailyFoodConsumption> dailyFoodConsumptions) {
		this.dailyFoodConsumptions = dailyFoodConsumptions;
	}

	@Getter
    @Setter
    public static class DailyFoodConsumption {
        private LocalDate date;
        private long totalCalories;
        private double totalAmount;
        private boolean isCaloriesLimitExceeded;
        private List<DailyFoodResponse> dailyFoodResponses;
		public LocalDate getDate() {
			return date;
		}
		public void setDate(LocalDate date) {
			this.date = date;
		}
		public long getTotalCalories() {
			return totalCalories;
		}
		public void setTotalCalories(long totalCalories) {
			this.totalCalories = totalCalories;
		}
		public double getTotalAmount() {
			return totalAmount;
		}
		public void setTotalAmount(double totalAmount) {
			this.totalAmount = totalAmount;
		}
		public boolean isCaloriesLimitExceeded() {
			return isCaloriesLimitExceeded;
		}
		public void setCaloriesLimitExceeded(boolean isCaloriesLimitExceeded) {
			this.isCaloriesLimitExceeded = isCaloriesLimitExceeded;
		}
		public List<DailyFoodResponse> getDailyFoodResponses() {
			return dailyFoodResponses;
		}
		public void setDailyFoodResponses(List<DailyFoodResponse> dailyFoodResponses) {
			this.dailyFoodResponses = dailyFoodResponses;
		}
        
    }

    @Getter
    @Setter
    public static class DailyFoodResponse {
        private Long foodId;
        private String foodName;
        private int calorieCount;
        private float price;
        private LocalTime consumptionTime;
        private LocalDateTime createdAt;
        private UserResponseDto userDetails;
		public Long getFoodId() {
			return foodId;
		}
		public void setFoodId(Long foodId) {
			this.foodId = foodId;
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
		public LocalDateTime getCreatedAt() {
			return createdAt;
		}
		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}
		public UserResponseDto getUserDetails() {
			return userDetails;
		}
		public void setUserDetails(UserResponseDto userDetails) {
			this.userDetails = userDetails;
		}
        
    }
}