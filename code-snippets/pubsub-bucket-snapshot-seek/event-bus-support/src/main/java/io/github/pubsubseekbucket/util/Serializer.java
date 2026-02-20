package io.github.pubsubseekbucket.util;

import java.io.IOException;

public interface Serializer<IN, OUT> {

    OUT write(IN value) throws IOException;
}
