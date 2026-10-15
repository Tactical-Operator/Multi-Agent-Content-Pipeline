package com.contentforge.backend.model;

import java.util.List;

public record EditorResult(

        String status,

        List<String> issues,

        String feedback

) {
}

// Here's what each field means.
// String status

// The Editor will return either:
// APPROVED

// or:
// REVISION_REQUIRED

// For example, if everything is good:
// {
//     "status": "APPROVED",
//     "issues": [],
//     "feedback": "The content satisfies the requested format and tone."
// }

// But suppose the user requested:
// Platform: Instagram
// Format: Carousel
// Maximum: 8 slides
// Tone: Educational

// and the Writer produced 12 slides. The Editor could return:
// {
//     "status": "REVISION_REQUIRED",
//     "issues": [
//         "The carousel contains 12 slides but the user requested a maximum of 8."
//     ],
//     "feedback": "Reduce the carousel to 8 slides while preserving the key information."
// }

// That feedback becomes very important later because we'll send it back to the Writer:
// Researcher
//     ↓
// Writer ───────────────┐
//     ↓                 │
// Fact Checker          │
//     ↓                 │
// Editor                │
//     ↓                 │
// REVISION_REQUIRED     │
//     ↓                 │
// Editor feedback ──────┘

// Then the Writer can produce version 2.
// If the Editor says:
// APPROVED

// we stop:
// Editor
//    ↓
// APPROVED
//    ↓
// Final Content