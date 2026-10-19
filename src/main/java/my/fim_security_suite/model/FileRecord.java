/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package my.fim_security_suite.model;

import java.io.Serializable;

public class FileRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String path;
    private final String hash;
    private final long size;
    private final long lastModified;

    public FileRecord(String path, String hash, long size, long lastModified) {
        this.path = path;
        this.hash = hash;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getPath() {
        return path;
    }

    public String getHash() {
        return hash;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
        String hashPreview = (hash == null) ? "unknown"
                : hash.substring(0, Math.min(8, hash.length()));

        if (hash != null && hash.length() > 8) {
            hashPreview += "...";
        }

        return "File: " + path
                + " | Hash: " + hashPreview
                + " | Size: " + size + " bytes";
    }
}
