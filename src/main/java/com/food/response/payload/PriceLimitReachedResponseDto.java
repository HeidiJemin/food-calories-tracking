package com.food.response.payload;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceLimitReachedResponseDto {
    
    private double monthlyLimit;
    
    public List<ExceededUser> exceededUsers;
    

    public double getMonthlyLimit() {
		return monthlyLimit;
	}


	public void setMonthlyLimit(double monthlyLimit) {
		this.monthlyLimit = monthlyLimit;
	}


	public List<ExceededUser> getExceededUsers() {
		return exceededUsers;
	}


	public void setExceededUsers(List<ExceededUser> exceededUsers) {
		this.exceededUsers = exceededUsers;
	}


	@Getter
    @Setter
    public static class ExceededUser {
    	
        private UserResponseDto user;
        
        private double totalSpentAmount;
        
        private Double exceededAmount;

		public UserResponseDto getUser() {
			return user;
		}

		public void setUser(UserResponseDto user) {
			this.user = user;
		}

		public double getTotalSpentAmount() {
			return totalSpentAmount;
		}

		public void setTotalSpentAmount(double totalSpentAmount) {
			this.totalSpentAmount = totalSpentAmount;
		}

		public Double getExceededAmount() {
			return exceededAmount;
		}

		public void setExceededAmount(Double exceededAmount) {
			this.exceededAmount = exceededAmount;
		}
        
    }
}