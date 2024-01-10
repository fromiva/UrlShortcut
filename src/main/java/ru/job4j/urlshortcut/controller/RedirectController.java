package ru.job4j.urlshortcut.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;
import ru.job4j.urlshortcut.model.Url;
import ru.job4j.urlshortcut.service.UrlService;
import ru.job4j.urlshortcut.util.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.UUID;

/** Controller class to handle requests for {@code Url} entities redirection. */
@RequiredArgsConstructor
@RestController
@RequestMapping("redirect")
public class RedirectController {

    private final UrlService service;

    /**
     * Handles request to redirect {@code Url} entity.
     *
     * @param uuid ID of the {@code Url}
     * @return redirect response
     */
    @GetMapping("{uuid}")
    public RedirectView redirectByUuid(@PathVariable String uuid) {
        Url url;
        try {
            url = service.getById(UUID.fromString(uuid));
        } catch (EntityNotFoundException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "URL with ID " + uuid + " not found.");
        }
        if (url.getExpired().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE,
                    "URL with ID " + uuid + " is inactive anymore.");
        }
        return new RedirectView(url.getUrl().toString());
    }
}
