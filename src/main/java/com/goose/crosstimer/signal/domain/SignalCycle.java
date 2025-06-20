package com.goose.crosstimer.signal.domain;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@ToString(exclude = "crossroad")
@Table(name = "signal_cycle", uniqueConstraints = @UniqueConstraint(columnNames = {"itst_id", "direction"}))
public class SignalCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "itst_id", referencedColumnName = "itstId")
    private Crossroad crossroad;

    @Column(nullable = false, length = 4)
    private String direction;

    @Column(nullable = false)
    private Long referenceGreenStart;

    @Column(nullable = false)
    private Integer greenSeconds;

    @Column(nullable = false)
    private Integer redSeconds;

    @Column(nullable = false)
    private Instant updatedAt;
}
