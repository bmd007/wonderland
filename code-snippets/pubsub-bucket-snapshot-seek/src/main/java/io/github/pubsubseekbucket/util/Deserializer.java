package io.github.pubsubseekbucket.util;

import java.io.IOException;

public interface Deserializer<IN, OUT> {

    OUT read(IN serialized) throws IOException;
}
