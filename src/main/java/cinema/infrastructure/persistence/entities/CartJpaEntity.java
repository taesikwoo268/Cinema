package cinema.infrastructure.persistence.entities;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "cart_records", uniqueConstraints = {@UniqueConstraint(columnNames = {"customer_id"})})
public class CartJpaEntity extends JpaRecord {
    @Column(name = "customer_id", nullable = false, length = 128) public String customerId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private CustomerJpaEntity customer;
    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"cart_id", "screening_id", "seat_id"}))
    @OrderColumn(name = "item_index")
    public List<CartItemJpaValue> items = new ArrayList<>();
}
