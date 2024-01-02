package ru.job4j.urlshortcut.dto;

import ru.job4j.urlshortcut.util.Password;

/**
 * Data Transfer Object to transfer information for {@code Server} password change operation.
 * @param oldPassword old password to change
 * @param newPassword new password to set
 */
public record ServerPasswordChangeDto(
        @Password
        String oldPassword,
        @Password
        String newPassword) { }
