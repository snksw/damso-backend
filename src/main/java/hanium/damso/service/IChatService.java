package hanium.damso.service;

import hanium.damso.dto.ChatDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IChatService {
    String transcribe(MultipartFile file) throws Exception;
    ChatDTO createChat(String name) throws Exception;
    ChatDTO.ChatMessageDTO requestNextMessage(ChatDTO pDTO) throws Exception;
}
