package org.example.spqr.sql.dao.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.dm.SubscriptionDm;
import org.example.spqr.models.domain.Subscription;
import org.example.spqr.sql.dao.SubscriptionDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.SubscriptionSqlMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.slf4j.LoggerFactory.getLogger;

@Component("subscriptionDao")
@RequiredArgsConstructor
public class SqlSubscriptionDao implements SubscriptionDao {
    private static final Logger LOG = getLogger(SqlSubscriptionDao.class);

    private final SqlExceptionMapper sqlExceptionMapper;
    private final Domain2DataMapper domain2DataMapper;
    private final SubscriptionSqlMapper subscriptionSqlMapper;
    private final Data2DomainMapper data2DomainMapper;

    @Override
    public int addSubscriptions(@NonNull List<Subscription> subscriptions) {
        if (isEmpty(subscriptions)) {
            return 0;
        }

        try {
            subscriptions.forEach(s -> subscriptionSqlMapper.insert(domain2DataMapper.from(s)));
            subscriptionSqlMapper.flush();
            return subscriptions.size();
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }

    @Override
    public List<Subscription> getSubscriptions(@NonNull String entityId, ZonedDateTime entityCreatedAt) {
        EntitySearchSetDm entitySearchSetDm = EntitySearchSetDm.builder()
                .entityId(entityId)
                .creationTime(entityCreatedAt)
                .build();
        try {
            List<SubscriptionDm> subscriptions = subscriptionSqlMapper.selectBySearchSet(entitySearchSetDm);
            LOG.trace("Selected subscriptions by entityId = '{}' and creationTime = '{}': {}", entityId, entityCreatedAt, subscriptions);
            return data2DomainMapper.from(subscriptions);
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }
    }
}
