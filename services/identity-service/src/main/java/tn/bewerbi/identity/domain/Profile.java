package tn.bewerbi.identity.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true) private UUID userId;
    @Column(name = "first_name") private String firstName;
    @Column(name = "last_name") private String lastName;
    private String phone;
    private String city;
    private String country;
    @Column(length = 2000) private String bio;
    @Column(name = "photo_url") private String photoUrl;
    @Column(name = "desired_profession") private String desiredProfession;
    @Enumerated(EnumType.STRING) @Column(name = "german_level") private GermanLevel germanLevel;
    @Enumerated(EnumType.STRING) @Column(name = "recognition_status") private RecognitionStatus recognitionStatus;
    @Column(name = "onboarding_completed", nullable = false) private boolean onboardingCompleted;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    protected Profile() {}
    public Profile(UUID userId) { this.userId = userId; }

    public UUID getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { this.phone = v; }
    public String getCity() { return city; }
    public void setCity(String v) { this.city = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public String getBio() { return bio; }
    public void setBio(String v) { this.bio = v; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String v) { this.photoUrl = v; }
    public String getDesiredProfession() { return desiredProfession; }
    public void setDesiredProfession(String v) { this.desiredProfession = v; }
    public GermanLevel getGermanLevel() { return germanLevel; }
    public void setGermanLevel(GermanLevel v) { this.germanLevel = v; }
    public RecognitionStatus getRecognitionStatus() { return recognitionStatus; }
    public void setRecognitionStatus(RecognitionStatus v) { this.recognitionStatus = v; }
    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean v) { this.onboardingCompleted = v; }
    public List<String> getSkills() { return skills; }
}
