package my.fim_security_suite.model;

/**
 * One result from an integrity check: which file, what happened to it,
 * and its hash before and after.
 * For NEW files oldHash is null and for MISSING files newHash is null.
 *
 * Implements Comparable so ScanResult can sort the changes by path.
 *
 * @author wahin
 */
public class FileChange implements Comparable<FileChange> {
    private String path;
    private FileStatus status;
    private String oldHash;
    private String newHash;

    // order of the parameters follows the UML: path, status, oldHash, newHash
    public FileChange(String path, FileStatus status, String oldHash, String newHash){
        this.path = path;
        this.status = status;
        this.oldHash = oldHash;
        this.newHash = newHash;
    }

    public String getPath() {
        return path;
    }

    public FileStatus getStatus() {
        return status;
    }

    public String getOldHash() {
        return oldHash;
    }

    public String getNewHash() {
        return newHash;
    }

    @Override
    public int compareTo(FileChange other){
        return this.path.compareTo(other.path);
    }

    @Override
    public String toString(){
        return status + " " + path;
    }
}
