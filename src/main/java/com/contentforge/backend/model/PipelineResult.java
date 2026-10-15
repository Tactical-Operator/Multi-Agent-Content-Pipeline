package com.contentforge.backend.model;

public record PipelineResult(

        ResearchResult research,

        WriterResult writer,

        FactCheckResult factCheck,

        EditorResult editor

) {
}