package com.mvcjavacode.repository;

import com.mvcjavacode.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
