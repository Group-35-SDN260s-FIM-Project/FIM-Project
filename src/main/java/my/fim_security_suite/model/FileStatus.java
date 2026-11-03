package my.fim_security_suite.model;

/** The four things a file can be after a check. */
public enum FileStatus {
    UNCHANGED,
    MODIFIED,
    NEW,
    MISSING
}
