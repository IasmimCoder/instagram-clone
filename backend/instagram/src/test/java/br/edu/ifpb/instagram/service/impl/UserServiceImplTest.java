package br.edu.ifpb.instagram.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.deleteUser(userId));
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

        RuntimeException exception = assertThrows(UserNotFoundException.class, () -> {
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

        assertEquals(user1.getId(), userDtos.get(0).id());
        assertEquals(user1.getFullName(), userDtos.get(0).fullName());
        assertEquals(user1.getEmail(), userDtos.get(0).email());

        assertEquals(user2.getId(), userDtos.get(1).id());
        assertEquals(user2.getFullName(), userDtos.get(1).fullName());
        assertEquals(user2.getEmail(), userDtos.get(1).email());

        // Verificar a interação com o mock
        verify(userRepository, times(1)).findAll();
    }
}
