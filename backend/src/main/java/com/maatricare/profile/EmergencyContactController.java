package com.maatricare.profile;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.maatricare.user.User;
import com.maatricare.user.UserRepository;

@RestController
@RequestMapping("/api/emergency-contacts")
public class EmergencyContactController {
    private final EmergencyContactRepository contacts;
    private final UserRepository users;

    public EmergencyContactController(EmergencyContactRepository contacts, UserRepository users) {
        this.contacts = contacts;
        this.users = users;
    }

    @GetMapping
    public List<ContactResponse> list(Authentication authentication) {
        return contacts.findByUserIdOrderByNameAsc(currentUser(authentication).getId()).stream()
                .map(this::response).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactResponse create(Authentication authentication, @Valid @RequestBody ContactRequest request) {
        return response(contacts.save(new EmergencyContact(currentUser(authentication), request.name(),
                request.phone(), request.type())));
    }

    @PatchMapping("/{id}")
    public ContactResponse update(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody ContactRequest request) {
        EmergencyContact contact = ownedContact(authentication, id);
        contact.update(request.name(), request.phone(), request.type());
        return response(contacts.save(contact));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable UUID id) {
        contacts.delete(ownedContact(authentication, id));
    }

    private EmergencyContact ownedContact(Authentication authentication, UUID id) {
        return contacts.findByIdAndUserId(id, currentUser(authentication).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private User currentUser(Authentication authentication) {
        return users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private ContactResponse response(EmergencyContact contact) {
        return new ContactResponse(contact.getId(), contact.getName(), contact.getPhone(), contact.getType());
    }

    public record ContactRequest(@NotBlank @Size(max = 100) String name,
            @NotBlank @Pattern(regexp = "\\+?[0-9]{7,15}") String phone,
            @NotNull EmergencyContact.ContactType type) {}
    public record ContactResponse(UUID id, String name, String phone, EmergencyContact.ContactType type) {}
}