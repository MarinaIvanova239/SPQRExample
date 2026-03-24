package org.example.spqr.rest.service;

import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.Response;
import org.example.spqr.exceptions.EntityNotFoundException;
import org.example.spqr.rest.mappers.Domain2ResponseMapper;
import org.example.spqr.rest.mappers.Request2DomainMapper;
import org.example.spqr.models.rq.ResponseRq;
import org.example.spqr.models.rs.ResponseRs;
import org.example.spqr.sql.dao.ActivationDao;
import org.example.spqr.sql.dao.InteractionDao;
import org.example.spqr.sql.dao.EntityDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;

@Component("interactionService")
public class InteractionService {

    private final EntityDao entityDao;
    private final InteractionDao interactionDao;
    private final TransactionOperations transactionOperations;
    private final ActivationDao activationDao;

    private final Request2DomainMapper request2DomainMapper;
    private final Domain2ResponseMapper domain2ResponseMapper;

    public InteractionService(
            EntityDao entityDao,
            InteractionDao interactionDao,
            TransactionOperations transactionOperations,
            ActivationDao activationDao) {
        this.entityDao = entityDao;
        this.activationDao = activationDao;
        this.interactionDao = interactionDao;
        this.transactionOperations = transactionOperations;
        this.request2DomainMapper = new Request2DomainMapper();
        this.domain2ResponseMapper = new Domain2ResponseMapper();
    }

    public List<ResponseRs> getResponses(String entityId) {
        if (!entityDao.exists(entityId)) {
            throw new EntityNotFoundException();
        }

        Entity entity = entityDao.getById(entityId);
        List<Response> responses = interactionDao.getResponses(entity);
        return domain2ResponseMapper.from(responses);
    }

    public List<ResponseRs> addResponses(List<ResponseRq> responseRqs) {
        List<ResponseRs> addedResponses = new ArrayList<>();
        responseRqs.forEach(callbackRq -> {
            ResponseRs callbackRs = addResponse(callbackRq);
            if (callbackRs != null) {
                addedResponses.add(callbackRs);
            }
        });
        return addedResponses;
    }

    public ResponseRs addResponse(ResponseRq responseRq) {
        Response response = request2DomainMapper.from(responseRq);

        Request request = interactionDao.getRequestsById(response.requestId())
                    .stream()
                    .max(comparing(Request::createdAt))
                    .orElse(null);
        if (request == null) {
            return null;
        }

        var result = transactionOperations.execute(status -> {
            Entity entity = activationDao.getByRequestInAnyState(request);
            interactionDao.addResponse(request, response);
            return response.toBuilder()
                    .entityId(entity.entityId())
                    .entityCreatedAt(entity.status().createdAt())
                    .build();
        });
        return domain2ResponseMapper.from(result);
    }
}
