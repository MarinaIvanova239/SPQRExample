package org.example.spqr.sql.dao;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.domain.Request;

import java.util.List;


public interface InteractionDao {

    void addRequests(List<Request> requests);

    List<Request> getRequestsById(String requestId);

    void addResponse(Request request, Response response);

    List<Response> getResponses(Entity entity);
}
