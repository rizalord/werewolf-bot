package com.rizalord.werewolf.repository;

import com.rizalord.werewolf.models.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;

public interface HistoryRepository extends JpaRepository<History, Integer>, CustomHistoryRepository {
    @Query(value = "SELECT * FROM histories WHERE group_id = :id", nativeQuery = true)
    ArrayList<History> findHistoryByGroupId(@Param("id") int id);

    // DELETE
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM histories WHERE group_id = :id", nativeQuery = true)
    void deleteHistoryByGroupId(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM histories WHERE group_id = :id", nativeQuery = true)
    void endGame3ByGroupId(@Param("id") int id);
}

interface CustomHistoryRepository {
    void refresh();
}

class CustomHistoryRepositoryImpl implements CustomHistoryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void refresh() {
        em.getEntityManagerFactory().getCache().evictAll();
        em.clear();
    }

}
