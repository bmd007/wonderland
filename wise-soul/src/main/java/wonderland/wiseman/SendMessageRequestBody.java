package wonderland.wiseman;


import org.springframework.lang.Nullable;

import java.util.Map;

public record SendMessageRequestBody(String sender, String receiver, String content, @Nullable Map<String, String> headers) {
}
