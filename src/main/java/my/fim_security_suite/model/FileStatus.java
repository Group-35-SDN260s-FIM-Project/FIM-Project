package my.fim_security_suite.model;

/** The four results a file can get in an integrity check. */
public enum FileStatus {
    UNCHANGED,
    MODIFIED,
    NEW,
    MISSING
}
