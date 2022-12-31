package wonderland.game.engine;


public record SendMessageRequestBody(String sender, String receiver, String content, String type) {
}
