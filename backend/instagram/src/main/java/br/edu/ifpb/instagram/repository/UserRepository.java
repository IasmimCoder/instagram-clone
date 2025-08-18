package br.edu.ifpb.instagram.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Métodos case-insensitive
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);

    Optional<UserEntity> findByUsername(String username);
    List<UserEntity> findAll();

    @Modifying
    @Transactional
    @Query("UPDATE users u SET " +
           "u.fullName = COALESCE(:fullName, u.fullName), " +
           "u.email = COALESCE(:email, u.email), " +
           "u.username = COALESCE(:username, u.username), " +
           "u.encryptedPassword = COALESCE(:encryptedPassword, u.encryptedPassword) " +
           "WHERE u.id = :id")
    int updatePartialUser(
            @org.springframework.lang.Nullable String fullName,
            @org.springframework.lang.Nullable String email,
            @org.springframework.lang.Nullable String username,
            @org.springframework.lang.Nullable String encryptedPassword,
            Long id);
}
