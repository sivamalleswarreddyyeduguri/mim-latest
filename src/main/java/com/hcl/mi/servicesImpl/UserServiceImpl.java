package com.hcl.mi.servicesImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hcl.mi.entities.User;
import com.hcl.mi.entities.Vendor;
import com.hcl.mi.exceptions.GenericAlreadyExistsException;
import com.hcl.mi.exceptions.GenericNotFoundException;
import com.hcl.mi.mapper.UserMapper;
import com.hcl.mi.repositories.UserRepository;
import com.hcl.mi.requestdtos.NewUser;
import com.hcl.mi.services.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	private final UserRepository userRepo;

	private final PasswordEncoder passwordEncoder;
 
	@Lazy
	public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
		super();
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
	}
 
	@Override 
	public void saveUser(NewUser user) {
		String username = user.getUsername().toLowerCase();
		String email = user.getEmail().toLowerCase();

		Optional<User> existingUser = userRepo.findByUsernameOrEmail(username, email);
		if (existingUser.isPresent()) {
			User found = existingUser.get();
			if (found.getUsername().equalsIgnoreCase(username)) {
				throw new GenericAlreadyExistsException("Username already exists");
			} else if (found.getEmail().equalsIgnoreCase(email)) {
				throw new GenericAlreadyExistsException("Email already exists");
			}
		}

		String assignedRole = (user.getRole() != null && !user.getRole().isBlank()) ? user.getRole().toUpperCase()
				: "USER";

		User newUser = User.builder().username(username.toLowerCase())
				.password(passwordEncoder.encode(user.getPassword())).email(email).mobileNum(user.getMobileNum())
				.status("Active")
				.role(assignedRole).build();

		userRepo.save(newUser);
	}
 
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.hcl.mi.entities.User u = userRepo.findByUsername(username.toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		return org.springframework.security.core.userdetails.User.builder().username(u.getUsername())
				.password(u.getPassword()).roles(u.getRole()).build();
	}

	@Override
	public User checkUserCredentails(String username, String password) {
		return userRepo.findByUsername(username).filter(user -> passwordEncoder.matches(password, user.getPassword()))
				.orElse(null);
	}

	@Override
	public List<NewUser> getAllUsers() {

		return userRepo.findAll().stream().map(u -> UserMapper.convertEntityToDto(u)).toList();
	}

	@Override
	@Transactional
	public void updateUser(Integer id, NewUser dto) {
		
		
		Optional<User> Optionaluser = userRepo.findById(id);
					
         User user = Optionaluser.get(); 
         
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername()); 
        user.setMobileNum(dto.getMobileNum()); 

		if (dto.getPassword() != null) {
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
		}

		userRepo.save(user); 
	}

	@Override
	public void deleteVendor(Integer id) {
		
		 User user = userRepo.findById(id)
		            .orElseThrow(() -> new GenericNotFoundException("User with ID " + id + " does not exist."));

		    user.setStatus("Inactive"); 
		    userRepo.save(user);
	}
 
}
