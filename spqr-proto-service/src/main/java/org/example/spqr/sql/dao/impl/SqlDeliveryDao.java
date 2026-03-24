package org.example.spqr.sql.dao.impl;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.example.spqr.models.domain.Delivery;
import org.example.spqr.sql.dao.DeliveryDao;
import org.example.spqr.sql.mappers.Domain2DataMapper;
import org.example.spqr.sql.mappers.SqlExceptionMapper;
import org.example.spqr.sql.sqlmappers.DeliverySqlMapper;
import org.springframework.stereotype.Component;

import java.util.List;

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
