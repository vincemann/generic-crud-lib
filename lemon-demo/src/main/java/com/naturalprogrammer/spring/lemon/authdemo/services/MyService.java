package com.naturalprogrammer.spring.lemon.authdemo.services;

import com.naturalprogrammer.spring.lemon.authdemo.entities.User;
import com.naturalprogrammer.spring.lemon.auth.service.LemonServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MyService extends LemonServiceImpl<User, Long> {


	//is ok
	@Override
    public User newUser() {
        return new User();
    }

//	@Override
//    protected void updateUserFields(User user, User updatedUser, UserDto currentUser) {
//
//        super.updateUserFields(user, updatedUser, currentUser);
//
//        user.setName(updatedUser.getName());
//
//        LecjUtils.afterCommit(() -> {
//            if (currentUser.getId().equals(user.getId().toString()))
//                currentUser.setTag(user.toTag());
//        });
//    }


	@Override
	public Long toId(String id) {
		
		return Long.valueOf(id);
	}
}