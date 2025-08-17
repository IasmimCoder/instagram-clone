package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        mockUserEntity.setEmail("paulo@ppereira.dev");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUserEntity));

        // Executar o método a ser testado
        UserDto userDto = userService.findById(userId);

        // Verificar o resultado
        assertNotNull(userDto);
        assertEquals(mockUserEntity.getId(), userDto.id());
        assertEquals(mockUserEntity.getFullName(), userDto.fullName());
        assertEquals(mockUserEntity.getEmail(), userDto.email());

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
        // Configurar o comportamento do mock
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setFullName("Paulo Pereira");
        user1.setUsername("paulodev");
        user1.setEmail("paulo@ppereira.dev");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setFullName("Maria Silva");
        user2.setUsername("marias");
        user2.setEmail("maria@silva.dev");

        List<UserEntity> mockUsers = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(mockUsers);

        // Executar o método a ser testado
        List<UserDto> userDtos = userService.findAll();

        // Verificar o resultado
        assertNotNull(userDtos);
        assertEquals(2, userDtos.size());

        UserDto userDto1 = userDtos.getFirst();

        assertEquals(user1.getId(), userDto1.id());
        assertEquals(user1.getFullName(), userDto1.fullName());
        assertEquals(user1.getUsername(), userDto1.username());
        assertEquals(user1.getEmail(), userDto1.email());

        assertEquals(user2.getId(), userDtos.get(1).id());
        assertEquals(user2.getFullName(), userDtos.get(1).fullName());
        assertEquals(user2.getUsername(), userDtos.get(1).username());
        assertEquals(user2.getEmail(), userDtos.get(1).email());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findAll();
    }
}
