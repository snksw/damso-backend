package hanium.damso.service.impl;

import hanium.damso.dto.ChatDTO;
import hanium.damso.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements IChatService {
    @Value("${damso.stt.url}")
    private String STT_API_URL;
    @Value("${damso.llm.url}")
    private String LLM_API_URL;
    @Value("${damso.llm.model}")
    private String LLM_MODEL;

    private final RestClient restClient;

    @Override
    public String transcribe(MultipartFile file) throws Exception {
        log.info("Calling transcribe");
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", file.getResource());
        parts.add("temperature", "0.0");
        parts.add("response_format", "text");
        parts.add("task", "transcribe");
        parts.add("language", "auto");
        return restClient.post().uri(STT_API_URL).body(parts).retrieve().body(String.class);
    }

    @Override
    public ChatDTO createChat() {
        log.info("Calling createChat");
        return new ChatDTO(LLM_MODEL, new ArrayList<>());
    }

    @Override
    public ChatDTO.ChatMessageDTO requestNextMessage(ChatDTO pDTO) {
        log.info("Calling requestNextMessage");
        ChatDTO.ChatResultDTO result = restClient.post().uri(LLM_API_URL).contentType(MediaType.APPLICATION_JSON).body(pDTO).retrieve().body(ChatDTO.ChatResultDTO.class);
        if (result == null) return new ChatDTO.ChatMessageDTO(ChatDTO.Role.assistant, "");
        pDTO.messages().add(result.message());
        return result.message();
    }
}
