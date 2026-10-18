package com.maatricare.assistant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.tracking.AppointmentRepository;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/assistant")
public class AiAssistantController {

    private final AiAssistantService assistantService;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AiAssistantController(AiAssistantService assistantService, AppointmentRepository appointmentRepository,
            UserRepository userRepository) {
        this.assistantService = assistantService;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public AiAssistantService.AssistantReply reply(Authentication authentication,
            @Valid @RequestBody AssistantRequest request) {
        return assistantService.reply(request.question());
    }

        @PostMapping("/appointments/{appointmentId}/question-suggestions")
        public AiAssistantService.AppointmentQuestionSuggestions suggestAppointmentQuestions(
            Authentication authentication, @PathVariable java.util.UUID appointmentId) {
        var user = userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        var appointment = appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return assistantService.suggestAppointmentQuestions(appointment.getTitle());
        }

        @PostMapping("/appointments/{appointmentId}/post-visit-review")
        public AiAssistantService.PostVisitReview reviewVisit(Authentication authentication,
            @PathVariable java.util.UUID appointmentId, @Valid @RequestBody PostVisitRequest request) {
        var user = userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        appointmentRepository.findByIdAndUserId(appointmentId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return assistantService.reviewVisit(request.notes(), request.useAi());
        }

        public record PostVisitRequest(@NotBlank @Size(max = 5000) String notes, boolean useAi) {}
        public record AssistantRequest(@NotBlank @Size(max = 600) String question) {}
}