package ru.job4j.urlshortcut.model;

/** Entity lifecycle and access status. */
public enum Status {

    /** When entity saved in a persistent storage. */
    REGISTERED,

    /** When entity verified and approved. */
    VERIFIED,

    /** When entity blocked for access. */
    BLOCKED
}
