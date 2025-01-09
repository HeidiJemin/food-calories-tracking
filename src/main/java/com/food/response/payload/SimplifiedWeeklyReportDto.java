package com.food.response.payload;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimplifiedWeeklyReportDto {
    private LocalDate date;
    private String day;
    private int totalFoodEntries;
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
	public int getTotalFoodEntries() {
		return totalFoodEntries;
	}
	public void setTotalFoodEntries(int totalFoodEntries) {
		this.totalFoodEntries = totalFoodEntries;
	}
	public SimplifiedWeeklyReportDto(LocalDate date, String day, int totalFoodEntries) {
		super();
		this.date = date;
		this.day = day;
		this.totalFoodEntries = totalFoodEntries;
	}
    
}