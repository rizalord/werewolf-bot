package com.rizalord.werewolf.repository;

import com.rizalord.werewolf.models.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Integer> {
}
