package org.example.lineupmaker_be.domain.lineup;

import java.util.List;

public record ScenarioJson(String id, String label, List<StepJson> steps) {
}
