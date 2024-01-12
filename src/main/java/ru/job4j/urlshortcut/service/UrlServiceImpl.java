package ru.job4j.urlshortcut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.repository.UrlAccessRecordRepository;
import ru.job4j.urlshortcut.repository.UrlRepository;
import ru.job4j.urlshortcut.util.AccessForbiddenException;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** {@code Url}-specific service to manage entries. */
@RequiredArgsConstructor
@Service
public class UrlServiceImpl implements UrlService {

    private final ServerService serverService;
    private final UrlRepository repository;
    private final UrlAccessRecordRepository recordRepository;

    /** {@inheritDoc} */
    @Override
    public Url create(Url url, Principal principal) {
        String host = url.getUrl().getHost();
        if (!Objects.equals(host, principal.getName())) {
            throw new AccessForbiddenException();
        }
        url.setServerUuid(serverService.getByHost(host).getUuid());
        return repository.save(url);
    }

    /** {@inheritDoc} */
    @Override
    public Url getById(UUID uuid) {
        return repository.findById(uuid).orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    public Url getByIdAndLog(UUID uuid) {
        return repository.findByIdAndLog(uuid).orElseThrow(EntityNotFoundException::new);
    }

    /** {@inheritDoc} */
    @Override
    public List<Url> getAllByServerId(UUID uuid) {
        return repository.findAllByServerUuid(uuid);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean deleteByIdAndPrincipal(UUID uuid, Principal principal) {
        Url url = getById(uuid);
        if (!Objects.equals(url.getUrl().getHost(), principal.getName())) {
            throw new AccessForbiddenException();
        }
        return repository.deleteByUuid(uuid) > 0;
    }

    /** {@inheritDoc} */
    @Override
    public long getUrlVisitsCount(UUID uuid) {
        return recordRepository.countByUrlUuid(uuid);
    }
}
