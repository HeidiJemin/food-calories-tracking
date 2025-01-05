package com.food.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.food.constant.AppConstant;
import com.food.constant.RoleType;
import com.food.entity.Food;
import com.food.entity.User;
import com.food.exception.AppException;
import com.food.exception.NotFoundException;
import com.food.repository.FoodRepository;
import com.food.repository.UserRepository;
import com.food.request.payload.FoodDto;
import com.food.response.payload.FoodCaloriesResponseDto;
import com.food.response.payload.*;
import com.food.response.payload.GenericMessage;


@Service
public class FoodService {

	@Autowired
	private FoodRepository foodRepo;

	@Autowired
	UserRepository userRepo;

	@Value("${app.daily.calorie.threshold.limit}")
	private int dailyCaloriesLimit;

	@Value("${app.user.monthly.spend.limit}")
	private float monthlySpendLimit;

	public Food createFood(FoodDto foodDto) {
		try {
			Optional<User> user = userRepo.findById(foodDto.getUserId());
			if (!user.isPresent()) {
				throw new NotFoundException("User id not found");
			}

			Food food = new Food();
			food.setName(foodDto.getName());
			food.setCalories(foodDto.getCalories());
			food.setPrice(foodDto.getPrice());
			food.setDate(foodDto.getDate());
			food.setTime(foodDto.getTime());
			food.setUser(user.get());
			food.setCreatedAt(LocalDateTime.now());
			return foodRepo.save(food);
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public Food updateFood(Long id, FoodDto foodDto) {
		try {

			Optional<User> user = userRepo.findById(foodDto.getUserId());
			if (!user.isPresent()) {
				throw new NotFoundException("User id not found");
			}

			Optional<Food> existingFood = foodRepo.findById(id);
			if (existingFood.isPresent()) {

				if (!existingFood.get().getUser().getId().equals(foodDto.getUserId())
						&& user.get().getRole().equalsIgnoreCase(RoleType.ROLE_USER.name())) {
					throw new NotFoundException("You don't have a permission to update this details");
				}

				Food food = existingFood.get();
				food.setName(foodDto.getName());
				food.setCalories(foodDto.getCalories());
				food.setPrice(foodDto.getPrice());
				food.setDate(foodDto.getDate());
				food.setTime(foodDto.getTime());
				return foodRepo.save(food);
			} else {
				throw new NotFoundException("Food id not found");
			}
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public void deleteFood(Long id, long userId) {
		try {
			Optional<User> user = userRepo.findById(userId);
			if (!user.isPresent()) {
				throw new NotFoundException("User id not found");
			}

			Optional<Food> existingFood = foodRepo.findById(id);
			if (existingFood.isPresent()) {
				if (!existingFood.get().getUser().getId().equals(userId)
						&& user.get().getRole().equalsIgnoreCase(RoleType.ROLE_USER.name())) {
					throw new NotFoundException("You don't have a permission to delete this details");
				}
				foodRepo.delete(existingFood.get());
			} else {
				throw new NotFoundException("Food id not found");
			}
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public Food getFoodById(Long id) {
		try {
			Optional<Food> food = foodRepo.findById(id);
			if (food.isPresent()) {
				return food.get();
			} else {
				throw new NotFoundException("Food id not found");
			}
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public GenericMessage checkDailyCalorieThreshold(Long userId, LocalDate requestDate) {
		try {
			Optional<User> user = userRepo.findById(userId);
			if (!user.isPresent()) {
				throw new NotFoundException("User id not found");
			}
			LocalDate currentDate = requestDate != null ? requestDate : LocalDate.now();
			List<Food> foodsConsumedToday = foodRepo.findByUserIdAndDate(userId, currentDate);
			System.out.println("FOODS CONSUMED TODAY : "+foodsConsumedToday.size());
			int totalCaloriesConsumed = foodsConsumedToday.stream().mapToInt(Food::getCalories).sum();
			if (totalCaloriesConsumed >= dailyCaloriesLimit) {
				return new GenericMessage("You have exceeded your daily calorie limit of " + dailyCaloriesLimit + " .",
						LocalDateTime.now());
			}
			return null;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public GenericMessage checkMonthlyExpenditure(long userId, LocalDate requestDate) {
		try {
			Optional<User> userOptional = userRepo.findById(userId);
			if (!userOptional.isPresent()) {
				throw new NotFoundException("User with id " + userId + " not found.");
			}
			LocalDate currentDate = requestDate != null ? requestDate : LocalDate.now();
			LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
			LocalDate lastDayOfMonth = currentDate.withDayOfMonth(LocalDate.now().lengthOfMonth());
			List<Food> foodsConsumedThisMonth = foodRepo.findByUserIdAndDateBetween(userId, firstDayOfMonth, lastDayOfMonth);
			double totalExpenditureThisMonth = foodsConsumedThisMonth.stream().mapToDouble(Food::getPrice).sum();
			if (totalExpenditureThisMonth >= monthlySpendLimit) {
				return new GenericMessage(
						"You have exceeded your monthly expenditure limit of "+"\u20ac" + monthlySpendLimit + " .",
						LocalDateTime.now());
			}
			return null;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}
	

	
	
}

