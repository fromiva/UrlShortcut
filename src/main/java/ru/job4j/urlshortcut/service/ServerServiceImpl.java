package ru.job4j.urlshortcut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Server;
import ru.job4j.urlshortcut.repository.ServerRepository;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.security.Principal;
import java.util.Objects;
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
        return serverRepository.findById(uuid)
                .orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    public Server getByIdAndHost(UUID uuid, String host) {
        return serverRepository.findByUuidAndHost(uuid, host)
                .orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    public Server getByHost(String host) {
        return serverRepository.findByHost(host)
                .orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean updatePasswordByIdAndPrincipal(UUID uuid, Principal principal, String password) {
        Server server = getById(uuid);
        if (!Objects.equals(server.getHost(), principal.getName())) {
            throw new AccessForbiddenException();
        }
        return serverRepository.updatePasswordByUuid(uuid, passwordEncoder.encode(password)) > 0;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean deleteByIdAndPrincipal(UUID uuid, Principal principal) {
        Server server = getById(uuid);
        if (!Objects.equals(server.getHost(), principal.getName())) {
            throw new AccessForbiddenException();
        }
        return serverRepository.deleteByUuid(uuid) > 0;
    }
}
