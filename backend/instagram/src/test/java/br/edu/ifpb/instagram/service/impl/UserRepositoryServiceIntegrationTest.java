package br.edu.ifpb.instagram.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import br.edu.ifpb.instagram.model.entity.UserEntity;

/**
 * Testes de integração específicos para validar cenários reais do UserService
 * Complementa os testes unitários com Mockito
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository - Cenários do UserService Integration Tests")
class UserRepositoryServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.flush();
    }

    private UserEntity createUser(String fullName, String email, String username, String encryptedPassword) {
        UserEntity user = new UserEntity();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setEncryptedPassword(encryptedPassword);
        return user;
    }

    @Test
    @DisplayName("createUser - Deve validar que email já existe impede criação")
    void createUser_shouldValidateEmailAlreadyExistsPreventCreation() {
        UserEntity existingUser = createUser(
            "José Luan Fernandes da Silva",
            "jose.luan@academico.ifpb.edu.br",
            "Luan Fernandes",
            "encrypted123"
        );
        entityManager.persistAndFlush(existingUser);

        assertTrue(userRepository.existsByEmail("jose.luan@academico.ifpb.edu.br"));
        assertFalse(userRepository.existsByEmail("outro@email.com"));
    }

    @Test
    @DisplayName("createUser - Deve validar que username já existe impede criação")
    void createUser_shouldValidateUsernameAlreadyExistsPreventCreation() {
        UserEntity existingUser = createUser(
            "José Luan Fernandes da Silva",
            "jose.luan@academico.ifpb.edu.br",
            "Luan Fernandes",
            "encrypted123"
        );
        entityManager.persistAndFlush(existingUser);

        assertTrue(userRepository.existsByUsername("Luan Fernandes"));
        assertFalse(userRepository.existsByUsername("OutroUsername"));
    }

    @Test
    @DisplayName("createUser - Deve permitir criação quando email e username são únicos")
    void createUser_shouldAllowCreationWhenEmailAndUsernameAreUnique() {
        assertFalse(userRepository.existsByEmail("novo@email.com"));
        assertFalse(userRepository.existsByUsername("novousuario"));

        UserEntity newUser = createUser(
            "Novo Usuário",
            "novo@email.com",
            "novousuario",
            "encrypted999"
        );
        UserEntity savedUser = userRepository.save(newUser);
        entityManager.flush();

        assertNotNull(savedUser.getId());
        assertEquals("Novo Usuário", savedUser.getFullName());
        assertEquals("novo@email.com", savedUser.getEmail());
        assertEquals("novousuario", savedUser.getUsername());
    }

    @Test
    @DisplayName("updateUser - Deve encontrar usuário existente para atualização")
    void updateUser_shouldFindExistingUserForUpdate() {
        UserEntity existingUser = createUser(
            "José Luan Fernandes da Silva",
            "jose.luan@academico.ifpb.edu.br",
            "Luan Fernandes",
            "password123"
        );
        UserEntity savedUser = entityManager.persistAndFlush(existingUser);

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("José Luan Fernandes da Silva", foundUser.get().getFullName());
    }

    @Test
    @DisplayName("updateUser - Deve retornar empty para usuário inexistente")
    void updateUser_shouldReturnEmptyForNonExistentUser() {
        Optional<UserEntity> foundUser = userRepository.findById(999L);
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("updateUser - Deve atualizar dados do usuário corretamente")
    void updateUser_shouldUpdateUserDataCorrectly() {
        UserEntity existingUser = createUser(
            "Nome Original",
            "original@email.com",
            "originaluser",
            "originalpassword"
        );
        UserEntity savedUser = entityManager.persistAndFlush(existingUser);
        entityManager.clear();

        Optional<UserEntity> userToUpdate = userRepository.findById(savedUser.getId());
        assertTrue(userToUpdate.isPresent());

        UserEntity user = userToUpdate.get();
        user.setFullName("Nome Atualizado");
        user.setEmail("atualizado@email.com");
        user.setUsername("useratualizado");
        user.setEncryptedPassword("newencryptedpassword");

        UserEntity updatedUser = userRepository.save(user);
        entityManager.flush();

        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Nome Atualizado", updatedUser.getFullName());
        assertEquals("atualizado@email.com", updatedUser.getEmail());
        assertEquals("useratualizado", updatedUser.getUsername());
        assertEquals("newencryptedpassword", updatedUser.getEncryptedPassword());
    }

    @Test
    @DisplayName("deleteUser - Deve deletar usuário existente com sucesso")
    void deleteUser_shouldDeleteExistingUserSuccessfully() {
        UserEntity existingUser = createUser(
            "Paulo Pereira",
            "paulo@ppereira.dev",
            "paulodev",
            "senhaCriptografada"
        );
        UserEntity savedUser = entityManager.persistAndFlush(existingUser);
        Long userId = savedUser.getId();

        assertTrue(userRepository.existsById(userId));
        userRepository.deleteById(userId);
        entityManager.flush();

        assertFalse(userRepository.existsById(userId));
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("findById - Deve encontrar usuário existente e retornar dados corretos")
    void findById_shouldFindExistingUserAndReturnCorrectData() {
        UserEntity existingUser = createUser(
            "Paulo Pereira",
            "paulo@ppereira.dev",
            "paulodev",
            "senhaCriptografada"
        );
        UserEntity savedUser = entityManager.persistAndFlush(existingUser);

        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        UserEntity user = foundUser.get();

        assertEquals(savedUser.getId(), user.getId());
        assertEquals("Paulo Pereira", user.getFullName());
        assertEquals("paulodev", user.getUsername());
        assertEquals("paulo@ppereira.dev", user.getEmail());
        assertEquals("senhaCriptografada", user.getEncryptedPassword());
    }

    @Test
    @DisplayName("findById - Deve retornar empty para usuário inexistente")
    void findById_shouldReturnEmptyForNonExistentUser() {
        Optional<UserEntity> foundUser = userRepository.findById(999L);
        assertFalse(foundUser.isPresent());
    }
}
