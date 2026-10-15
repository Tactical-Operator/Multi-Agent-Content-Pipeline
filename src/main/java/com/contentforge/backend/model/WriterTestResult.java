package com.contentforge.backend.model;

public record WriterTestResult(

        ResearchResult research,
        WriterResult writtenContent,
        FactCheckResult factCheck,
        EditorResult editorReview

) {

}


// This simply holds the outputs of all three stages:
// WriterTestResult
// │
// ├── ResearchResult research
// ├── WriterResult writtenContent
// ├── FactCheckResult factCheck
// └── EditorResult editorReview

// Notice something interesting. A record field doesn't have to be String, int, etc. It can itself be another object:
// ResearchResult research
// WriterResult writtenContent
// FactCheckResult factCheck