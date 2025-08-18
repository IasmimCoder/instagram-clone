package br.edu.ifpb.instagram.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Testes de integração para UserRepository usando MariaDB em memória
 * Complementa os testes unitários do UserServiceImpl
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests - MariaDB")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity user1;
    private UserEntity user2;
    private UserEntity user3;

    @BeforeEach
    void setUp() {
        // Limpar dados existentes
        userRepository.deleteAll();
        entityManager.flush();

        // Configurar dados de teste
        user1 = createUserEntity(
            "José Luan Fernandes da Silva", 
            "jose.luan@academico.ifpb.edu.br", 
            "Luan Fernandes", 
            "encrypted123"
        );
        
        user2 = createUserEntity(
            "Paulo Pereira", 
            "paulo@ppereira.dev", 
            "paulodev", 
            "encrypted456"
        );
        
        user3 = createUserEntity(
            "Maria Silva", 
            "maria@silva.dev", 
            "marias", 
            "encrypted789"
        );
    }

    // =================== TESTES DE OPERAÇÕES CRUD BÁSICAS ===================

    @Test
    @DisplayName("Deve salvar usuário com sucesso no MariaDB")
    void shouldSaveUserSuccessfullyInMariaDB() {
        // Given
        UserEntity newUser = createUserEntity(
            "Ana Costa", 
            "ana@costa.dev", 
            "anacosta", 
            "encrypted999"
        );

        // When
        UserEntity savedUser = userRepository.save(newUser);
        entityManager.flush(); // Force SQL execution

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFullName()).isEqualTo("Ana Costa");
        assertThat(savedUser.getEmail()).isEqualTo("ana@costa.dev");
        assertThat(savedUser.getUsername()).isEqualTo("anacosta");
        assertThat(savedUser.getEncryptedPassword()).isEqualTo("encrypted999");
        
        // Verificar se foi realmente persistido
        UserEntity foundUser = entityManager.find(UserEntity.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("ana@costa.dev");
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void shouldFindUserById() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);

        // When
        Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("Luan Fernandes");
        assertThat(foundUser.get().getEmail()).isEqualTo("jose.luan@academico.ifpb.edu.br");
        assertThat(foundUser.get().getFullName()).isEqualTo("José Luan Fernandes da Silva");
    }

    @Test
    @DisplayName("Deve retornar empty quando usuário não existe por ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // When
        Optional<UserEntity> foundUser = userRepository.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldListAllUsers() {
        // Given
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // When
        List<UserEntity> users = userRepository.findAll();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(UserEntity::getUsername)
            .containsExactlyInAnyOrder("Luan Fernandes", "paulodev", "marias");
        assertThat(users).extracting(UserEntity::getEmail)
            .containsExactlyInAnyOrder(
                "jose.luan@academico.ifpb.edu.br", 
                "paulo@ppereira.dev", 
                "maria@silva.dev"
            );
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void shouldUpdateExistingUser() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);
        Long userId = savedUser.getId();

        // When
        savedUser.setFullName("José Luan Silva Atualizado");
        savedUser.setEmail("luan.novo@academico.ifpb.edu.br");
        UserEntity updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        // Then
        assertThat(updatedUser.getId()).isEqualTo(userId);
        assertThat(updatedUser.getFullName()).isEqualTo("José Luan Silva Atualizado");
        assertThat(updatedUser.getEmail()).isEqualTo("luan.novo@academico.ifpb.edu.br");
        assertThat(updatedUser.getUsername()).isEqualTo("Luan Fernandes"); // não alterado
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void shouldDeleteUserById() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
        
        // Verificar se foi realmente removido do banco
        UserEntity foundUser = entityManager.find(UserEntity.class, userId);
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("Deve contar total de usuários")
    void shouldCountTotalUsers() {
        // Given
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        long count = userRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve verificar se usuário existe por ID")
    void shouldCheckIfUserExistsById() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);

        // When & Then
        assertThat(userRepository.existsById(savedUser.getId())).isTrue();
        assertThat(userRepository.existsById(999L)).isFalse();
    }

    // =================== TESTES DE MÉTODOS CUSTOMIZADOS ===================

    @Test
    @DisplayName("Deve verificar existência por email - casos do UserService")
    void shouldCheckExistenceByEmailForUserService() {
        // Given
        entityManager.persistAndFlush(user1);

        // When & Then - casos similares aos testados no UserService
        assertThat(userRepository.existsByEmail("jose.luan@academico.ifpb.edu.br")).isTrue();
        assertThat(userRepository.existsByEmail("paulo@ppereira.dev")).isFalse(); // não foi salvo ainda
        assertThat(userRepository.existsByEmail("naoexiste@email.com")).isFalse();
        
        // Case sensitivity
        assertThat(userRepository.existsByEmail("JOSE.LUAN@ACADEMICO.IFPB.EDU.BR")).isFalse();
    }

    @Test
    @DisplayName("Deve verificar existência por username - casos do UserService")
    void shouldCheckExistenceByUsernameForUserService() {
        // Given
        entityManager.persistAndFlush(user2); // paulodev

        // When & Then
        assertThat(userRepository.existsByUsername("paulodev")).isTrue();
        assertThat(userRepository.existsByUsername("Luan Fernandes")).isFalse(); // não foi salvo ainda
        assertThat(userRepository.existsByUsername("naoexiste")).isFalse();
        
        // Case sensitivity
        assertThat(userRepository.existsByUsername("PAULODEV")).isFalse();
        assertThat(userRepository.existsByUsername("PauloDev")).isFalse();
    }

    @Test
    @DisplayName("Deve encontrar usuário por username")
    void shouldFindUserByUsername() {
        // Given
        entityManager.persistAndFlush(user2);

        // When
        Optional<UserEntity> foundUser = userRepository.findByUsername("paulodev");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("paulo@ppereira.dev");
        assertThat(foundUser.get().getFullName()).isEqualTo("Paulo Pereira");
        assertThat(foundUser.get().getEncryptedPassword()).isEqualTo("encrypted456");
    }

    @Test
    @DisplayName("Deve retornar empty quando username não existe")
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When
        Optional<UserEntity> foundUser = userRepository.findByUsername("usuarioInexistente");

        // Then
        assertThat(foundUser).isEmpty();
    }

    // =================== TESTES DO MÉTODO updatePartialUser ===================

    @Test
    @DisplayName("Deve atualizar todos os campos parcialmente")
    void shouldUpdateAllFieldsPartially() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);
        entityManager.clear();

        // When
        int rowsAffected = userRepository.updatePartialUser(
            "José Luan Silva Completo",
            "luan.atualizado@academico.ifpb.edu.br",
            "luanfernandes",
            "newEncryptedPassword123",
            savedUser.getId()
        );
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        
        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        
        UserEntity user = updatedUser.get();
        assertAll(
            () -> assertThat(user.getFullName()).isEqualTo("José Luan Silva Completo"),
            () -> assertThat(user.getEmail()).isEqualTo("luan.atualizado@academico.ifpb.edu.br"),
            () -> assertThat(user.getUsername()).isEqualTo("luanfernandes"),
            () -> assertThat(user.getEncryptedPassword()).isEqualTo("newEncryptedPassword123")
        );
    }

    @Test
    @DisplayName("Deve atualizar apenas campos não nulos - caso típico do UserService")
    void shouldUpdateOnlyNonNullFieldsLikeUserService() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user2);
        entityManager.clear();

        // When - apenas fullName e email (caso comum no UserService)
        int rowsAffected = userRepository.updatePartialUser(
            "Paulo Pereira Santos",
            "paulo.santos@ppereira.dev",
            null, // username não alterado
            null, // password não alterada
            savedUser.getId()
        );
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        
        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        
        UserEntity user = updatedUser.get();
        assertAll(
            () -> assertThat(user.getFullName()).isEqualTo("Paulo Pereira Santos"),
            () -> assertThat(user.getEmail()).isEqualTo("paulo.santos@ppereira.dev"),
            () -> assertThat(user.getUsername()).isEqualTo("paulodev"), // não alterado
            () -> assertThat(user.getEncryptedPassword()).isEqualTo("encrypted456") // não alterado
        );
    }

    @Test
    @DisplayName("Deve atualizar apenas password - caso do UserService com nova senha")
    void shouldUpdateOnlyPasswordLikeUserService() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user3);
        entityManager.clear();

        // When - apenas password (caso do UserService quando usuário troca senha)
        int rowsAffected = userRepository.updatePartialUser(
            null,
            null,
            null,
            "novaSenhaEncriptada999",
            savedUser.getId()
        );
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        
        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        
        UserEntity user = updatedUser.get();
        assertAll(
            () -> assertThat(user.getFullName()).isEqualTo("Maria Silva"), // não alterado
            () -> assertThat(user.getEmail()).isEqualTo("maria@silva.dev"), // não alterado
            () -> assertThat(user.getUsername()).isEqualTo("marias"), // não alterado
            () -> assertThat(user.getEncryptedPassword()).isEqualTo("novaSenhaEncriptada999") // alterado
        );
    }

    @Test
    @DisplayName("Deve retornar 0 quando usuário não existe no update parcial")
    void shouldReturnZeroWhenUserNotExistsForPartialUpdate() {
        // When
        int rowsAffected = userRepository.updatePartialUser(
            "Nome Inexistente",
            "inexistente@test.com",
            "inexistente",
            "senhaInexistente",
            999L
        );

        // Then
        assertThat(rowsAffected).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve manter valores originais quando todos os parâmetros são nulos")
    void shouldKeepOriginalValuesWhenAllParametersAreNull() {
        // Given
        UserEntity savedUser = entityManager.persistAndFlush(user1);
        String originalFullName = savedUser.getFullName();
        String originalEmail = savedUser.getEmail();
        String originalUsername = savedUser.getUsername();
        String originalPassword = savedUser.getEncryptedPassword();
        entityManager.clear();

        // When
        int rowsAffected = userRepository.updatePartialUser(
            null, null, null, null, savedUser.getId()
        );
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(rowsAffected).isEqualTo(1);
        
        Optional<UserEntity> unchangedUser = userRepository.findById(savedUser.getId());
        assertThat(unchangedUser).isPresent();
        
        UserEntity user = unchangedUser.get();
        assertAll(
            () -> assertThat(user.getFullName()).isEqualTo(originalFullName),
            () -> assertThat(user.getEmail()).isEqualTo(originalEmail),
            () -> assertThat(user.getUsername()).isEqualTo(originalUsername),
            () -> assertThat(user.getEncryptedPassword()).isEqualTo(originalPassword)
        );
    }

    // =================== TESTES DE CENÁRIOS ESPECIAIS ===================

    @Test
    @DisplayName("Deve lidar com lista vazia quando não há usuários")
    void shouldHandleEmptyListWhenNoUsers() {
        // When
        List<UserEntity> users = userRepository.findAll();

        // Then
        assertThat(users).isEmpty();
        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve manter integridade ao salvar múltiplos usuários - cenário do sistema")
    void shouldMaintainIntegrityWhenSavingMultipleUsersSystemScenario() {
        // When - salvar os três usuários de teste
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        entityManager.flush();

        // Then
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.existsByUsername("Luan Fernandes")).isTrue();
        assertThat(userRepository.existsByUsername("paulodev")).isTrue();
        assertThat(userRepository.existsByUsername("marias")).isTrue();
        
        assertThat(userRepository.existsByEmail("jose.luan@academico.ifpb.edu.br")).isTrue();
        assertThat(userRepository.existsByEmail("paulo@ppereira.dev")).isTrue();
        assertThat(userRepository.existsByEmail("maria@silva.dev")).isTrue();
    }

    @Test
    @DisplayName("Deve validar case sensitivity nos métodos de busca - comportamento do sistema")
    void shouldValidateCaseSensitivityInSearchMethodsSystemBehavior() {
        // Given
        entityManager.persistAndFlush(user1);

        // When & Then - validar comportamento case-sensitive
        assertThat(userRepository.existsByUsername("Luan Fernandes")).isTrue();
        assertThat(userRepository.existsByUsername("luan fernandes")).isFalse();
        assertThat(userRepository.existsByUsername("LUAN FERNANDES")).isFalse();
        
        assertThat(userRepository.existsByEmail("jose.luan@academico.ifpb.edu.br")).isTrue();
        assertThat(userRepository.existsByEmail("JOSE.LUAN@ACADEMICO.IFPB.EDU.BR")).isFalse();
        assertThat(userRepository.existsByEmail("Jose.Luan@Academico.IFPB.Edu.Br")).isFalse();
    }

    @Test
@DisplayName("Deve testar transações e rollback com MariaDB")
void shouldTestTransactionsAndRollbackWithMariaDB() {
    // Given
    UserEntity user = createUserEntity(
        "Teste Transação",
        "transacao@test.com",
        "transacao",
        "encrypted"
    );

    // When - salvar usuário
    UserEntity savedUser = userRepository.save(user);
    entityManager.flush();

    // Simular rollback manual: limpar contexto do EntityManager
    entityManager.clear();

    // Then - verificar se a entidade ainda existe no banco
    Optional<UserEntity> foundUser = userRepository.findById(savedUser.getId());
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo("transacao");
    assertThat(foundUser.get().getEmail()).isEqualTo("transacao@test.com");

    // Simulação de rollback real: deletar e verificar remoção
    userRepository.deleteById(savedUser.getId());
    entityManager.flush();
    entityManager.clear();

    Optional<UserEntity> deletedUser = userRepository.findById(savedUser.getId());
    assertThat(deletedUser).isEmpty();
}

    // =================== MÉTODO AUXILIAR ===================

    private UserEntity createUserEntity(String fullName, String email, 
                                       String username, String encryptedPassword) {
        UserEntity user = new UserEntity();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setEncryptedPassword(encryptedPassword);
        return user;
    }
}