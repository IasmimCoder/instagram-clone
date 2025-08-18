package br.edu.ifpb.instagram.repository;

import br.edu.ifpb.instagram.model.entity.UserEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindUser() {
        UserEntity user = new UserEntity();
        user.setFullName("Teste Usuário");
        user.setUsername("testeuser");
        user.setEmail("teste@email.com");
        user.setEncryptedPassword("1234");

        UserEntity savedUser = userRepository.save(user);
        entityManager.flush(); // força persistir no DB

        Optional<UserEntity> found = userRepository.findById(savedUser.getId());
        assertTrue(found.isPresent());
        assertEquals("testeuser", found.get().getUsername());
    }

    @Test
    void shouldCheckExistsByEmailAndUsername() {
        UserEntity user = new UserEntity();
        user.setFullName("Outro Usuário");
        user.setUsername("user1");
        user.setEmail("email@teste.com");
        user.setEncryptedPassword("senha");
        userRepository.save(user);
        entityManager.flush();

        assertTrue(userRepository.existsByEmail("email@teste.com"));
        assertTrue(userRepository.existsByUsername("user1"));
        assertFalse(userRepository.existsByEmail("naoexiste@teste.com"));
        assertFalse(userRepository.existsByUsername("naoexiste"));
    }

    @Test
    void shouldDeleteUserById() {
        UserEntity user = new UserEntity();
        user.setFullName("Deletar Usuário");
        user.setUsername("deleteuser");
        user.setEmail("delete@email.com");
        user.setEncryptedPassword("123");
        UserEntity savedUser = userRepository.save(user);
        entityManager.flush();

        userRepository.deleteById(savedUser.getId());

        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }
}