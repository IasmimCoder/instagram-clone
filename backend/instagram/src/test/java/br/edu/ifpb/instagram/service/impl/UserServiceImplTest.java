package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;

@SpringBootTest
public class UserServiceImplTest {

    @MockitoBean
    UserRepository userRepository; // Repositório simulado

    @Autowired
    UserServiceImpl userService; // Classe sob teste

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @Test
    void should_throwFieldAlreadyExistsException_when_emailAlreadyExists() {
        // Preparação do DTO de entrada
        UserDto userDto = new UserDto(
                null,
                "José Luan Fernandes da Silva",
                "Luan Fernandes",
                "jose.luan@academico.ifpb.edu.br",
                "password123",
                null
        );

        // Configurar o mock: o e-mail já existe
        when(userRepository.existsByEmail(userDto.email())).thenReturn(true);

        // Executar e verificar se a exceção é lançada
        FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> {
            userService.createUser(userDto);
        });

        assertEquals("E-email already in use.", exception.getMessage());

        // Verificar que o método de username e save não foram chamados
        verify(userRepository, times(1)).existsByEmail(userDto.email());
        verify(userRepository, times(0)).existsByUsername(anyString());
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void should_throwFieldAlreadyExistsException_when_usernameAlreadyExists() {
        // Preparação do DTO de entrada
        UserDto userDto = new UserDto(
                null,
                "José Luan Fernandes da Silva",
                "Luan Fernandes",
                "jose.luan@academico.ifpb.edu.br",
                "password123",
                null
        );

        // Configurar o mock: email não existe, mas username já existe
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(true);

        // Executar e verificar se a exceção é lançada
        FieldAlreadyExistsException exception = assertThrows(FieldAlreadyExistsException.class, () -> {
            userService.createUser(userDto);
        });

        assertEquals("Username already in use.", exception.getMessage());

        // Verificar interações com o mock
        verify(userRepository, times(1)).existsByEmail(userDto.email());
        verify(userRepository, times(1)).existsByUsername(userDto.username());
        verify(userRepository, times(0)).save(any(UserEntity.class));
    }

    @Test
    void should_createUser_when_emailAndUsernameDoNotExist() {
        // Preparação do DTO de entrada
        UserDto userDto = new UserDto(
                null,
                "José Luan Fernandes da Silva",
                "Luan Fernandes",
                "jose.luan@academico.ifpb.edu.br",
                "password123",
                null
        );

        // Configurar mocks
        when(userRepository.existsByEmail(userDto.email())).thenReturn(false);
        when(userRepository.existsByUsername(userDto.username())).thenReturn(false);

        // Simular a entidade salva retornada pelo repositório
        UserEntity savedUserEntity = new UserEntity();
        savedUserEntity.setId(1L);
        savedUserEntity.setFullName(userDto.fullName());
        savedUserEntity.setUsername(userDto.username());
        savedUserEntity.setEmail(userDto.email());
        savedUserEntity.setEncryptedPassword("encryptedPassword");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUserEntity);

        // Executar o método
        UserDto result = assertDoesNotThrow(() -> userService.createUser(userDto));

        // Verificar o retorno
        assertNotNull(result);
        assertEquals(savedUserEntity.getId(), result.id());
        assertEquals(savedUserEntity.getFullName(), result.fullName());
        assertEquals(savedUserEntity.getUsername(), result.username());
        assertEquals(savedUserEntity.getEmail(), result.email());
        assertNull(result.password()); // senha não é retornada

        // Verificar interações com o mock
        verify(userRepository, times(1)).existsByEmail(userDto.email());
        verify(userRepository, times(1)).existsByUsername(userDto.username());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testUpdateUser_WhenUserDtoIsNull_ShouldThrowIllegalArgumentException() {

        UserDto userDto = null;


        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userDto),
                "UserDto or UserDto.id must not be null");
    }

    @Test
    void testUpdateUser_WhenUserDtoIdIsNull_ShouldThrowIllegalArgumentException() {

        UserDto userDto = new UserDto(
                null,
                "Luan Fernandes",
                "Luan Fernandes",
                "jose.luan@academico.ifpb.edu.br",
                "password123",
                null
        );


        assertThrows(IllegalArgumentException.class,
                () -> userService.updateUser(userDto),
                "UserDto or UserDto.id must not be null");
    }

    @Test
    void testUpdateUser_WhenUserDoesNotExist_ShouldThrowUserNotFoundException() {
        // Preparar DTO de entrada com id existente
        UserDto userDto = new UserDto(
                999L, // id que não existe
                "Nome Teste",
                "usuarioTeste",
                "email.teste@test.com",
                "senha123",
                null
        );

        // Configurar mock: usuário não encontrado
        when(userRepository.findById(userDto.id())).thenReturn(Optional.empty());

        // Executar método e verificar se lança exceção
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(userDto));

        assertEquals("User not found with id: 999", exception.getMessage());

        // Verificar interação com o mock
        verify(userRepository, times(1)).findById(userDto.id());
        verify(userRepository, times(0)).save(any(UserEntity.class));
        verify(passwordEncoder, times(0)).encode(anyString());
    }


    @Test
    void testUpdateUser_WhenPasswordIsProvided_ShouldEncodeAndSetPassword() {
        // Preparar DTO de entrada com senha
        UserDto userDto = new UserDto(
                1L,
                "José Luan Fernandes da Silva",
                "Luan Fernandes",
                "jose.luan@academico.ifpb.edu.br",
                "newPassword123",
                null
        );

        // Preparar entidade existente retornada pelo repositório
        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setFullName("Old Name");
        existingUser.setUsername("oldUser");
        existingUser.setEmail("old.email@test.com");
        existingUser.setEncryptedPassword("oldEncryptedPassword");

        // Configurar mocks
        // simula que o usuário existe no banco e retorna existingUser
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        // simula que a senha será codificada, retornando "encodedPassword"
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");
        // simula que o usuário será salvo, retornando o próprio objeto passado (existingUser).
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executar método
        UserDto updatedDto = userService.updateUser(userDto);

        // Verificações
        assertNotNull(updatedDto);
        assertEquals("José Luan Fernandes da Silva", existingUser.getFullName());
        assertEquals("Luan Fernandes", existingUser.getUsername());
        assertEquals("jose.luan@academico.ifpb.edu.br", existingUser.getEmail());
        assertEquals("encodedPassword", existingUser.getEncryptedPassword());

        // Verificar interações com mocks
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void testUpdateUser_WhenPasswordIsEmpty_ShouldNotEncodeOrChangePassword() {
        // Preparar DTO de entrada com senha apenas espaços
        UserDto userDto = new UserDto(
                1L,
                "Nome Atualizado",
                "usuarioAtualizado",
                "email.atualizado@test.com",
                "   ", // senha com espaços
                null
        );

        // Preparar entidade existente retornada pelo repositório
        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setFullName("Nome Original");
        existingUser.setUsername("usuarioOriginal");
        existingUser.setEmail("email.original@test.com");
        existingUser.setEncryptedPassword("senhaExistente");

        // Configurar mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executar método
        UserDto updatedDto = userService.updateUser(userDto);

        // Verificações
        assertNotNull(updatedDto);
        assertEquals("Nome Atualizado", updatedDto.fullName());
        assertEquals("usuarioAtualizado", updatedDto.username());
        assertEquals("email.atualizado@test.com", updatedDto.email());

        // Senha não deve ter sido alterada
        assertEquals("senhaExistente", existingUser.getEncryptedPassword());

        // Verificar interações com mocks
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).encode(anyString()); // senha não deve ser codificada
        verify(userRepository, times(1)).save(existingUser);
    }


    @Test
    void testUpdateUser_WhenPasswordIsNotProvided_ShouldUpdateOtherFieldsOnly() {
        // Preparar DTO de entrada sem senha
        UserDto userDto = new UserDto(
                1L,
                "Novo Nome",
                "novoUser",
                "novo.email@test.com",
                null,
                null
        );

        // Preparar entidade existente retornada pelo repositório
        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setFullName("José Luan Fernandes da Silva");
        existingUser.setUsername("Luan Fernandes");
        existingUser.setEmail("jose.luan@academico.ifpb.edu.br");
        existingUser.setEncryptedPassword("password123");

        // Configurar mocks
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        // save retorna o mesmo objeto passado
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Executar método
        UserDto updatedDto = userService.updateUser(userDto);

        // Verificações
        assertNotNull(updatedDto);
        assertEquals("Novo Nome", updatedDto.fullName());
        assertEquals("novoUser", updatedDto.username());
        assertEquals("novo.email@test.com", updatedDto.email());
        assertEquals("password123", existingUser.getEncryptedPassword()); // senha permanece igual

        // Verificar interações com mocks
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(0)).encode(anyString()); // não deve codificar senha
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void deleteUser_existingUser_shouldDeleteSuccessfully() {
        Long userId = 1L;

        // Configurar o mock
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // Executar o método
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // Verificar a interação com o mock
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUser_userNotFound_shouldThrowException() {
        Long userId = 999L;

        // Configurar o mock
        when(userRepository.existsById(userId)).thenReturn(false);

        // Executar o método e verificar exceção
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        assertEquals("User not found with id: 999", exception.getMessage());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void findById_existingUser_shouldReturnUserDto() {
        // Configurar o comportamento do mock
        Long userId = 1L;

        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setId(userId);
        mockUserEntity.setFullName("Paulo Pereira");
        mockUserEntity.setUsername("paulodev");
        mockUserEntity.setEmail("paulo@ppereira.dev");
        mockUserEntity.setEncryptedPassword("senhaCriptografada");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUserEntity));

        // Executar o método a ser testado
        UserDto userDto = userService.findById(userId);

        // Verificar o resultado
        assertNotNull(userDto);
        assertEquals(mockUserEntity.getId(), userDto.id());
        assertEquals(mockUserEntity.getFullName(), userDto.fullName());
        assertEquals(mockUserEntity.getUsername(), userDto.username());
        assertEquals(mockUserEntity.getEmail(), userDto.email());

        // Esses campos devem SEMPRE vir nulos no DTO
        assertNull(userDto.password());
        assertNull(userDto.encryptedPassword());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findById_userNotFound_shouldThrowException() {
        // Configurar o comportamento do mock
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.findById(userId);
        });

        assertEquals("User not found with id: 999", exception.getMessage());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findAll_whenCalled_shouldReturnListOfUserDto() {

        // Configurar o comportamento do mock do repositório
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setFullName("Paulo Pereira");
        user1.setUsername("paulodev");
        user1.setEmail("paulo@ppereira.dev");
        user1.setEncryptedPassword("senhaCriptografada");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setFullName("Maria Silva");
        user2.setUsername("marias");
        user2.setEmail("maria@silva.dev");
        user2.setEncryptedPassword("senhaCriptografada");

        List<UserEntity> mockUsers = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Executar o método a ser testado
        List<UserDto> userDtos = userService.findAll();

       /* // Verificações
        assertNotNull(userDtos);
        assertEquals(2, userDtos.size());

        UserDto dto1 = userDtos.getFirst();
        //UserDto firstUser = userDtos.isEmpty() ? null : userDtos.get(0);
        assertEquals(user1.getId(), dto1.id());
        assertEquals(user1.getFullName(), dto1.fullName());
        assertEquals(user1.getUsername(), dto1.username());
        assertEquals(user1.getEmail(), dto1.email());

        assertNull(dto1.password());
        assertNull(dto1.encryptedPassword());

        UserDto dto2 = userDtos.get(1);
        assertEquals(user2.getId(), dto2.id());
        assertEquals(user2.getFullName(), dto2.fullName());
        assertEquals(user2.getUsername(), dto2.username());
        assertEquals(user2.getEmail(), dto2.email());

        assertNull(dto2.password());
        assertNull(dto2.encryptedPassword());*/
        // Verificações
        assertNotNull(userDtos);
        assertEquals(2, userDtos.size());

        // Pega o primeiro usuário de forma segura
        UserDto dto1 = userDtos.isEmpty() ? null : userDtos.get(0);
        assertNotNull(dto1); // garante que não é null

        assertEquals(user1.getId(), dto1.id());
        assertEquals(user1.getFullName(), dto1.fullName());
        assertEquals(user1.getUsername(), dto1.username());
        assertEquals(user1.getEmail(), dto1.email());

        assertNull(dto1.password());
        assertNull(dto1.encryptedPassword());

        // Segundo usuário
        UserDto dto2 = userDtos.get(1);
        assertEquals(user2.getId(), dto2.id());
        assertEquals(user2.getFullName(), dto2.fullName());
        assertEquals(user2.getUsername(), dto2.username());
        assertEquals(user2.getEmail(), dto2.email());

        assertNull(dto2.password());
        assertNull(dto2.encryptedPassword());


        // Verificar interação com o mock
        verify(userRepository, times(1)).findAll();
        verifyNoMoreInteractions(userRepository);
    }
}
