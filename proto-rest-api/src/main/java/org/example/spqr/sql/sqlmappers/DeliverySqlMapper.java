package org.example.spqr.sql.sqlmappers;

import org.example.spqr.models.dm.DeliveryDm;
import org.springframework.stereotype.Component;


@Component("deliverySqlMapper")
public interface DeliverySqlMapper extends FlushableSqlMapper {

    void insert(DeliveryDm deliveryDm);
}
