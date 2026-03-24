package org.example.spqr.sql.dao;

import org.example.spqr.models.domain.Delivery;

import java.util.List;

public interface DeliveryDao {

    void addDeliveries(List<Delivery> deliveries);
}
