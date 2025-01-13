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
	
	public List<FoodConsumptionDto> getFoodConsumptionSummary(long userId, Integer month, Integer year) {
		try {
			LocalDate currentDate = LocalDate.now();
			if (year == null || year <= 0) {
				year = currentDate.getYear();
			}

			if (month == null || month <= 0) {
				month = currentDate.getMonthValue();
			}

			LocalDate startDate = LocalDate.of(year, month, 1);
			LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

			List<Food> consumedFoods = foodRepo.findByUserIdAndDateBetween(userId, startDate, endDate);
			List<FoodConsumptionDto> consumptionDtos = new ArrayList<>();

			FoodConsumptionDto consumptionDto = new FoodConsumptionDto();
			consumptionDto.setMonth(startDate.getMonth().toString());
			consumptionDto.setYear(year);

			double totalAmount = consumedFoods.stream().mapToDouble(Food::getPrice).sum();
			long totalCalories = consumedFoods.stream().mapToLong(Food::getCalories).sum();
			boolean limitExceeded = totalAmount >= monthlySpendLimit;

			consumptionDto.setTotalAmountSpentInMonth(totalAmount);
			consumptionDto.setTotalCaloriesConsumedInMonth(totalCalories);
			consumptionDto.setMonthlyAmountLimitReached(limitExceeded);

			List<FoodConsumptionDto.DailyFoodConsumption> dailyConsumptions = new ArrayList<>();
			for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
				LocalDate iteratedDate = date;
				List<Food> foodsForDay = consumedFoods.stream().filter(food -> food.getDate().equals(iteratedDate))
						.collect(Collectors.toList());

				FoodConsumptionDto.DailyFoodConsumption dailyConsumption = new FoodConsumptionDto.DailyFoodConsumption();
				dailyConsumption.setDate(date);
				dailyConsumption.setDay(date.getDayOfWeek().toString());
				if (!foodsForDay.isEmpty()) {
					dailyConsumption.setCalories(foodsForDay.stream().mapToLong(Food::getCalories).sum());
					dailyConsumption.setAmount(foodsForDay.stream().mapToDouble(Food::getPrice).sum());
					dailyConsumption.setCaloriesLimitExceeded(dailyConsumption.getCalories() >= dailyCaloriesLimit);
				}
				dailyConsumptions.add(dailyConsumption);
			}

			consumptionDto.setDailyFoodConsumptions(dailyConsumptions);
			consumptionDtos.add(consumptionDto);

			return consumptionDtos;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public PagedResponseDto<FoodResponseDto> getAllFood(long userId, LocalDate requestStartDate,
			LocalDate requestEndDate, int page, int size, long searchUserId) {
		try {
			LocalDate startDate = requestStartDate != null ? requestStartDate
					: LocalDate.now().minusDays(AppConstant.DEFAULT_FOOD_TRACKING_DAYS_COUNT);
			LocalDate endDate = requestEndDate != null ? requestEndDate : LocalDate.now();

			if (startDate.isAfter(endDate)) {
				throw new AppException("Start date cannot be after end date.");
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("date")));
			Page<Food> foodPage;

			Optional<User> user = userRepo.findById(userId);
			if (!user.isPresent()) {
				throw new NotFoundException("User id not found");
			}

			if (!user.get().getRole().equals(RoleType.ROLE_ADMIN.name())) {
				foodPage = foodRepo.findByUserIdAndDateBetween(userId, startDate, endDate, pageable);
			} else {
				foodPage = searchUserId > 0
						? foodRepo.findByUserIdAndDateBetween(searchUserId, startDate, endDate, pageable)
						: foodRepo.findByDateBetween(startDate, endDate, pageable);
			}

			List<FoodResponseDto> responseDtos = new ArrayList<>();
			List<Food> foods = foodPage.getContent();
			
			System.out.println("====Size===="+foods.size());

			FoodResponseDto monthlyDto = new FoodResponseDto();
			monthlyDto.setTotalAmountSpentInMonth(foods.stream().mapToDouble(Food::getPrice).sum());
			monthlyDto.setTotalCaloriesConsumedInMonth(foods.stream().mapToLong(Food::getCalories).sum());
			monthlyDto.setMonthlyAmountLimitReached(monthlyDto.getTotalCaloriesConsumedInMonth() >= monthlySpendLimit);
			
			System.out.println("========"+foods.stream().mapToDouble(Food::getPrice).sum());

			List<FoodResponseDto.DailyFoodConsumption> dailyConsumptions = new ArrayList<>();
			for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
				LocalDate iteratedDate = date;
				List<Food> dailyFoods = foods.stream().filter(food -> food.getDate().equals(iteratedDate))
						.collect(Collectors.toList());

				FoodResponseDto.DailyFoodConsumption dailyDto = new FoodResponseDto.DailyFoodConsumption();
				dailyDto.setDate(date);
				dailyDto.setTotalCalories(dailyFoods.stream().mapToLong(Food::getCalories).sum());
				dailyDto.setTotalAmount(dailyFoods.stream().mapToDouble(Food::getPrice).sum());
				dailyDto.setCaloriesLimitExceeded(dailyDto.getTotalCalories() >= dailyCaloriesLimit);

				List<FoodResponseDto.DailyFoodResponse> dailyFoodResponses = dailyFoods.stream().map(food -> {
					FoodResponseDto.DailyFoodResponse foodResponse = new FoodResponseDto.DailyFoodResponse();
					foodResponse.setFoodId(food.getId());
					foodResponse.setFoodName(food.getName());
					foodResponse.setCalorieCount(food.getCalories());
					foodResponse.setPrice(food.getPrice());
					foodResponse.setConsumptionTime(food.getTime());
					foodResponse.setCreatedAt(food.getCreatedAt());

					UserResponseDto userDto = new UserResponseDto();
					userDto.setId(food.getUser().getId());
					userDto.setEmail(food.getUser().getEmail());
					userDto.setName(food.getUser().getName());
					userDto.setRole(food.getUser().getRole());
					foodResponse.setUserDetails(userDto);
					return foodResponse;
				}).collect(Collectors.toList());

				dailyDto.setDailyFoodResponses(dailyFoodResponses);
				dailyConsumptions.add(dailyDto);

				boolean dayFound = dailyConsumptions.stream()
						.anyMatch(consumption -> consumption.getDate().equals(iteratedDate));
				if (!dayFound) {
					FoodResponseDto.DailyFoodConsumption emptyDayDto = new FoodResponseDto.DailyFoodConsumption();
					emptyDayDto.setDate(date);
					emptyDayDto.setTotalCalories(0);
					emptyDayDto.setTotalAmount(0.0);
					emptyDayDto.setCaloriesLimitExceeded(false);
					emptyDayDto.setDailyFoodResponses(new ArrayList<>());
					dailyConsumptions.add(emptyDayDto);
				}

			}

			monthlyDto.setDailyFoodConsumptions(dailyConsumptions);
			responseDtos.add(monthlyDto);
			
			return new PagedResponseDto<>(responseDtos, foodPage.getNumber(), foodPage.getSize(),
					foodPage.getTotalElements(), foodPage.getTotalPages(), foodPage.isLast());

		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}
	
	public long getDaysFoodCount(long userId, LocalDate requestStartDate, LocalDate requestEndDate) {
		try {
			LocalDate startDate = requestStartDate != null ? requestStartDate
					: LocalDate.now().minusDays(AppConstant.DEFAULT_FOOD_TRACKING_DAYS_COUNT);
			LocalDate endDate = requestEndDate != null ? requestEndDate : LocalDate.now();

			if (startDate.isAfter(endDate)) {
				throw new AppException("Start date cannot be after end date.");
			}
			return foodRepo.countByDateBetween(startDate, endDate);
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}
	
	public List<FoodCaloriesResponseDto> calculateAverageCaloriesPerUser(LocalDate startDate, LocalDate endDate) {
	    try {
	        LocalDate effectiveStartDate = (startDate != null) ? startDate
	                : LocalDate.now().minusDays(AppConstant.DEFAULT_FOOD_TRACKING_DAYS_COUNT);
	        LocalDate effectiveEndDate = (endDate != null) ? endDate : LocalDate.now();

	        if (effectiveStartDate.isAfter(effectiveEndDate)) {
	            throw new AppException("Start date cannot be after end date.");
	        }

	        List<User> allUsers = userRepo.findAllByRole(RoleType.ROLE_USER.name());
	        List<Food> foodsConsumedInPeriod = foodRepo.findAllByDateBetween(effectiveStartDate, effectiveEndDate);

	        return allUsers.stream().map(user -> {
	            List<Food> userFoods = foodsConsumedInPeriod.stream()
	                    .filter(food -> food.getUser().equals(user))
	                    .collect(Collectors.toList());

	            int totalCaloriesConsumed = userFoods.stream().mapToInt(Food::getCalories).sum();
	            int foodCount = userFoods.size();

	            float averageCalories = (foodCount > 0) ? (float) totalCaloriesConsumed / foodCount : 0;

	            UserResponseDto userDto = new UserResponseDto();
	            userDto.setEmail(user.getEmail());
	            userDto.setId(user.getId());
	            userDto.setName(user.getName());
	            userDto.setRole(user.getRole());

	            FoodCaloriesResponseDto responseDto = new FoodCaloriesResponseDto();
	            responseDto.setUser(userDto);
	            responseDto.setCalories(averageCalories);

	            return responseDto;
	        }).collect(Collectors.toList());
	    } catch (Exception e) {
	        throw new AppException(e.getMessage());
	    }
	}
	public PriceLimitReachedResponseDto getUsersWithExceededPriceLimit(Integer month, Integer year) {
		try {
			YearMonth currentMonth = (month != null && year != null) ? YearMonth.of(year, month)
					: YearMonth.now().minusMonths(1);

			LocalDate startOfMonth = currentMonth.atDay(1);
			LocalDate endOfMonth = currentMonth.atEndOfMonth();

			List<Food> foodsConsumed = foodRepo.findAllByDateBetween(startOfMonth, endOfMonth);
			Map<User, List<Food>> foodsByUser = foodsConsumed.stream().collect(Collectors.groupingBy(Food::getUser));

			PriceLimitReachedResponseDto responseDto = new PriceLimitReachedResponseDto();
			responseDto.setMonthlyLimit(monthlySpendLimit);

			List<PriceLimitReachedResponseDto.ExceededUser> exceededUsers = new ArrayList<>();

			for (Map.Entry<User, List<Food>> entry : foodsByUser.entrySet()) {
				User user = entry.getKey();
				List<Food> userFoods = entry.getValue();
				double totalSpentAmount = userFoods.stream().mapToDouble(Food::getPrice).sum();
				if (totalSpentAmount > monthlySpendLimit) {
					PriceLimitReachedResponseDto.ExceededUser exceededUser = new PriceLimitReachedResponseDto.ExceededUser();
					UserResponseDto userResponseDto = new UserResponseDto();
					userResponseDto.setId(user.getId());
					userResponseDto.setEmail(user.getEmail());
					userResponseDto.setName(user.getName());
					userResponseDto.setRole(user.getRole());

					exceededUser.setUser(userResponseDto);
					exceededUser.setTotalSpentAmount(totalSpentAmount);
					exceededUser.setExceededAmount(totalSpentAmount - monthlySpendLimit);

					exceededUsers.add(exceededUser);
				}
			}

			responseDto.setExceededUsers(exceededUsers);
			return responseDto;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}
	
	public List<WeeklyFoodReportResponseDto> getWeeklyFoodReport(long userId) {
		try {
			Optional<User> optionalUser = userRepo.findById(userId);
			if (!optionalUser.isPresent()) {
				throw new NotFoundException("User ID not found");
			}

			User user = optionalUser.get();
			if (!user.getRole().equals(RoleType.ROLE_ADMIN.name())) {
				throw new NotFoundException("You don't have permission to view this information");
			}

			LocalDate today = LocalDate.now();
			LocalDate startOfCurrentWeek = today.with(DayOfWeek.SUNDAY);
			if (!today.equals(startOfCurrentWeek)) {
				startOfCurrentWeek = today.minusDays(today.getDayOfWeek().getValue());
			}
			LocalDate endOfCurrentWeek = startOfCurrentWeek.plusDays(AppConstant.WEEKDAYS_COUNT);
			LocalDate startOfPreviousWeek = startOfCurrentWeek.minusWeeks(1);
//			LocalDate endOfPreviousWeek = startOfPreviousWeek.plusDays(AppConstant.WEEKDAYS_COUNT);

			List<Food> foods = foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek);

			List<WeeklyFoodReportResponseDto> weeklyReport = new ArrayList<>();

			for (LocalDate date = startOfPreviousWeek; !date.isAfter(endOfCurrentWeek); date = date.plusDays(1)) {

				LocalDate iteratedDate = date;

				List<Food> dailyFoods = foods.stream().filter(food -> food.getDate().equals(iteratedDate))
						.collect(Collectors.toList());

				double totalAmountSpent = dailyFoods.stream().mapToDouble(Food::getPrice).sum();
				int totalCaloriesConsumed = dailyFoods.stream().mapToInt(Food::getCalories).sum();

				WeeklyFoodReportResponseDto reportDto = new WeeklyFoodReportResponseDto();
				reportDto.setDate(date);
				reportDto.setDay(date.getDayOfWeek().toString());
				reportDto.setMonth(date.getMonth().toString());
				reportDto.setMonthlyPriceLimitExceed(totalAmountSpent >= monthlySpendLimit);
				reportDto.setTotalAmount(totalAmountSpent);
				reportDto.setCaloriesLimitExceeded(totalCaloriesConsumed >= dailyCaloriesLimit);
				reportDto.setTotalCalories(totalCaloriesConsumed);

				List<WeeklyFoodReportResponseDto.UserDetail> userDetails = new ArrayList<>();

				List<User> usersWhoCreatedFood = dailyFoods.stream().map(Food::getUser).distinct()
						.collect(Collectors.toList());

				for (User foodCreator : usersWhoCreatedFood) {
					WeeklyFoodReportResponseDto.UserDetail userDetail = new WeeklyFoodReportResponseDto.UserDetail();
					userDetail.setUserResponse(buildUserResponse(foodCreator));

					List<WeeklyFoodReportResponseDto.FoodResponse> foodResponses = dailyFoods.stream()
							.filter(food -> food.getUser().equals(foodCreator)).map(this::buildFoodResponse)
							.collect(Collectors.toList());

					userDetail.setFoodResponses(foodResponses);
					userDetails.add(userDetail);
				}

				reportDto.setUserDetails(userDetails);
				weeklyReport.add(reportDto);
			}
			return weeklyReport;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public UserResponseDto buildUserResponse(User user) {
		UserResponseDto userResponseDto = new UserResponseDto();
		userResponseDto.setId(user.getId());
		userResponseDto.setEmail(user.getEmail());
		userResponseDto.setName(user.getName());
		userResponseDto.setRole(user.getRole());
		return userResponseDto;
	}

	public WeeklyFoodReportResponseDto.FoodResponse buildFoodResponse(Food food) {
		WeeklyFoodReportResponseDto.FoodResponse foodResponse = new WeeklyFoodReportResponseDto.FoodResponse();
		foodResponse.setId(food.getId());
		foodResponse.setFoodName(food.getName());
		foodResponse.setCalorieCount(food.getCalories());
		foodResponse.setPrice(food.getPrice());
		foodResponse.setConsumptionTime(food.getTime());
		return foodResponse;
	}
	
}

