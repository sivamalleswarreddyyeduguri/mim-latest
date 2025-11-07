package com.hcl.mi.services;

import com.hcl.mi.requestdtos.NewUser;

public interface UserService {
	void saveUser(NewUser user);

	boolean checkUserCredentails(String username, String password);
}
 