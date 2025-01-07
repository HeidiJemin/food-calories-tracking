package com.food.response.payload;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodConsumptionDto {

    private String month;
    
    private int year;
    
    private long totalCaloriesConsumedInMonth;
    
    private double totalAmountSpentInMonth;
    
    private boolean isMonthlyAmountLimitReached;
    
    private List<DailyFoodConsumption> dailyFoodConsumptions;

    public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

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
        
        private String day;
        
        private long calories;
        
        private double amount;
        
        private boolean isCaloriesLimitExceeded;

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

		public long getCalories() {
			return calories;
		}

		public void setCalories(long calories) {
			this.calories = calories;
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}

		public boolean isCaloriesLimitExceeded() {
			return isCaloriesLimitExceeded;
		}

		public void setCaloriesLimitExceeded(boolean isCaloriesLimitExceeded) {
			this.isCaloriesLimitExceeded = isCaloriesLimitExceeded;
		}
        
    }
}
