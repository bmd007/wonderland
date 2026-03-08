package io.github.pubsubseekbucket.publish.statedump;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import lombok.RequiredArgsConstructor;

import java.io.BufferedWriter;
import java.io.IOException;

@RequiredArgsConstructor
class BlobWriterWrapper implements AutoCloseable {

    private final BlobInfo blobInfo;
    private final BufferedWriter bufferedWriter;
    private final WriteChannel writeChannel;

    public BlobInfo getBlobInfo() {
        return blobInfo;
    }

    public BufferedWriter getBufferedWriter() {
        return bufferedWriter;
    }

    @Override
    public void close() throws IOException {
        bufferedWriter.close();
        writeChannel.close();
    }
}
