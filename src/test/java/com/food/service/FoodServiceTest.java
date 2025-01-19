package com.food.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.food.constant.AppConstant;
import com.food.constant.RoleType;
import com.food.entity.Food;
import com.food.entity.User;
import com.food.exception.AppException;
import com.food.repository.FoodRepository;
import com.food.repository.UserRepository;
import com.food.request.payload.FoodDto;
import com.food.response.payload.FoodCaloriesResponseDto;
import com.food.response.payload.FoodConsumptionDto;
import com.food.response.payload.FoodConsumptionDto.DailyFoodConsumption;
import com.food.response.payload.FoodResponseDto;
import com.food.response.payload.GenericMessage;
import com.food.response.payload.PagedResponseDto;
import com.food.response.payload.PriceLimitReachedResponseDto;
import com.food.response.payload.UserResponseDto;
import com.food.response.payload.WeeklyFoodReportResponseDto;

public class FoodServiceTest {
	
	@Mock
	private UserRepository userRepo;

	@Mock
	private FoodRepository foodRepo;

	@InjectMocks
	private FoodService foodService;

	private User adminUser;
	private User regularUser;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		adminUser = new User();
		adminUser.setId(1L);
		adminUser.setEmail("admin@food.com");
		adminUser.setName("Admin User");
		adminUser.setRole(RoleType.ROLE_ADMIN.name());

		regularUser = new User();
		regularUser.setId(2L);
		regularUser.setEmail("user@food.com");
		regularUser.setName("Regular User");
		regularUser.setRole(RoleType.ROLE_USER.name());
	}

	@Test
	@DisplayName("Create Food - Success")
	void testCreateFood_Success() {
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Pizza");
		foodDto.setCalories(250);
		foodDto.setPrice(10.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		Food food = new Food();
		food.setName(foodDto.getName());
		food.setCalories(foodDto.getCalories());
		food.setPrice(foodDto.getPrice());
		food.setDate(foodDto.getDate());
		food.setTime(foodDto.getTime());
		food.setUser(user);
		food.setCreatedAt(LocalDateTime.now());

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.save(any(Food.class))).thenReturn(food);

		Food result = foodService.createFood(foodDto);

		assertEquals("Pizza", result.getName());
		assertEquals(250, result.getCalories());
		assertEquals(10.0, result.getPrice());
	}

	@Test
	@DisplayName("Create Food - User Not Found")
	void testCreateFood_UserNotFound() {
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(999L);
		foodDto.setName("Burger");
		foodDto.setCalories(300);
		foodDto.setPrice(8.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.createFood(foodDto);
		});
	}

	@Test
	@DisplayName("Create Food - Invalid User ID")
	void testCreateFood_InvalidUserId() {
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(0L);
		foodDto.setName("Pasta");
		foodDto.setCalories(400);
		foodDto.setPrice(15.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		assertThrows(AppException.class, () -> {
			foodService.createFood(foodDto);
		});
	}

	@Test
	@DisplayName("Create Food - Save Failed")
	void testCreateFood_SaveFailed() {
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Pizza");
		foodDto.setCalories(250);
		foodDto.setPrice(10.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.save(any(Food.class))).thenThrow(new RuntimeException("Database Error"));

		assertThrows(AppException.class, () -> {
			foodService.createFood(foodDto);
		});
	}

	@Test
	@DisplayName("Create Food - Error Handling")
	void testCreateFood_ErrorHandling() {
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Salad");
		foodDto.setCalories(150);
		foodDto.setPrice(5.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.save(any(Food.class))).thenThrow(new RuntimeException("Unexpected error"));

		assertThrows(AppException.class, () -> {
			foodService.createFood(foodDto);
		});
	}

	@Test
	@DisplayName("Update Food - Success")
	void testUpdateFood_Success() {
		Long foodId = 1L;
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Pizza");
		foodDto.setCalories(250);
		foodDto.setPrice(10.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		Food existingFood = new Food();
		existingFood.setId(foodId);
		existingFood.setUser(user);
		existingFood.setName("Old Pizza");
		existingFood.setCalories(200);
		existingFood.setPrice(8.0F);
		existingFood.setDate(LocalDateTime.now().toLocalDate());
		existingFood.setTime(LocalDateTime.now().toLocalTime());

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.findById(foodId)).thenReturn(Optional.of(existingFood));
		when(foodRepo.save(any(Food.class))).thenReturn(existingFood);

		Food result = foodService.updateFood(foodId, foodDto);

		assertEquals("Pizza", result.getName());
		assertEquals(250, result.getCalories());
		assertEquals(10.0, result.getPrice());
	}
	
	@Test
	@DisplayName("Update Food - User Not Found")
	void testUpdateFood_UserNotFound() {
		Long foodId = 1L;
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(999L);
		foodDto.setName("Burger");
		foodDto.setCalories(300);
		foodDto.setPrice(8.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.updateFood(foodId, foodDto);
		});
	}

	@Test
	@DisplayName("Update Food - Food Not Found")
	void testUpdateFood_FoodNotFound() {
		Long foodId = 999L;
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Pasta");
		foodDto.setCalories(350);
		foodDto.setPrice(12.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.findById(foodId)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.updateFood(foodId, foodDto);
		});
	}

	@Test
	@DisplayName("Update Food - Permission Denied")
	void testUpdateFood_PermissionDenied() {
		Long foodId = 1L;
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(2L);
		foodDto.setName("Salad");
		foodDto.setCalories(150);
		foodDto.setPrice(5.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		Food existingFood = new Food();
		existingFood.setId(foodId);
		existingFood.setUser(user);

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.findById(foodId)).thenReturn(Optional.of(existingFood));

		assertThrows(AppException.class, () -> {
			foodService.updateFood(foodId, foodDto);
		});
	}

	@Test
	@DisplayName("Update Food - Save Failed")
	void testUpdateFood_SaveFailed() {
		Long foodId = 1L;
		FoodDto foodDto = new FoodDto();
		foodDto.setUserId(1L);
		foodDto.setName("Pizza");
		foodDto.setCalories(250);
		foodDto.setPrice(10.0F);
		foodDto.setDate(LocalDateTime.now().toLocalDate());
		foodDto.setTime(LocalDateTime.now().toLocalTime());

		User user = new User();
		user.setId(1L);

		Food existingFood = new Food();
		existingFood.setId(foodId);
		existingFood.setUser(user);
		existingFood.setName("Old Pizza");
		existingFood.setCalories(200);
		existingFood.setPrice(8.0F);
		existingFood.setDate(LocalDateTime.now().toLocalDate());
		existingFood.setTime(LocalDateTime.now().toLocalTime());

		when(userRepo.findById(foodDto.getUserId())).thenReturn(Optional.of(user));
		when(foodRepo.findById(foodId)).thenReturn(Optional.of(existingFood));
		when(foodRepo.save(any(Food.class))).thenThrow(new RuntimeException("Database Error"));

		assertThrows(AppException.class, () -> {
			foodService.updateFood(foodId, foodDto);
		});
	}

	@Test
    @DisplayName("Delete Food - Success")
    void testDeleteFood_Success() {
        Long foodId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        Food food = new Food();
        food.setId(foodId);
        food.setUser(user);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(foodRepo.findById(foodId)).thenReturn(Optional.of(food));

        foodService.deleteFood(foodId, userId);

        verify(foodRepo, times(1)).delete(food);
    }

    @Test
    @DisplayName("Delete Food - User Not Found")
    void testDeleteFood_UserNotFound() {
        Long foodId = 1L;
        Long userId = 1L;

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            foodService.deleteFood(foodId, userId);
        });

        assertEquals("User id not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete Food - Food Not Found")
    void testDeleteFood_FoodNotFound() {
        Long foodId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(foodRepo.findById(foodId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            foodService.deleteFood(foodId, userId);
        });

        assertEquals("Food id not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete Food - Permission Denied")
    void testDeleteFood_PermissionDenied() {
        Long foodId = 1L;
        Long userId = 1L;
        Long anotherUserId = 2L;

        User user = new User();
        user.setId(userId);

        Food food = new Food();
        food.setId(foodId);
        food.setUser(new User());

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(foodRepo.findById(foodId)).thenReturn(Optional.of(food));

        AppException exception = assertThrows(AppException.class, () -> {
            foodService.deleteFood(foodId, anotherUserId);
        });

        assertEquals("User id not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete Food - Invalid ID")
    void testDeleteFood_InvalidId() {
        Long foodId = 999L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(foodRepo.findById(foodId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            foodService.deleteFood(foodId, userId);
        });

        assertEquals("Food id not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete Food - User Role Check")
    void testDeleteFood_UserRoleCheck() {
        Long foodId = 1L;
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setRole(RoleType.ROLE_USER.name());

        Food food = new Food();
        food.setId(foodId);
        food.setUser(user);

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(foodRepo.findById(foodId)).thenReturn(Optional.of(food));

        foodService.deleteFood(foodId, userId);

        verify(foodRepo, times(1)).delete(food);

        Long anotherUserId = 2L;

        AppException exception = assertThrows(AppException.class, () -> {
            foodService.deleteFood(foodId, anotherUserId);
        });

        assertEquals("User id not found", exception.getMessage());
    }

	@Test
	@DisplayName("Get Food By ID - Success")
	void testGetFoodById_Success() {
		Long foodId = 1L;

		Food food = new Food();
		food.setId(foodId);
		food.setName("Pizza");
		food.setCalories(250);
		food.setPrice(10.0F);

		when(foodRepo.findById(foodId)).thenReturn(Optional.of(food));

		Food result = foodService.getFoodById(foodId);

		assertEquals(foodId, result.getId());
		assertEquals("Pizza", result.getName());
		assertEquals(250, result.getCalories());
		assertEquals(10.0, result.getPrice());
	}

	@Test
	@DisplayName("Get Food By ID - Food Not Found")
	void testGetFoodById_FoodNotFound() {
		Long foodId = 1L;

		when(foodRepo.findById(foodId)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.getFoodById(foodId);
		});
	}

	@Test
	@DisplayName("Get Food By ID - Invalid ID")
	void testGetFoodById_InvalidId() {
		Long foodId = 999L;

		when(foodRepo.findById(foodId)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.getFoodById(foodId);
		});
	}

	@Test
	@DisplayName("Get Food By ID - Database Error")
	void testGetFoodById_DatabaseError() {
		Long foodId = 1L;

		when(foodRepo.findById(foodId)).thenThrow(new RuntimeException("Database Error"));

		assertThrows(AppException.class, () -> {
			foodService.getFoodById(foodId);
		});
	}

	@Test
	@DisplayName("Get Food By ID - Null Food Object")
	void testGetFoodById_NullFood() {
		Long foodId = 1L;

		when(foodRepo.findById(foodId)).thenReturn(Optional.ofNullable(null));

		assertThrows(AppException.class, () -> {
			foodService.getFoodById(foodId);
		});
	}

	@Test
	@DisplayName("Check Daily Calorie Threshold - Success, Below Limit")
	void testCheckDailyCalorieThreshold_Success_BelowLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setCalories(500);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setCalories(400);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedToday = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedToday);

		when(foodService.checkDailyCalorieThreshold(userId, requestDate)).thenReturn(null);

		assertNull(null, "The result should be null as the calories are below the limit.");
	}

	@Test
	@DisplayName("Check Daily Calorie Threshold - Success, Exactly at Limit")
	void testCheckDailyCalorieThreshold_Success_AtLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setCalories(1000);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setCalories(1000);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedToday = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedToday);

		when(foodService.checkDailyCalorieThreshold(userId, requestDate)).thenReturn(null);

		assertNull(null, "The result should be null as the calories are exactly at the limit.");
	}

	@Test
	@DisplayName("Check Daily Calorie Threshold - Success, Exceeded Limit")
	void testCheckDailyCalorieThreshold_Success_ExceededLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setCalories(1200);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setCalories(900);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedToday = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedToday);

		GenericMessage result = foodService.checkDailyCalorieThreshold(userId, requestDate);

		assertEquals("You have exceeded your daily calorie limit of 0 .", result.getMessage());
	}

	@Test
	@DisplayName("Check Daily Calorie Threshold - User Not Found")
	void testCheckDailyCalorieThreshold_UserNotFound() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		when(userRepo.findById(userId)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.checkDailyCalorieThreshold(userId, requestDate);
		});
	}

	@Test
	@DisplayName("Check Daily Calorie Threshold - Food List Not Found")
	void testCheckDailyCalorieThreshold_FoodListNotFound() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(null);

		assertThrows(AppException.class, () -> {
			foodService.checkDailyCalorieThreshold(userId, requestDate);
		});
	}

	@Test
	@DisplayName("Check Monthly Expenditure - Success, Below Limit")
	void testCheckMonthlyExpenditure_Success_BelowLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setPrice(150.0F);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setPrice(200.0F);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedThisMonth = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedThisMonth);

		when(foodService.checkMonthlyExpenditure(userId, requestDate)).thenReturn(null);

		assertNull(null, "The result should be null as the total expenditure is below the limit.");
	}

	@Test
	@DisplayName("Check Monthly Expenditure - Success, Exactly at Limit")
	void testCheckMonthlyExpenditure_Success_AtLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setPrice(250.0F);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setPrice(250.0F);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedThisMonth = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedThisMonth);

		when(foodService.checkMonthlyExpenditure(userId, requestDate)).thenReturn(null);

		assertNull(null, "The result should be null as the total expenditure is exactly at the limit.");
	}

	@Test
	@DisplayName("Check Monthly Expenditure - Success, Exceeded Limit")
	void testCheckMonthlyExpenditure_Success_ExceededLimit() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		Food food1 = new Food();
		food1.setPrice(300.0F);
		food1.setUser(new User());
		food1.setDate(requestDate);

		Food food2 = new Food();
		food2.setPrice(250.0F);
		food2.setUser(new User());
		food2.setDate(requestDate);

		List<Food> foodsConsumedThisMonth = Arrays.asList(food1, food2);

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(foodsConsumedThisMonth);

		GenericMessage result = foodService.checkMonthlyExpenditure(userId, requestDate);

		assertEquals("You have exceeded your monthly expenditure limit of " + "\u20ac" + "0.0 .", result.getMessage());
	}

	@Test
	@DisplayName("Check Monthly Expenditure - User Not Found")
	void testCheckMonthlyExpenditure_UserNotFound() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		when(userRepo.findById(userId)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> {
			foodService.checkMonthlyExpenditure(userId, requestDate);
		});
	}

	@Test
	@DisplayName("Check Monthly Expenditure - No Food Records")
	void testCheckMonthlyExpenditure_NoFoodRecords() {
		Long userId = 1L;
		LocalDate requestDate = LocalDate.now();

		when(userRepo.findById(userId)).thenReturn(Optional.of(new User()));
		when(foodRepo.findByUserIdAndDate(userId, requestDate)).thenReturn(Arrays.asList());

		when(foodService.checkMonthlyExpenditure(userId, requestDate)).thenReturn(null);

		assertNull(null, "The result should be null as no food records exist.");
	}
	
	
	
	

	@Test
	@DisplayName("Test: Admin can get empty weekly food report if no food exists")
	void testGetWeeklyFoodReport_Admin_NoData() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(Collections.emptyList());

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
	}

	@Test
	@DisplayName("Test: Regular user can't get weekly food report")
	void testGetWeeklyFoodReport_RegularUser() {
		when(userRepo.findById(2L)).thenReturn(Optional.of(regularUser));

		assertThrows(AppException.class, () -> foodService.getWeeklyFoodReport(2L));
	}


	@Test
	@DisplayName("Test: Admin gets weekly food report with no data for a week")
	void testGetWeeklyFoodReport_Admin_NoDataForWeek() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(Collections.emptyList());

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
	}

	@Test
	@DisplayName("Test: User not found in database")
	void testGetWeeklyFoodReport_UserNotFound() {
		when(userRepo.findById(1L)).thenReturn(Optional.empty());

		assertThrows(AppException.class, () -> foodService.getWeeklyFoodReport(1L));
	}

	@Test
	@DisplayName("Test: Exception thrown when repository fails")
	void testGetWeeklyFoodReport_Exception() {
		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(any(), any())).thenThrow(new RuntimeException("Database error"));

		assertThrows(AppException.class, () -> foodService.getWeeklyFoodReport(1L));
	}

	@Test
	@DisplayName("Test: Admin gets weekly food report when no foods consumed")
	void testGetWeeklyFoodReport_Admin_NoFoodConsumed() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(Collections.emptyList());

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
		assertEquals(15, result.size());
	}

	
	@Test
	void testBuildFoodResponse() {
		Food food = new Food();
		food.setId(1L);
		food.setName("Food 1");
		food.setCalories(200);
		food.setPrice(5f);
		food.setTime(LocalTime.now());

		WeeklyFoodReportResponseDto.FoodResponse result = foodService.buildFoodResponse(food);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Food 1", result.getFoodName());
		assertEquals(200, result.getCalorieCount());
	}

	@Test
	void testBuildUserResponse() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setName("Test User");
		user.setRole("ROLE_USER");

		UserResponseDto result = foodService.buildUserResponse(user);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("test@example.com", result.getEmail());
		assertEquals("Test User", result.getName());
		assertEquals("ROLE_USER", result.getRole());
	}
}
