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
	@DisplayName("Test: Admin can get weekly food report with data for the week")
	void testGetWeeklyFoodReport_Admin_WithData() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);
		List<Food> foods = List.of(new Food() {
			{
				setId(1L);
				setName("Pizza");
				setCalories(getCalories());
				setPrice(10.0F);
				setDate(startOfPreviousWeek);
				setTime(null);
				setUser(adminUser);
			}
		}, new Food() {
			{
				setId(2L);
				setName("Burger");
				setCalories(getCalories());
				setPrice(5.0F);
				setDate(LocalDate.now());
				setTime(null);
				setUser(adminUser);
			}
		});

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(foods);

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
		assertEquals(15, result.size());
		assertEquals(0, result.get(0).getUserDetails().size());
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
	@DisplayName("Test: Admin gets weekly food report with one day of data")
	void testGetWeeklyFoodReport_Admin_SingleDayData() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);
		List<Food> foods = List.of(new Food() {
			{
				setId(1L);
				setName("Pizza");
				setCalories(500);
				setPrice(10.0F);
				setDate(startOfPreviousWeek);
				setTime(null);
				setUser(adminUser);
			}
		});

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(foods);

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
		assertEquals(15, result.size());
		assertEquals(0, result.get(0).getUserDetails().size());
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
	@DisplayName("Test: Admin gets weekly food report with multiple users consuming food")
	void testGetWeeklyFoodReport_Admin_MultipleUsersFood() {
		LocalDate startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY);
		LocalDate endOfCurrentWeek = LocalDate.now().with(DayOfWeek.SUNDAY).plusDays(7);
		List<Food> foods = List.of(new Food() {
			{
				setId(1L);
				setName("Pizza");
				setCalories(getCalories());
				setPrice(10.0F);
				setDate(startOfPreviousWeek);
				setTime(null);
				setUser(adminUser);
			}
		}, new Food() {
			{
				setId(2L);
				setName("Burger");
				setCalories(getCalories());
				setPrice(5.0F);
				setDate(LocalDate.now());
				setTime(null);
				setUser(regularUser);
			}
		});

		when(userRepo.findById(1L)).thenReturn(Optional.of(adminUser));
		when(foodRepo.findAllByDateBetween(startOfPreviousWeek, endOfCurrentWeek)).thenReturn(foods);

		List<WeeklyFoodReportResponseDto> result = foodService.getWeeklyFoodReport(1L);

		assertNotNull(result);
		assertEquals(15, result.size());
		assertEquals(0, result.get(0).getUserDetails().size());
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
