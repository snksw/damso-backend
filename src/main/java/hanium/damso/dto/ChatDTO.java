package hanium.damso.dto;

import java.util.List;

public record ChatDTO(String model, List<ChatMessageDTO> messages, boolean stream) {
    public ChatDTO(String model, List<ChatMessageDTO> messages) {
        this(model, messages, false);
    }
    public enum Role {
        user,
        assistant
    }
    public record ChatMessageDTO(Role role, String content) {}
    public record ChatResultDTO(ChatMessageDTO message) {}
}
