package com.rizalord.werewolf.repository;

import com.rizalord.werewolf.models.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    @Query(value = "SELECT * FROM groups WHERE group_id = :id", nativeQuery = true)
    Group findGroupByLineId(@Param("id") String id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM groups WHERE id = :id", nativeQuery = true)
    void deleteGroupById(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM groups WHERE id = :id", nativeQuery = true)
    void endGame2ByGroupId(@Param("id") int id);

}
