package com.maatricare.pregnancy;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class EducationResourceController {

    private static final List<EducationResource> RESOURCES = List.of(
        resource("healthy-eating", "Nutrition", "Healthy eating through pregnancy",
            "Balanced meal, hydration, and food-safety basics.",
            "Regular meals can make daily nourishment easier to manage. Individual nutrition needs vary, so discuss supplements and dietary restrictions with your healthcare professional.",
            List.of("Include a variety of vegetables, fruit, protein-rich foods, and grains where practical.",
                    "Keep fluids available throughout the day.",
                    "Ask your care team which foods, supplements, or preparation methods you should avoid.")),
        resource("gentle-movement", "Exercise", "Gentle movement ideas",
            "Questions and habits for planning comfortable activity.",
            "Movement recommendations depend on your pregnancy and health history. Confirm suitable activities and intensity with your healthcare professional.",
            List.of("Choose comfortable movement such as walking or gentle stretching when approved.",
                    "Build up gradually and include rest.",
                    "Stop and seek advice if you experience concerning symptoms.")),
        resource("emotional-wellbeing", "Wellbeing", "Emotional wellbeing routines",
            "Simple ways to notice mood, rest, and support needs.",
            "Emotional changes deserve the same attention as physical changes. Tracking patterns can help you explain what you are experiencing to someone you trust or your care team.",
            List.of("Create a small daily check-in for mood and energy.",
                    "Make time for rest and supportive contact.",
                    "Contact a healthcare professional if distress persists or feels difficult to manage.")),
        resource("postpartum-recovery", "Postpartum", "Preparing for postpartum recovery",
            "Plan practical support, follow-up care, and recovery questions.",
            "Recovery differs for every person. Before birth, discuss expected follow-up, warning signs, feeding support, and practical help with your maternity team.",
            List.of("Identify people who can help with meals, rest, transport, or household tasks.",
                    "Keep your follow-up contact details easy to find.",
                    "Ask which physical or emotional changes require urgent advice.")),
        resource("newborn-preparation", "Newborn", "Newborn preparation checklist",
            "Focus on safe essentials, care contacts, and the first days at home.",
            "Preparation can stay simple: a safe sleep space, basic clothing and feeding supplies, transport planning, and clear care-team contact information.",
            List.of("Discuss safe sleep guidance with your healthcare professional.",
                    "Plan how you will travel home and attend early appointments.",
                    "Write down where to get feeding and newborn-care support.")),
        resource("hospital-bag", "Hospital", "Hospital and birth preparation",
            "Organize documents, practical items, and care preferences.",
            "Your hospital or birth setting may provide its own checklist. Confirm when to arrive, whom to call, and what documents or supplies they recommend.",
            List.of("Keep identification, care records, and important phone numbers together.",
                    "Pack comfortable essentials for you and the baby.",
                    "Review transport and support-person arrangements.")),
        resource("urgent-care", "Emergency", "When to seek urgent help",
            "Keep emergency and maternity contact routes visible.",
            "Do not use MaatriCare to assess an emergency. Contact your maternity unit, local urgent-care service, or emergency services when you have severe or rapidly worsening symptoms or believe immediate help is needed.",
            List.of("Save your maternity unit and local emergency numbers.",
                    "Follow the urgent-care instructions provided by your care team.",
                    "Do not delay emergency help while waiting for an online response."))
    );

    @GetMapping
    public List<EducationResource> resources(@RequestParam(required = false) String category,
            @RequestParam(required = false, name = "q") String query) {
        String normalizedCategory = normalize(category);
        String normalizedQuery = normalize(query);
        return RESOURCES.stream()
            .filter(resource -> normalizedCategory.isEmpty()
                    || resource.category().toLowerCase(Locale.ROOT).equals(normalizedCategory))
            .filter(resource -> normalizedQuery.isEmpty() || searchableText(resource).contains(normalizedQuery))
            .toList();
    }

    private static String searchableText(EducationResource resource) {
        return normalize(resource.title() + " " + resource.description() + " " + resource.content()
                + " " + String.join(" ", resource.keyPoints()));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static EducationResource resource(String id, String category, String title, String description,
            String content, List<String> keyPoints) {
        return new EducationResource(id, category, title, description, content, keyPoints,
                "EDUCATIONAL_REVIEW", "1.0", LocalDate.of(2026, 9, 26));
    }

    public record EducationResource(String id, String category, String title, String description,
            String content, List<String> keyPoints, String reviewStatus, String contentVersion,
            LocalDate reviewedOn) {}
}