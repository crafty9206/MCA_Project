package com.maatricare.pregnancy;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api")
public class PregnancyMilestoneController {

    private final UserRepository userRepository;
    private final PregnancyMilestoneRepository milestoneRepository;

    public PregnancyMilestoneController(UserRepository userRepository, PregnancyMilestoneRepository milestoneRepository) {
        this.userRepository = userRepository;
        this.milestoneRepository = milestoneRepository;
    }

    @GetMapping("/pregnancy-milestones")
    public List<MilestoneResponse> milestones(Authentication authentication) {
        User user = currentUser(authentication);
        return milestoneRepository.findByUserIdOrderByWeekNumberAsc(user.getId()).stream()
                .map(this::response).toList();
    }

    @PostMapping("/pregnancy-milestones")
    @ResponseStatus(HttpStatus.CREATED)
    public MilestoneResponse createMilestone(Authentication authentication, @Valid @RequestBody MilestoneRequest request) {
        User user = currentUser(authentication);
        PregnancyMilestone milestone = milestoneRepository.save(
                new PregnancyMilestone(user, request.title().trim(), request.description() == null ? null : request.description().trim(), request.weekNumber(), request.type()));
        return response(milestone);
    }

    @PatchMapping("/pregnancy-milestones/{id}/complete")
    public MilestoneResponse completeMilestone(Authentication authentication, @PathVariable UUID id,
            @RequestBody CompletionRequest request) {
        PregnancyMilestone milestone = milestoneRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Milestone not found"));
        milestone.setCompleted(request.completed());
        return response(milestoneRepository.save(milestone));
    }

    @DeleteMapping("/pregnancy-milestones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMilestone(Authentication authentication, @PathVariable UUID id) {
        PregnancyMilestone milestone = milestoneRepository.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Milestone not found"));
        milestoneRepository.delete(milestone);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found"));
    }

    private MilestoneResponse response(PregnancyMilestone milestone) {
        return new MilestoneResponse(milestone.getId(), milestone.getTitle(), milestone.getDescription(), milestone.getWeekNumber(),
                milestone.getType(), milestone.isCompleted());
    }

    public record MilestoneRequest(@NotBlank @Size(max = 120) String title,
            @Size(max = 500) String description, int weekNumber, String type) {}
    public record CompletionRequest(boolean completed) {}
    public record MilestoneResponse(UUID id, String title, String description, int weekNumber, String type, boolean completed) {}
}
