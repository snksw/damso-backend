package hanium.damso.service.impl;

import hanium.damso.dto.ChatDTO;
import hanium.damso.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    @Value("${damso.tts.url}")
    private String TTS_API_URL;
    @Value("${damso.sts.url}")
    private String STS_API_URL;
    @Value("${damso.sts.model}")
    private String STS_MODEL;

    private final RestClient restClient;

    private final String PROMPT = "당신의 이름은 도담이입니다. 당신은 친절하고 알기 쉬운 말투를 사용하며, 가능하다면 대화를 지속하도록 시도하며 오늘 일어난 일을 사용자가 계속해서 말하도록 유도하여야 합니다. 당신은 문자로만 답변해야 하며 Markdown이나 표 등은 절대로 생성해서는 안 됩니다. 당신은 반드시 한국어 및 한글, 숫자와 문장 부호만 사용해야 합니다. 사용자의 이름은 {name}입니다.";

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
    public ChatDTO createChat(String name) {
        log.info("Calling createChat: {}", name);

        List<ChatDTO.ChatMessageDTO> messageList = new ArrayList<>();
        messageList.add(new ChatDTO.ChatMessageDTO(ChatDTO.Role.system, PROMPT.replace("{name}", name)));

        return new ChatDTO(LLM_MODEL, messageList);
    }

    @Override
    public ChatDTO.ChatMessageDTO requestNextMessage(ChatDTO pDTO) {
        log.info("Calling requestNextMessage");
        ChatDTO.ChatResultDTO result = restClient.post().uri(LLM_API_URL).contentType(MediaType.APPLICATION_JSON).body(pDTO).retrieve().body(ChatDTO.ChatResultDTO.class);
        if (result == null) return new ChatDTO.ChatMessageDTO(ChatDTO.Role.assistant, "");
        pDTO.messages().add(result.message());
        log.info(pDTO.toString());
        return result.message();
    }

    @Override
    public Resource synthesize(String text) {
        log.info("Calling synthesize: {}", text);

        return restClient.post().uri(TTS_API_URL).contentType(MediaType.APPLICATION_JSON).body(Map.of(
            "text", text,
            "language", "KR",
            "speaker", "KR",
            "speed", 1.0
        )).retrieve().body(Resource.class);
    }

    @Override
    public Resource convert(Resource file, String model) {
        log.info("Calling convert");

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("file", file);
        parts.add("model_name", model);
        parts.add("pitch_shift", 6);

        return restClient.post().uri(STS_API_URL).contentType(MediaType.MULTIPART_FORM_DATA).body(parts).retrieve().body(Resource.class);
    }

    @Override
    public Resource convert(Resource file) {
        return convert(file, STS_MODEL);
    }
}
