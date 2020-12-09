package com.rizalord.werewolf.repository;

import com.rizalord.werewolf.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {
}
