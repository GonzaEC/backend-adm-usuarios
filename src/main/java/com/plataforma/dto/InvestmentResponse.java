// src/main/java/com/plataforma/dto/InvestmentResponse.java
package com.plataforma.dto;

import com.plataforma.model.Investment;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa el comprobante de inversión que se devuelve al cliente.
 *
 * No devolvemos la entidad Investment directamente por dos razones:
 * 1. Evitar exponer relaciones lazy que pueden causar LazyInitializationException
 *    al serializar fuera de la transacción.
 * 2. Controlar exactamente qué campos ve el cliente (el tokenPrice interno
 *    no necesariamente debe exponerse tal cual).
 */
@Getter
public class InvestmentResponse
{
    private final Long   investmentId;
    private final Long   projectId;
    private final String projectName;
    private final Long   tokensPurchased;
    private final BigDecimal amountPaid;
    private final BigDecimal tokenPrice;
    private final LocalDateTime createdAt;

    /**
     * Factory method: construye el DTO desde la entidad dentro de la transacción,
     * donde las relaciones lazy todavía están disponibles.
     */
    public static InvestmentResponse from(Investment investment)
    {
        return new InvestmentResponse(investment);
    }

    private InvestmentResponse(Investment investment)
    {
        this.investmentId    = investment.getId();
        this.projectId       = investment.getProject().getId();
        this.projectName     = investment.getProject().getName();
        this.tokensPurchased = investment.getTokensPurchased();
        this.amountPaid      = investment.getAmountPaid();
        this.tokenPrice      = investment.getTokenPrice();
        this.createdAt       = investment.getCreatedAt();
    }
}