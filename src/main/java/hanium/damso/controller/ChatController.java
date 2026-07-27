package hanium.damso.controller;

import hanium.damso.dto.ChatDTO;
import hanium.damso.dto.ResultDTO;
import hanium.damso.service.IChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/chat")
public class ChatController {
    private final IChatService chatService;
    private final Map<String, ChatDTO> chatDTOMap = new HashMap<>(); // Should be migrated to DB

    @PostMapping(value = "transcribe")
    public ResultDTO<String> transcribe(@RequestParam(value = "file") MultipartFile multipartFile) throws Exception {
        log.info("Calling transcribe");
        String result = chatService.transcribe(multipartFile);
        log.info(result);
        return ResultDTO.success("STT_RESULT", result);
    }

    // Temporal method for grabbing a /chat session
    // Should be replaced with Auth and Passive sessions instead in later
    private ChatDTO getChat(String sessionId) throws Exception {
        ChatDTO pDTO = chatDTOMap.get(sessionId);
        if (pDTO == null) {
            pDTO = chatService.createChat();
            chatDTOMap.put(sessionId, pDTO);
        }
        return pDTO;
    }

    @PostMapping(value = "chat")
    public ResultDTO<ChatDTO.ChatMessageDTO> chat(HttpServletRequest request, HttpSession session) throws Exception {
        log.info("Calling chat");
        ChatDTO pDTO = getChat(session.getId());
        pDTO.messages().add(new ChatDTO.ChatMessageDTO(ChatDTO.Role.user, request.getParameter("content")));
        return ResultDTO.success("LLM_RESPONSE", chatService.requestNextMessage(pDTO));
    }
}
