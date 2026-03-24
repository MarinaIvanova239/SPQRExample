package org.example.spqr.models.rq;

import lombok.Data;

@Data
public class ResponseRq {
    String requestId;
    String body;
}
