package org.mediawiki.api.de.mastacode.http;

public interface ProgressListener {
    void onProgress(long transferred, long total);
}
