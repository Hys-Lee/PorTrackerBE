package com.PorTracker.PorTrackerBE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetRequest {
    private String name;
    private String description;
    // can be numeric id or public_id/code string from client
    private String currencyId;
    // can be numeric id or public_id string from client
    private String typeId;
}
