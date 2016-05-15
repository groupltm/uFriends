package instance;

public class ChatMessage {
    public boolean left;
    public String message;
    public boolean isImage;
 
    public ChatMessage(boolean left, String message, boolean isImage) {
        super();
        this.left = left;
        this.message = message;
        this.isImage = isImage;
    }
}
