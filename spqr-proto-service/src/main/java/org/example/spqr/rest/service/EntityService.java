package org.example.spqr.rest.service;

import org.example.spqr.models.domain.Delivery;
import org.example.spqr.models.domain.Entity;
import org.example.spqr.models.domain.EntityChangeSet;
import org.example.spqr.models.domain.EntityState;
import org.example.spqr.models.domain.EntityStatus;
import org.example.spqr.models.domain.Page;
import org.example.spqr.models.domain.Request;
import org.example.spqr.models.domain.Scenario;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.models.domain.Subscription;
import org.example.spqr.exceptions.EntityNotFoundException;
import org.example.spqr.exceptions.EntityContentionException;
import org.example.spqr.sql.retriever.PreconditionsRetriever;
import org.example.spqr.rest.factories.SubscriptionFactory;
import org.example.spqr.rest.mappers.Domain2ResponseMapper;
import org.example.spqr.rest.mappers.Request2DomainMapper;
import org.example.spqr.models.rq.EntityRq;
import org.example.spqr.models.rs.EntityRs;
import org.example.spqr.models.rs.PageRs;
import org.example.spqr.sql.dao.ActivationDao;
import org.example.spqr.sql.dao.InteractionDao;
import org.example.spqr.sql.dao.EntityDao;
import org.example.spqr.sql.dao.SubscriptionDao;
import org.example.spqr.sql.dao.DeliveryDao;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionOperations;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.example.spqr.utils.DateUtils.toDateTime;
import static org.example.spqr.utils.DateUtils.toZonedDateTime;

@Component("entityService")
public class EntityService {
    private int idempotencyPeriod = 30; // days

    private final EntityDao entityDao;
    private final ScenarioService scenarioService;
    private final SubscriptionDao subscriptionDao;
    private final TransactionOperations transactionOperations;
    private final PreconditionsRetriever preconditionsRetriever;
    private final ActivationDao activationDao;
    private final DeliveryDao deliveryDao;
    private final InteractionDao interactionDao;

    private final Request2DomainMapper request2DomainMapper;
    private final Domain2ResponseMapper domain2ResponseMapper;

    public EntityService(
            EntityDao entityDao,
            ScenarioService scenarioService,
            TransactionOperations transactionOperations,
            SubscriptionDao subscriptionDao,
            ActivationDao activationDao,
            DeliveryDao deliveryDao,
            InteractionDao interactionDao
    ) {
        this.entityDao = entityDao;
        this.scenarioService = scenarioService;
        this.transactionOperations = transactionOperations;
        this.subscriptionDao = subscriptionDao;
        this.activationDao = activationDao;
        this.deliveryDao = deliveryDao;
        this.interactionDao = interactionDao;
        this.preconditionsRetriever = new PreconditionsRetriever(entityDao);
        this.request2DomainMapper = new Request2DomainMapper();
        this.domain2ResponseMapper = new Domain2ResponseMapper();
    }

    public EntityRs addEntity(EntityRq request) {
        Entity existedEntity = entityDao.getById(request.getEntityId(), idempotencyPeriod);
        if (nonNull(existedEntity)) {
            return domain2ResponseMapper.from(existedEntity);
        }

        Entity registered = transactionOperations.execute(
                s -> {
                    Entity entity = request2DomainMapper.from(request);
                    List<Subscription> subscriptions = SubscriptionFactory.create(entity, preconditionsRetriever::getPrecondition);
                    int subscriptionCount =subscriptionDao.addSubscriptions(subscriptions);
                    Scenario scenario = scenarioService.register(request.getScenarioName());
                    Entity modifiedEntity = entity.toBuilder()
                            .scenarioSpec(entity.scenarioSpec().toBuilder()
                                    .id(scenario.id())
                                    .build())
                            .preconditions(entity.preconditions().toBuilder()
                                    .subscriptionsCount(subscriptionCount)
                                    .build())
                            .build();
                    return entityDao.add(modifiedEntity);
                }
        );

        return domain2ResponseMapper.from(registered);
    }

    public List<EntityRs> addEntities(List<EntityRq> requests) {
        List<EntityRs> addedEntities = new ArrayList<>();
        for (EntityRq request: requests) {
            try {
                EntityRs addedEntity = addEntity(request);
                addedEntities.add(addedEntity);
            } catch (Exception e) {
                // skip exceptions
            }
        }
        return addedEntities;
    }

    public EntityRs getEntity(String entityId) {
        Entity entity = entityDao.getById(entityId);
        if (entity == null) {
            throw new EntityNotFoundException();
        }
        return domain2ResponseMapper.from(entity);
    }

    public PageRs<EntityRs> getEntities(int limit, int offset) {
        List<Entity> entities = entityDao.getEntities(limit, offset);
        Page<Entity> page = new Page<>(entities, limit, offset);
        return domain2ResponseMapper.fromEntities(page);
    }

    public List<EntityRs> suspendEntities(List<String> entityIds) {
        List<EntityRs> suspendedEntities = new ArrayList<>();
        for (String entityId: entityIds) {
            Entity entity = entityDao.getById(entityId);
            if (entity == null) {
                throw new EntityNotFoundException();
            }

            DateTime originalUpdateTime = toDateTime(entity.status().updatedAt());

            ZonedDateTime now = ZonedDateTime.now();
            var status = EntityStatus.builder()
                    .createdAt(entity.status().createdAt())
                    .updatedAt(now)
                    .state(EntityState.SUSPENDED)
                    .build();

            EntityChangeSet changeset = new EntityChangeSet()
                    .setStatus(status)
                    .setActivatedAt(now.plusMinutes(5));

            Entity updatedEntity = transactionOperations.execute(s -> {
                boolean updated = entityDao.update(entity, changeset, originalUpdateTime);
                if (!updated) {
                    throw new EntityContentionException(entity.entityId());
                }

                Request request = Request.builder()
                        .requestId(UUID.randomUUID().toString())
                        .entityId(entity.entityId())
                        .entityCreatedAt(entity.status().createdAt())
                        .createdAt(changeset.getStatus().get().updatedAt())
                        .build();
                interactionDao.addRequests(singletonList(request));

                return entity.toBuilder()
                        .status(changeset.getStatus().get())
                        .build();
            });
            suspendedEntities.add(domain2ResponseMapper.from(updatedEntity));
        }
        return suspendedEntities;
    }

    public List<EntityRs> activateConditional(String conditionalId) {
        Entity preconditional = entityDao.getById(conditionalId);

        List<Subscription> subscriptions = subscriptionDao.getSubscriptions(preconditional.entityId(),
                preconditional.status().createdAt());

        if (subscriptions.isEmpty()) {
            return emptyList();
        }

        List<SubscriberInfo> subscriberInfos = subscriptions.stream()
                .map(Subscription::subscriberInfo)
                .collect(toList());
        List<Entity> existentSubscribers = activationDao.getSubscribed(subscriberInfos);

        Set<SubscriberInfo> existentSubscriberInfos = existentSubscribers.stream()
                .map(entity -> SubscriberInfo.builder()
                        .entityId(entity.entityId())
                        .entityCreatedAt(entity.status().createdAt())
                        .build())
                .collect(toSet());

        ZonedDateTime deliveryTime = ZonedDateTime.now();
        List<Delivery> deliveries = subscriptions.stream()
                .filter(subscription -> isSubscriberExists(subscription, existentSubscriberInfos))
                .map(subscription -> Delivery.create(subscription, deliveryTime))
                .collect(toList());

        if (deliveries.isEmpty()) {
            return emptyList();
        }

        transactionOperations.execute(status -> {
            deliveryDao.addDeliveries(deliveries);
            ZonedDateTime updateTime =  ZonedDateTime.now();
            List<Entity> updatedEntities = existentSubscribers.stream()
                    .map(entity -> {
                        return entity.toBuilder()
                                .status(entity.status().toBuilder()
                                        .updatedAt(updateTime)
                                        .build())
                                .build();
                    })
                    .collect(toList());
            int updatedCount = activationDao.updateActivationTime(updatedEntities);
            if (updatedCount != existentSubscribers.size()) {
                List<String> entityIds = existentSubscribers.stream()
                        .map(Entity::entityId)
                        .collect(toList());
                throw new EntityContentionException(entityIds);
            }
            return status;
        });

        return existentSubscribers.stream()
                .map(domain2ResponseMapper::from)
                .collect(toList());
    }

    public List<EntityRs> activateSuspended(int max) {
        DateTime now = DateTime.now();
        List<Entity> suspendedEntities = activationDao.getSuspendedEntities(max, 14, 0);
        List<Entity> updatedEntities = suspendedEntities.stream()
                .map(entity -> {
                    return entity.toBuilder()
                            .activatedAt(null)
                            .status(entity.status().toBuilder()
                                    .updatedAt(toZonedDateTime(now))
                                    .build())
                            .build();
                })
                .collect(toList());

        activationDao.updateActivationTime(updatedEntities);
        return suspendedEntities.stream()
                .map(domain2ResponseMapper::from)
                .collect(toList());
    }

    private boolean isSubscriberExists(Subscription subscription, Set<SubscriberInfo> existentSubscriberInfos) {
        return existentSubscriberInfos.contains(subscription.subscriberInfo());
    }
}
