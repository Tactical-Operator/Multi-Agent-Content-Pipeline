package com.contentforge.backend.model;

import java.util.List;

public record FactCheckResult(

        boolean factuallyCorrect,
        List<String> issues,
        List<String> verifiedClaims,
        List<String> recommendations) {

}
