package dev.michaelgoldman.catalog.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "products")
class ProductEntity implements Persistable<String> {

    @Id
    String sku;
    String name;
    String description;
    String brand;
    String category;

    @JdbcTypeCode(SqlTypes.JSON)
    Map<String, Object> attributes;

    // amount and time are both filled in, or both empty (no price yet)
    BigDecimal priceAmount;
    Instant priceUpdatedAt;

    // tells Spring whether to INSERT or UPDATE (the key is the SKU, so it can't tell by itself)
    @Transient
    private boolean isNew = true;

    protected ProductEntity() {
    }

    ProductEntity(String sku) {
        this.sku = sku;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }

    @Override
    public String getId() {
        return sku;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
