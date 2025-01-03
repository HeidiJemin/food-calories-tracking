package com.food.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.food.constant.RoleType;
import com.food.entity.User;
import com.food.exception.AlreadyExistsException;
import com.food.exception.AppException;
import com.food.repository.UserRepository;
import com.food.request.payload.LoginDto;
import com.food.request.payload.UserDto;
import com.food.response.payload.UserResponseDto;
import com.food.security.service.UserDetailsImpl;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	AuthenticationManager authenticationManager;

	public User register(UserDto userDto) {
		try {
			
			User existUser = userRepository.findByEmail(userDto.getEmail());
			if (existUser != null) {
				throw new AlreadyExistsException("Email already exist..!");
			}
			
			User user = new User();
			user.setName(userDto.getName());
			user.setEmail(userDto.getEmail());
			user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			user.setRole(RoleType.ROLE_USER.name());
			user.setCreatedAt(LocalDateTime.now());
			return userRepository.save(user);
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
	}

	public UserResponseDto login(LoginDto loginDto) {
	    try {
	        Authentication authentication = authenticationManager.authenticate(
	            new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
	        System.out.println("Authorities authentication: " + authentication.getAuthorities());
	        SecurityContextHolder.getContext().setAuthentication(authentication);

	        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
	        
	        if (userDetails == null) {
	            throw new AppException("User details are null, unable to invalid login");
	        }


	        UserResponseDto userResponseDto = new UserResponseDto();
	        userResponseDto.setId(userDetails.getId());
	        userResponseDto.setEmail(userDetails.getEmail());
	        userResponseDto.setName(userDetails.getName());
	        userResponseDto.setRole(userDetails.getRole());

	        return userResponseDto;
	    } catch (Exception e) {
	        throw new AppException(e.getMessage());
	    }
	}


}
