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

import com.food.response.payload.UserResponseDto;

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

	

}
