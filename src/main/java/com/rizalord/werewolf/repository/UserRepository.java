package com.rizalord.werewolf.repository;


import com.rizalord.werewolf.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM users WHERE u.line_user_id = ?1")
    User findByLineUserId(String id);
}
