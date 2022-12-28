package wonderland.message.publisher;


public record SendMessageRequestBody(String sender, String receiver, String content, String type) {
}
