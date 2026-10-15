package com.contentforge.backend.model;

import java.util.List;

public record ResearchResult(

        String summary,

        List<String> keyFacts,

        List<String> importantConcepts,

        List<String> sources

) {
}

// ResearchResult object
// │
// ├── summary
// │   └── "Virtual threads are lightweight..."
// │
// ├── keyFacts
// │   ├── "Managed by the JVM"
// │   └── "Useful for concurrency"
// │
// ├── importantConcepts
// │   ├── "Project Loom"
// │   └── "Blocking I/O"
// │
// └── sources
//     └── []

// ResearchResult.java
// Your code is:
// package com.contentforge.backend.model;

// import java.util.List;

// public record ResearchResult(

//         String summary,

//         List<String> keyFacts,

//         List<String> importantConcepts,

//         List<String> sources

// ) {
// }

// Why did we create this?
// Gemini could simply return:
// Java Virtual Threads are lightweight threads...
// They were introduced...
// They are useful because...

// That's just one giant String.
// But later our Writer Agent needs to use the research.
// It is much easier if Java receives organized information:
// ResearchResult
// │
// ├── summary
// ├── keyFacts
// ├── importantConcepts
// └── sources

// That's the entire reason for this file.
// What is record?
// You've already worked with normal Java classes.
// Suppose we wrote this:
// public class ResearchResult {

//     private String summary;
//     private List<String> keyFacts;
//     private List<String> importantConcepts;
//     private List<String> sources;

//     // constructor
//     // getters
//     // equals()
//     // hashCode()
//     // toString()
// }

// That's a lot of boilerplate just for something whose main purpose is holding data.
// Java records give us a shorter way:
// public record ResearchResult(
//         String summary,
//         List<String> keyFacts,
//         List<String> importantConcepts,
//         List<String> sources
// ) {
// }

// Think:
// ResearchResult is a container for the research produced by our AI.

// For example:
// ResearchResult result = new ResearchResult(
//         "Virtual threads are lightweight threads.",
//         List.of(
//                 "They are managed by the JVM",
//                 "They are useful for concurrency"
//         ),
//         List.of(
//                 "Project Loom",
//                 "Carrier Threads"
//         ),
//         List.of()
// );

// Now:
// result.summary()

// returns:
// Virtual threads are lightweight threads.

// And:
// result.keyFacts()

// returns the list:
// [
//     "They are managed by the JVM",
//     "They are useful for concurrency"
// ]

// So ResearchResult.java does not call AI.
// It doesn't research anything.
// It's simply the shape/container of our research data.