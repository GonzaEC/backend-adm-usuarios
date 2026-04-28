// src/main/java/com/plataforma/dto/InvestmentRequest.java
package com.plataforma.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body del request POST /api/projects/{projectId}/investments
 *
 * Solo lleva tokensToBuy porque:
 * - El projectId viene en el path variable
 * - El inversor viene del JWT (@AuthenticationPrincipal)
 * El cliente no puede falsificar ninguno de los dos.
 */
@Getter
@Setter
@NoArgsConstructor
public class InvestmentRequest
{
    private Long tokensToBuy;
}