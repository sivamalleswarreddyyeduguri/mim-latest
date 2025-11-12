package com.hcl.mi.services;

import java.util.List;

import com.hcl.mi.entities.User;
import com.hcl.mi.requestdtos.NewUser;

public interface UserService {
	void saveUser(NewUser user);

//	boolean checkUserCredentails(String username, String password);
	
	User checkUserCredentails(String username, String password);

	List<NewUser> getAllUsers();

}
 