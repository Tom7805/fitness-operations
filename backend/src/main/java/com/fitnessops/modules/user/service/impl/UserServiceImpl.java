package com.fitnessops.modules.user.service.impl;

import com.fitnessops.modules.user.entity.User;
import com.fitnessops.modules.user.repository.UserRepository;
import com.fitnessops.modules.user.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<User> findByUsernameForUpdate(String normalizedUsername) {
        return userRepository.findByUsernameForUpdate(normalizedUsername);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findWithAccessById(Long id) {
        return userRepository.findWithAccessById(id);
    }
}
