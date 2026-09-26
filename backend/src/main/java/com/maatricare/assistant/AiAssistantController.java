package com.maatricare.assistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
public class AiAssistantController {

    private final AiAssistantService assistantService;

    public AiAssistantController(AiAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping
    public AiAssistantService.AssistantReply reply(Authentication authentication,
            @Valid @RequestBody AssistantRequest request) {
        return assistantService.reply(request.question());
    }

    public record AssistantRequest(@NotBlank @Size(max = 600) String question) {}
}