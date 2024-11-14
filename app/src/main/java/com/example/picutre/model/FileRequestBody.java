package com.example.picutre.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class FileRequestBody extends RequestBody {

    private File file;
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    public FileRequestBody(File file) {
        this.file = file;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse("application/zip");
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), DEFAULT_BUFFER_SIZE)) != -1) {
                total += read;
                sink.flush(); // Ensure data is flushed to the network
            }
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }
}
