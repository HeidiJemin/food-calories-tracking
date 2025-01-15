package com.food.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.food.constant.RoleType;
import com.food.entity.Food;
import com.food.repository.FoodRepository;
import com.food.repository.UserRepository;
import com.food.request.payload.FoodDto;
import com.food.request.payload.LoginDto;
import com.food.request.payload.UserDto;
import com.food.response.payload.FoodCaloriesResponseDto;
import com.food.response.payload.FoodConsumptionDto;
import com.food.response.payload.FoodResponseDto;
import com.food.response.payload.GenericMessage;
import com.food.response.payload.PagedResponseDto;
import com.food.response.payload.PriceLimitReachedResponseDto;
import com.food.response.payload.SimplifiedWeeklyReportDto;
import com.food.response.payload.UserResponseDto;
import com.food.response.payload.WeeklyFoodReportResponseDto;
import com.food.service.AuthService;
import com.food.service.FoodService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping
public class ThymeLeafController {
	
	@Autowired
	UserRepository userRepo;

	@Autowired
	private AuthService authService;

	@Autowired
	private FoodService foodService;
	
	@Autowired
	private FoodRepository foodRepo;

	@GetMapping("/login")
	public String showLoginPage1(Model model) {
		model.addAttribute("loginDto", new LoginDto());
		return "login";
	}

	@GetMapping("/register")
	public String showregisterPage1(Model model) {
		model.addAttribute("userDto", new UserDto());
		return "register";
	}

	@GetMapping("/public/auth/login")
	public String showLoginPage(Model model) {
		model.addAttribute("loginDto", new LoginDto());
		return "login";
	}

	@PostMapping("/public/auth/login")
	public String login(@Valid LoginDto loginDto, BindingResult result, Model model, HttpSession session) {

		System.out.println("SUCCESS=========");
		if (result.hasErrors()) {
			return "login";
		}
		try {
			UserResponseDto userResponseDto = authService.login(loginDto);
			session.setAttribute("user", userResponseDto);
			if (userResponseDto.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
				return "redirect:/admin/dashboard";
			} else {
				return "redirect:/public/dashboard";
			}

		} catch (Exception e) {
			model.addAttribute("error", "Invalid login credentials");
			return "login";
		}
	}

	@GetMapping("/public/auth/register")
	public String showRegisterPage(Model model) {
		model.addAttribute("userDto", new UserDto());
		return "register";
	}

	@PostMapping("/public/auth/register")
	public String register(@Valid UserDto userDto, BindingResult result, Model model) {
		if (result.hasErrors()) {
			return "register";
		}
		try {
			authService.register(userDto);
			model.addAttribute("success", "Registration successful, please log in");
			return "redirect:/login";
		} catch (Exception e) {
			model.addAttribute("error", e.getMessage());
			return "register";
		}
	}

	@GetMapping("/public/auth/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		new SecurityContextLogoutHandler().logout(request, response, authentication);
		return "redirect:/login";
	}
	
	@GetMapping("/public/dashboard")
	public String showDashboardPage(Model model, HttpSession session,
	        @RequestParam(name = "month", required = false, defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") Integer month,
	        @RequestParam(name = "year", required = false, defaultValue = "#{T(java.time.LocalDate).now().getYear()}") Integer year) {
	    UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
	    if (userResponseDto != null) {
	        model.addAttribute("user", userResponseDto);
	        List<FoodConsumptionDto> consumptionSummary = foodService.getFoodConsumptionSummary(userResponseDto.getId(), month, year);
	        model.addAttribute("consumptionSummary", consumptionSummary);
	        model.addAttribute("selectedMonth", String.valueOf(month));
	        model.addAttribute("selectedYear", year);

	        List<Map<String, String>> months = IntStream.rangeClosed(1, 12)
	                .mapToObj(m -> Map.of("value", String.valueOf(m), "displayName",
	                        Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)))
	                .toList();
	        model.addAttribute("months", months);

	        int currentYear = LocalDate.now().getYear();
	        List<Integer> years = IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList();
	        model.addAttribute("years", years);

	        LocalDate today = LocalDate.now();
	        LocalDate startOfWeek = today.with(ChronoField.DAY_OF_WEEK, 1);
	        LocalDate endOfWeek = startOfWeek.plusDays(6);

	        List<Food> weeklyFoods = foodRepo.findByUserIdAndDateBetween(userResponseDto.getId(), startOfWeek, endOfWeek);

	        Map<LocalDate, List<Food>> dailyFoodMap = weeklyFoods.stream()
	                .collect(Collectors.groupingBy(Food::getDate));

	        int daysCaloriesExceeded = 0;
	        double totalExpenditure = 0;

	        for (LocalDate date : dailyFoodMap.keySet()) {
	            List<Food> foodsForDay = dailyFoodMap.get(date);
	            long dailyCalories = foodsForDay.stream().mapToLong(Food::getCalories).sum();
	            double dailyAmount = foodsForDay.stream().mapToDouble(Food::getPrice).sum();

	            if (dailyCalories > 2500) {
	                daysCaloriesExceeded++;
	            }

	            totalExpenditure += dailyAmount;
	        }

	        model.addAttribute("daysCaloriesExceeded", daysCaloriesExceeded);
	        model.addAttribute("totalWeeklyExpenditure", totalExpenditure);
	    } else {
	        model.addAttribute("error", "User not logged in.");
	        return "redirect:/login";
	    }
	    return "dashboard";
	}
	
	@GetMapping("/public/food-report")
	public String getFoodReport(
			@RequestParam(name="startDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name="endDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(name="page",defaultValue = "0") int page, @RequestParam(name="size",defaultValue = "10") int size,
			@RequestParam(name="searchMemberId",required = false) Long searchMemberId, Model model, HttpSession session) {
		UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
		if (userResponseDto == null) {
			model.addAttribute("error", "Please log in to view the food report");
			return "redirect:/login";
		}

		LocalDate currentDate = LocalDate.now();
		if (startDate == null) {
			startDate = currentDate.withDayOfMonth(1);
		}
		if (endDate == null) {
			endDate = currentDate;
		}

		try {
			Long userId = userResponseDto.getId();

			PagedResponseDto<FoodResponseDto> foodList = foodService.getAllFood(userId, startDate, endDate, page, 100000,
					0);

			model.addAttribute("foodData", foodList);
			model.addAttribute("currentPage", page);
			model.addAttribute("pageSize", 100000);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("searchMemberId", 0);
			model.addAttribute("userId", userId);
			model.addAttribute("user", userResponseDto);
			
			if (userResponseDto.getRole().equals(RoleType.ROLE_ADMIN.name())) {
				model.addAttribute("users", userRepo.findAll().stream()
						.filter(e -> e.getRole().equals(RoleType.ROLE_USER.name())).collect(Collectors.toList()));
			}

			LocalDate today = LocalDate.now();
			GenericMessage calorieMessage = foodService.checkDailyCalorieThreshold(userResponseDto.getId(), today);
			if (calorieMessage != null) {
				model.addAttribute("warningCalorieMessage", calorieMessage.getMessage());
			}

			GenericMessage monthlyExpenditureMessage = foodService.checkMonthlyExpenditure(userId, today);
			if (monthlyExpenditureMessage != null) {
				model.addAttribute("warningMonthlyExpenditureMessage", monthlyExpenditureMessage.getMessage());
			}

			return "food";
		} catch (Exception e) {
			PagedResponseDto<FoodResponseDto> foodList  = new PagedResponseDto<FoodResponseDto>(new ArrayList<FoodResponseDto>(), page, 100000, 0, 0, true);
			model.addAttribute("foodData", foodList);
			model.addAttribute("currentPage", page);
			model.addAttribute("pageSize", 100000);
			model.addAttribute("startDate", startDate);
			model.addAttribute("endDate", endDate);
			model.addAttribute("searchMemberId", 0);
			model.addAttribute("user", userResponseDto);
			model.addAttribute("error", "Error fetching food report: " + e.getMessage());
			return "food";
		}
	}

	@PostMapping("/public/food-report")
	public String createFood(@Valid FoodDto foodDto, BindingResult result, Model model, HttpSession session) {
		UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
		if (userResponseDto == null) {
			model.addAttribute("error", "Please log in to add food.");
			return "redirect:/login";
		}

		if (result.hasErrors()) {
			return "food";
		}

		try {
			if (!userResponseDto.getRole().equals(RoleType.ROLE_ADMIN.name())) {
	            foodDto.setUserId(userResponseDto.getId());
	        }
			foodService.createFood(foodDto);
			model.addAttribute("success", "Food added successfully!");
			return "redirect:/public/food-report";
		} catch (Exception e) {
			model.addAttribute("error", "Error adding food: " + e.getMessage());
			return "food";
		}
	}
	
	@GetMapping("/public/food-report/edit/{id}")
	public String showEditForm(@PathVariable("id") Long id, Model model, HttpSession session) {
		UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
		if (userResponseDto == null) {
			return "redirect:/public/auth/login";
		}

		Food food = foodService.getFoodById(id);
		
		model.addAttribute("user", userResponseDto);
		model.addAttribute("food", food);
		
		return "food-edit";
	}

	@PostMapping("/public/food-report/edit/{id}")
	public String updateFood(@PathVariable("id") Long id, @Valid Food food, BindingResult result, HttpSession session) {

		UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
		if (userResponseDto == null) {
			return "redirect:/login";
		}

		if (result.hasErrors()) {
			return "food-edit";
		}

		try {
			FoodDto foodDto = new FoodDto();
			
			foodDto.setCalories(food.getCalories());
			foodDto.setDate(food.getDate());
			foodDto.setName(food.getName());
			foodDto.setPrice(food.getPrice());
			foodDto.setTime(food.getTime());
			foodDto.setUserId(userResponseDto.getId());
			foodService.updateFood(id, foodDto);
			return "redirect:/public/food-report?success=Food updated successfully!";
		} catch (Exception e) {
			return "redirect:/public/food-report/edit/" + id + "?error=" + e.getMessage();
		}
	}

	@PostMapping("/public/food-report/delete/{id}")
	public String deleteFood(@PathVariable("id") Long id, @RequestParam(name="userId") Long userId, HttpSession session, Model model) {
		UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");
		if (userResponseDto == null) {
			return "redirect:/login";
		}
		try {
			foodService.deleteFood(id, userResponseDto.getId());
			model.addAttribute("successMessage", "Food deleted successfully!");
		} catch (Exception e) {
			model.addAttribute("error", "Error deleting food: " + e.getMessage());
		}
		return "redirect:/public/food-report";
	}
	@GetMapping("/admin/dashboard")
	public String showAdminDashboard(
	        @RequestParam(name="startDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam(name="endDate",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
	        @RequestParam(name="month",required = false) Integer month, 
	        @RequestParam(name="year", required = false) Integer year, 
	        Model model,
	        HttpSession session) {

	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    System.out.println("Authorities: " + auth.getAuthorities());

	    UserResponseDto userResponseDto = (UserResponseDto) session.getAttribute("user");

	    if (userResponseDto == null) {
	        model.addAttribute("error", "You must be logged in to access the admin dashboard.");
	        return "redirect:/login";
	    }

	    LocalDate currentDate = LocalDate.now();
	    if (startDate == null) {
	        startDate = currentDate.minusDays(7);
	    }
	    if (endDate == null) {
	        endDate = currentDate;
	    }

	    if (month == null) {
	        month = currentDate.getMonth().getValue();
	    }
	    if (year == null) {
	        year = currentDate.getYear();
	    }

	    List<FoodCaloriesResponseDto> averageCalories = foodService.calculateAverageCaloriesPerUser(startDate, endDate);
	    model.addAttribute("averageCalories", averageCalories);

	    PriceLimitReachedResponseDto exceededPriceLimitUsers = foodService.getUsersWithExceededPriceLimit(month, year);
	    model.addAttribute("exceededPriceLimitUsers", exceededPriceLimitUsers != null ? exceededPriceLimitUsers : new PriceLimitReachedResponseDto());

	    try {
	        List<WeeklyFoodReportResponseDto> weeklyReport = foodService.getWeeklyFoodReport(userResponseDto.getId());

	        LocalDate now = LocalDate.now();
	        LocalDate startOfCurrentWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	        LocalDate startOfPreviousWeek = startOfCurrentWeek.minusWeeks(1);

	        List<SimplifiedWeeklyReportDto> currentWeekReport = weeklyReport.stream()
	                .filter(report -> {
	                    LocalDate reportDate = report.getDate();
	                    return !reportDate.isBefore(startOfCurrentWeek) && !reportDate.isAfter(now);
	                })
	                .map(report -> new SimplifiedWeeklyReportDto(
	                        report.getDate(),
	                        report.getDay(),
	                        calculateTotalFoodEntries(report)
	                ))
	                .collect(Collectors.toList());

	        List<SimplifiedWeeklyReportDto> previousWeekReport = weeklyReport.stream()
	                .filter(report -> {
	                    LocalDate reportDate = report.getDate();
	                    return !reportDate.isBefore(startOfPreviousWeek) && reportDate.isBefore(startOfCurrentWeek);
	                })
	                .map(report -> new SimplifiedWeeklyReportDto(
	                        report.getDate(),
	                        report.getDay(),
	                        calculateTotalFoodEntries(report)
	                ))
	                .collect(Collectors.toList());


	        int currentWeekTotal = currentWeekReport.stream()
	                .mapToInt(SimplifiedWeeklyReportDto::getTotalFoodEntries)
	                .sum();
	        
	        int previousWeekTotal = previousWeekReport.stream()
	                .mapToInt(SimplifiedWeeklyReportDto::getTotalFoodEntries)
	                .sum();

	        model.addAttribute("currentWeekReport", currentWeekReport);
	        model.addAttribute("previousWeekReport", previousWeekReport);
	        model.addAttribute("currentWeekTotal", currentWeekTotal);
	        model.addAttribute("previousWeekTotal", previousWeekTotal);
	    } catch (Exception e) {
	        model.addAttribute("error", "Error fetching weekly food report: " + e.getMessage());
	    }

	    // Add month and year selection options
	    List<Map<String, String>> months = IntStream.rangeClosed(1, 12)
	            .mapToObj(m -> Map.of("value", String.valueOf(m), 
	                                 "displayName", Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH)))
	            .toList();
	    model.addAttribute("months", months);

	    int currentYear = currentDate.getYear();
	    List<Integer> years = IntStream.rangeClosed(currentYear - 5, currentYear + 5).boxed().toList();
	    model.addAttribute("years", years);

	    model.addAttribute("selectedMonth", String.valueOf(month));
	    model.addAttribute("selectedYear", year);
	    model.addAttribute("startDate", startDate);
	    model.addAttribute("endDate", endDate);
	    model.addAttribute("user", userResponseDto);

	    return "adminDashboard";
	}
	
	private int calculateTotalFoodEntries(WeeklyFoodReportResponseDto report) {
	    return report.getUserDetails().stream()
	            .mapToInt(userDetail -> userDetail.getFoodResponses().size())
	            .sum();
	}



}
