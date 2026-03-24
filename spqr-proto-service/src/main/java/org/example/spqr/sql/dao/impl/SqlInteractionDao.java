package org.example.spqr.sql.dao.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.spqr.models.dm.ResponseDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.dm.RequestDm;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.Response;
import org.example.spqr.models.domain.Request;
import org.example.spqr.sql.dao.InteractionDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.InteractionSqlMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.joda.time.DateTime.now;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

@Component("interactionDao")
@RequiredArgsConstructor
@Transactional(propagation = REQUIRED)
public class SqlInteractionDao implements InteractionDao {

    private static final Logger LOG = getLogger(SqlInteractionDao.class);

    @NonNull
    private final InteractionSqlMapper interactionSqlMapper;
    @NonNull
    private final Domain2DataMapper domain2DataMapper;
    @NonNull
    private final Data2DomainMapper data2DomainMapper;
    @NonNull
    private final SqlExceptionMapper sqlExceptionMapper;

    @Value("${database.maxStoragePeriod:365}")
    private Integer maxStoragePeriod;

    @Override
    public void addRequests(@NonNull List<Request> requests) {
        if (isEmpty(requests)) {
            return;
        }
        LOG.trace("Inserting requests into a database... Interactions: {}", requests);
        try {
            for (Request request : requests) {
                RequestDm requestDm = domain2DataMapper.from(request);
                interactionSqlMapper.insertRequest(requestDm);
                LOG.trace("Interaction has been inserted into a database: {}", requestDm);
            }
            interactionSqlMapper.flush();
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public void addResponse(@NonNull Request request, @NonNull Response response) {
        LOG.trace("Inserting new response into a database for request: {}, {}", response, request);
        ResponseDm responseDm = domain2DataMapper.from(response, request);
        try {
            interactionSqlMapper.insertResponse(responseDm);
            interactionSqlMapper.flush();
            LOG.trace("Response for request was inserted into a database: {}, {}", responseDm, request);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    @Transactional(propagation = SUPPORTS)
    public List<Request> getRequestsById(@NonNull String requestId) {
        LOG.trace("Getting request for id '{}'", requestId);
        DateTime currentTime = now();

        EntitySearchSetDm searchSetDm = EntitySearchSetDm.builder()
                .startSearchTime(currentTime.withTimeAtStartOfDay().minusDays(1))
                .endSearchTime(currentTime.plusSeconds(600))
                .build();

        List<RequestDm> requestDms;
        try {
            requestDms = interactionSqlMapper.selectRequestsByIdAndSearchSet(requestId, searchSetDm);
            if (isEmpty(requestDms)) {
                requestDms = interactionSqlMapper.selectRequestsByIdAndSearchSet(requestId, searchSetDm);
            }

            LOG.trace("Requests for id '{}' has been selected: {}", requestId, requestDms);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }

        return data2DomainMapper.from(toArray(requestDms, RequestDm.class));
    }

    @Override
    @Transactional(propagation = SUPPORTS)
    public List<Response> getResponses(@NonNull Entity response) {
        LOG.trace("Getting interactions for response: {}", response);
        String entityId = response.entityId();
        ZonedDateTime entityCreatedAt = response.status().createdAt();
        EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .creationTime(entityCreatedAt)
                .build();
        try {
            List<ResponseDm> responseDms = interactionSqlMapper.selectResponsesBySearchSet(entitySearchSetDm);
            LOG.trace("Requests for entityId '{}' and createdAt '{}' has been selected: {}", entityId, entityCreatedAt, responseDms);
            return data2DomainMapper.from(toArray(responseDms, ResponseDm.class));
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }
}
