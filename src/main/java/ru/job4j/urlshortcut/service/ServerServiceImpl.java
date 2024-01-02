package ru.job4j.urlshortcut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.repository.ServerRepository;
import ru.job4j.urlshortcut.util.EntityNotFoundException;
import ru.job4j.urlshortcut.util.UnauthorizedException;

import java.util.UUID;

/** {@code Server}-specific service to manage entries. */
@RequiredArgsConstructor
@Service
public class ServerServiceImpl implements ServerService {

    private final PasswordEncoder passwordEncoder;
    private final ServerRepository serverRepository;

    /** {@inheritDoc} */
    @Override
    public Server create(Server server) {
        server.setPassword(passwordEncoder.encode(server.getPassword()));
        return serverRepository.save(server);
    }

    /** {@inheritDoc} */
    @Override
    public Server getById(UUID uuid) {
        return serverRepository.findById(uuid).orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean updatePassword(UUID uuid, String old, String current) {
        Server server = getById(uuid);
        if (!passwordEncoder.matches(old, server.getPassword())) {
            throw new UnauthorizedException("Password is incorrect.");
        }
        return serverRepository.updatePasswordById(uuid, passwordEncoder.encode(current)) > 0;
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(UUID uuid) {
        return serverRepository.deleteByIdAndReturnCount(uuid) > 0;
    }
}
