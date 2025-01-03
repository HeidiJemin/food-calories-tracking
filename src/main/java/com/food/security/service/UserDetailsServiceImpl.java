package com.food.security.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.food.entity.User;
import com.food.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    	System.out.println("-------------loadUserByUsername--------------->"+email);
        User user = userRepo.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Email id not found : " + email);
        }
        UserDetailsImpl build = UserDetailsImpl.build(user);
        System.out.println(build);
        return UserDetailsImpl.build(user);
    }
}