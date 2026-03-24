package org.example.spqr.sql.dao.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.example.spqr.models.dm.DeliveryDm;
import org.example.spqr.models.dm.EntitySearchSetDm;
import org.example.spqr.models.domain.Delivery;
import org.example.spqr.models.domain.SubscriberInfo;
import org.example.spqr.sql.dao.DeliveryDao;
import org.example.spqr.sql.mappers.Data2DomainMapper;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.DeliverySqlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;

@Component("deliveryDao")
@AllArgsConstructor
public class SqlDeliveryDao implements DeliveryDao {

    private final SqlExceptionMapper sqlExceptionMapper;
    private final Domain2DataMapper domain2DataMapper;
    private DeliverySqlMapper deliverySqlMapper;

    @Override
    public void addDeliveries(@NonNull List<Delivery> deliveries) {
        try {
            deliveries.forEach(delivery -> deliverySqlMapper.insert(domain2DataMapper.from(delivery)));
            deliverySqlMapper.flush();
        } catch (Exception e) {
            throw sqlExceptionMapper.toProtoException(e);
        }

    }
}
