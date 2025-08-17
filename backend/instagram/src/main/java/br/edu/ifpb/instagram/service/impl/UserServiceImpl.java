package br.edu.ifpb.instagram.service.impl;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.exception.UserNotFoundException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.entity.UserEntity;
import br.edu.ifpb.instagram.repository.UserRepository;
import br.edu.ifpb.instagram.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.email())) {
            throw new FieldAlreadyExistsException("E-email already in use.");
        }

        if (userRepository.existsByUsername(userDto.username())) {
            throw new FieldAlreadyExistsException("Username already in use.");
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDto.username());
        userEntity.setEmail(userDto.email());
        userEntity.setFullName(userDto.fullName());
        userEntity.setEncryptedPassword(passwordEncoder.encode(userDto.password()));

        UserEntity storedUserEntity = userRepository.save(userEntity);

        return mapToDto(storedUserEntity); // 3. Usando método auxiliar
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        if (userDto == null || userDto.id() == null) {
            throw new IllegalArgumentException("UserDto or UserDto.id must not be null");
        }

        UserEntity userEntityToUpdate = userRepository.findById(userDto.id())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userDto.id()));

        userEntityToUpdate.setFullName(userDto.fullName());
        userEntityToUpdate.setUsername(userDto.username());
        userEntityToUpdate.setEmail(userDto.email());

        if (userDto.password() != null && !userDto.password().trim().isEmpty()) {
            userEntityToUpdate.setEncryptedPassword(passwordEncoder.encode(userDto.password()));
        }

        UserEntity updatedUser = userRepository.save(userEntityToUpdate);

        return mapToDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto findById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return mapToDto(userEntity);
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(UserEntity userEntity) {
        return new UserDto(
                userEntity.getId(),
                userEntity.getFullName(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                null,
                null
        );
    }
}
