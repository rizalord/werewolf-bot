package com.rizalord.werewolf.repository;

import com.rizalord.werewolf.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Integer> {
}
