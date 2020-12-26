package com.rizalord.werewolf.repository;


import com.rizalord.werewolf.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;

public interface UserRepository extends JpaRepository<User, Integer>, CustomUserRepository {

    // GETTER

    @Query(value = "SELECT * FROM users WHERE id = :userid", nativeQuery = true)
    User findUserById(@Param("userid") int userid);

    @Query(value = "SELECT * FROM users WHERE line_user_id = :id", nativeQuery = true)
    User findUserByLineUserId(@Param("id") String id);

    @Query(value = "SELECT * FROM users WHERE role_id = 2 AND group_id = :id", nativeQuery = true)
    User findWerewolf(@Param("id") int id);

    @Query(value = "SELECT COUNT(id) as total_user FROM users WHERE group_id = :id", nativeQuery = true)
    int countUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT COUNT(id) as total_user FROM users WHERE room_id = :id", nativeQuery = true)
    int countUsersByLineGroupId(@Param("id") String id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id", nativeQuery = true)
    ArrayList<User> findUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND role_id != 1 AND is_visibled = FALSE AND is_alive = TRUE ORDER BY created_at ASC", nativeQuery = true)
    ArrayList<User> findNotVisibledYetUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND role_id != 2 AND is_alive = TRUE ORDER BY created_at ASC", nativeQuery = true)
    ArrayList<User> findNotKilledYetUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND role_id != 3 AND is_alive = TRUE AND is_poisoned = FALSE ORDER BY created_at ASC", nativeQuery = true)
    ArrayList<User> findNotPoisonedYetUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND is_alive = TRUE ORDER BY created_at ASC", nativeQuery = true)
    ArrayList<User> findStillAliveUsersByGroupId(@Param("id") int id);

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND is_alive = TRUE AND id != :selfid ORDER BY created_at ASC", nativeQuery = true)
    ArrayList<User> findStillAliveUsersByGroupIdExceptSelfId(@Param("id") int id, @Param("selfid") int selfid );

    @Query(value = "SELECT * FROM users WHERE group_id = :id AND ww_voter >= 1 ORDER BY ww_voter DESC LIMIT 1", nativeQuery = true)
    User findVotedUsers(@Param("id") int id );

    @Query(value = "SELECT CASE WHEN ( (SELECT COUNT(id) FROM users WHERE role_id = 2 AND is_alive = TRUE AND group_id = :id ) > 0) THEN CAST (1 AS BIT) ELSE CAST(0 AS BIT) END", nativeQuery = true)
    boolean isWerewolfStillAlive(@Param("id") int id );

    @Query(value = "SELECT CASE WHEN ( (SELECT COUNT(id) FROM users WHERE role_id != 2 AND group_id = :id AND is_alive = TRUE) <= (SELECT COUNT(id) FROM users WHERE role_id = 2 AND group_id = :id AND is_alive = TRUE) ) THEN CAST (1 AS BIT) ELSE CAST(0 AS BIT) END", nativeQuery = true)
    boolean isWerewolfWin(@Param("id") int id );

    @Query(value = "SELECT COUNT(id) FROM users WHERE role_id != 2 AND group_id = :id", nativeQuery = true)
    int countNotWerewolfs(@Param("id") int id);

    @Query(value = "SELECT COUNT(id) FROM users WHERE role_id = 2 AND group_id = :id", nativeQuery = true)
    int countWerewolfs(@Param("id") int id);


    // DELETE
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE group_id = :id", nativeQuery = true)
    void deleteUserByGroupId(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE group_id = :id", nativeQuery = true)
    void endGame1ByGroupId(@Param("id") int id);


    // UPDATE
    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_voting = :vote WHERE group_id = :id", nativeQuery = true)
    void updateAllUserVoteState(@Param("id") int id, @Param("vote") boolean vote);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET ww_voter = ww_voter + 1 WHERE group_id = :groupid AND id = :userid", nativeQuery = true)
    void incrementVoter(@Param("groupid") int groupid,@Param("userid") int userid );

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET ww_voter = 0 WHERE group_id = :groupid", nativeQuery = true)
    void resetVoter(@Param("groupid") int groupid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_voting = FALSE WHERE group_id = :groupid", nativeQuery = true)
    void updateVotingToNotVoting(@Param("groupid") int groupid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_guarding = FALSE WHERE group_id = :groupid", nativeQuery = true)
    void updateGuardingToNotGuarding(@Param("groupid") int groupid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET is_poisoning = FALSE, is_visibling = FALSE, is_killing = FALSE WHERE group_id = :groupid", nativeQuery = true)
    void resetSpecialRoleState(@Param("groupid") int groupid);
}

interface CustomUserRepository {
    void refreshEntity(User user);

    void refreshAll();
}

class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void refreshAll() {
        em.getEntityManagerFactory().getCache().evictAll();
        em.clear();
    }

    @Override
    @Transactional
    public void refreshEntity(User user) {
        em.refresh(user);
    }


}
