package com.Web.Pharmagest.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LotDateUpdateRequest {

    private LocalDate datePeremption;

    private LocalDate dateFabrication;
}
