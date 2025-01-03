package com.food.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.food.entity.User;
import com.food.exception.AppException;
import com.food.exception.NotFoundException;
import com.food.repository.UserRepository;

@Service
public class UserService {

	@Autowired
	UserRepository userRepository;

	public User getUserById(Long id) {
		try {
			Optional<User> user = userRepository.findById(id);
			if (user.isPresent()) {
				return user.get();
			} else {
				throw new NotFoundException("User id not found");
			}
		} catch (NotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public List<User> getAllUsers() {
		try {
			return userRepository.findAll();
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

}
