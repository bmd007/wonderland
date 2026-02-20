package io.github.pubsubseekbucket.publish.statedump;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.CopyWriter;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
class StateDumpHandler {
    private final Storage storage;

    public StateDumpHandler(Storage storage) {
        this.storage = storage;
    }

    public BlobInfo createStateDumpBlob(String name, String contentType, String bucketName) {
        BlobId blobId = BlobId.of(bucketName, name);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        log.debug("Created blobInfo={}", blobInfo);
        return blobInfo;
    }

    public BlobWriterWrapper createStateDumpWriter(BlobInfo stateDumpTarget) throws IOException {
        WriteChannel blobWriter = storage.writer(stateDumpTarget);
        BufferedWriter bw = new BufferedWriter(Channels.newWriter(blobWriter, UTF_8));
        return new BlobWriterWrapper(stateDumpTarget, bw, blobWriter);
    }

    public BlobInfo renameDump(BlobInfo tmpBlobInfo, String name, String bucketName) {
        BlobId originalBlobId = tmpBlobInfo.getBlobId();
        BlobId targetBlobId = BlobId.of(bucketName, name);
        BlobInfo targetBlobInfo = BlobInfo.newBuilder(targetBlobId)
                .setContentType(tmpBlobInfo.getContentType())
                .build();

        // Rename is implemented as copy followed by delete
        CopyWriter copyWriter = storage.copy(Storage.CopyRequest.of(originalBlobId, targetBlobInfo));
        copyWriter.getResult();

        try {
            storage.delete(originalBlobId);
        } catch (StorageException e) {
            log.warn("Failed deleting blob {}, most likely because retention policy is configured for the bucket.", originalBlobId.getName(), e);
        }

        return targetBlobInfo;
    }

    public boolean deleteDump(BlobInfo blobInfo) {
        BlobId originalBlobId = blobInfo.getBlobId();

        try {
            return storage.delete(originalBlobId);
        } catch (StorageException e) {
            log.warn("Failed deleting blob {}, most likely because retention policy is configured for the bucket.", originalBlobId.getName(), e);
            return false;
        }
    }
}
