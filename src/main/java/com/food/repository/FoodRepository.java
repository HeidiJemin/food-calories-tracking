package com.food.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.food.entity.Food;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

	List<Food> findByUserIdAndDate(Long userId, LocalDate date);

	List<Food> findByUserIdAndDateBetween(long userId, LocalDate startDate, LocalDate endDate);

	Page<Food> findByUserIdAndDateBetween(long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

	long countByDateBetween(LocalDate startDate, LocalDate endDate);

	Page<Food> findByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

	List<Food> findAllByDateBetween(LocalDate startDate, LocalDate endDate);

}
