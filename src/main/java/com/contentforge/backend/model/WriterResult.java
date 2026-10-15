package com.contentforge.backend.model;

import java.util.List;

public record WriterResult(

    String title,
    List<String> contentParts,
    String caption
) {
    
}
