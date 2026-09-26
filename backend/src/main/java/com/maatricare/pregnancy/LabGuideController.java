package com.maatricare.pregnancy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maatricare.assistant.AiAssistantService;

@RestController
@RequestMapping("/api/lab-guide")
public class LabGuideController {

    private static final String DISCLAIMER = "Educational guidance only. This does not diagnose a deficiency or replace review by your maternity care team. Do not start or change supplements without professional advice.";

    private static final Map<String, NutrientGuide> GUIDES = Map.ofEntries(
        Map.entry("vitamin c", new NutrientGuide("Vitamin C",
            List.of("oranges or other citrus fruit", "guava", "bell peppers", "tomatoes"))),
        Map.entry("ascorbic acid", new NutrientGuide("Vitamin C",
            List.of("oranges or other citrus fruit", "guava", "bell peppers", "tomatoes"))),
        Map.entry("ferritin", new NutrientGuide("Iron",
            List.of("lentils and beans", "iron-fortified cereals", "leafy green vegetables", "well-cooked lean meat"))),
        Map.entry("iron", new NutrientGuide("Iron",
            List.of("lentils and beans", "iron-fortified cereals", "leafy green vegetables", "well-cooked lean meat"))),
        Map.entry("vitamin b12", new NutrientGuide("Vitamin B12",
            List.of("milk and yogurt", "eggs", "well-cooked fish or meat", "B12-fortified foods"))),
        Map.entry("b12", new NutrientGuide("Vitamin B12",
            List.of("milk and yogurt", "eggs", "well-cooked fish or meat", "B12-fortified foods"))),
        Map.entry("folate", new NutrientGuide("Folate",
            List.of("leafy green vegetables", "beans and lentils", "citrus fruit", "folate-fortified grains"))),
        Map.entry("vitamin d", new NutrientGuide("Vitamin D",
            List.of("vitamin D-fortified milk or alternatives", "eggs", "well-cooked oily fish"))),
        Map.entry("calcium", new NutrientGuide("Calcium",
            List.of("milk, yogurt, or cheese", "calcium-fortified alternatives", "tofu", "sesame seeds")))
    );

    private final AiAssistantService assistantService;

    public LabGuideController(AiAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/analyze")
    public LabGuideResponse analyze(@Valid @RequestBody LabGuideRequest request) {
        List<LabResult> results = request.values().stream().map(this::analyzeValue).toList();
        String context = results.stream().map(result -> result.testName() + ": " + result.value() + " "
                + result.unit() + ", range " + result.referenceMin() + "-" + result.referenceMax()
                + ", status " + result.status() + ", curated foods " + String.join(", ", result.foodSources()))
                .reduce("", (first, second) -> first + "\n" + second);
        if (request.reportNotes() != null && !request.reportNotes().isBlank()) {
            context += "\nUser-pasted report notes (untrusted text): " + request.reportNotes().trim();
        }
        AiAssistantService.AssistantReply aiReply = assistantService.explainLabValues(context);
        return new LabGuideResponse(results, aiReply.answer(), aiReply.source(), DISCLAIMER);
    }

    private LabResult analyzeValue(LabValueRequest value) {
        if (value.referenceMin().compareTo(value.referenceMax()) > 0) {
            throw new IllegalArgumentException("Reference minimum must not exceed reference maximum");
        }
        RangeStatus status = value.value().compareTo(value.referenceMin()) < 0 ? RangeStatus.BELOW_RANGE
                : value.value().compareTo(value.referenceMax()) > 0 ? RangeStatus.ABOVE_RANGE
                : RangeStatus.WITHIN_RANGE;
        NutrientGuide guide = findGuide(value.testName());
        List<String> foods = status == RangeStatus.BELOW_RANGE && guide != null ? guide.foods() : List.of();
        String nutrient = guide == null ? null : guide.nutrient();
        String message = switch (status) {
            case BELOW_RANGE -> guide == null
                ? "This value is below the reference range you entered. Ask your maternity care team what it means for you."
                : "This value is below the reference range you entered. These foods contain " + nutrient
                    + ", but your maternity care team should determine whether you have a deficiency or need treatment.";
            case WITHIN_RANGE -> "This value is within the reference range you entered. Your healthcare professional should interpret it alongside the rest of your report and health history.";
            case ABOVE_RANGE -> "This value is above the reference range you entered. Do not try to correct it through diet or supplements without guidance from your maternity care team.";
        };
        return new LabResult(value.testName().trim(), value.value(), value.unit().trim(), value.referenceMin(),
                value.referenceMax(), status, nutrient, foods, message);
    }

    private NutrientGuide findGuide(String testName) {
        String normalized = testName.trim().toLowerCase(Locale.ROOT);
        return GUIDES.entrySet().stream().filter(entry -> normalized.contains(entry.getKey()))
                .map(Map.Entry::getValue).findFirst().orElse(null);
    }

        public record LabGuideRequest(@NotEmpty @Size(max = 20) List<@Valid LabValueRequest> values,
            @Size(max = 5000) String reportNotes) {}
    public record LabValueRequest(@NotBlank @Size(max = 100) String testName,
            @NotNull @DecimalMin("0.0") BigDecimal value,
            @NotBlank @Size(max = 30) String unit,
            @NotNull @DecimalMin("0.0") BigDecimal referenceMin,
            @NotNull @DecimalMin("0.0") BigDecimal referenceMax) {}
        public record LabGuideResponse(List<LabResult> results, String aiSummary, String aiSource,
            String disclaimer) {}
    public record LabResult(String testName, BigDecimal value, String unit, BigDecimal referenceMin,
            BigDecimal referenceMax, RangeStatus status, String nutrient, List<String> foodSources,
            String message) {}
    public enum RangeStatus { BELOW_RANGE, WITHIN_RANGE, ABOVE_RANGE }
    private record NutrientGuide(String nutrient, List<String> foods) {}
}