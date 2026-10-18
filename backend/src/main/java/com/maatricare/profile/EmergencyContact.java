package com.maatricare.profile;

import java.util.UUID;
import com.maatricare.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactType type;

    protected EmergencyContact() {}

    public EmergencyContact(User user, String name, String phone, ContactType type) {
        this.user = user;
        update(name, phone, type);
    }

    public void update(String name, String phone, ContactType type) {
        this.name = name.trim();
        this.phone = phone;
        this.type = type;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public ContactType getType() { return type; }

    public enum ContactType { HOSPITAL, DOCTOR, PARTNER, FAMILY, FRIEND, OTHER }
}