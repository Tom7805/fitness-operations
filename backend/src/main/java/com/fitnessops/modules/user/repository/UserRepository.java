package com.fitnessops.modules.user.repository;

import com.fitnessops.modules.user.entity.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm tài khoản và khóa dòng ({@code SELECT … FOR UPDATE}) để các lần đăng nhập đồng thời của cùng một
     * tài khoản được xử lý tuần tự, bộ đếm sai không bị đếm thiếu.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);

    @EntityGraph(attributePaths = {"roles", "branchIds"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findWithAccessById(@Param("id") Long id);
}
