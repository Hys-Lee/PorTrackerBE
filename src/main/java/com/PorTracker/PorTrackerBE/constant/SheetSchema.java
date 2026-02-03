package com.PorTracker.PorTrackerBE.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SheetSchema {
    DATE("date", true),
    CATEGORY("category", true),
    ITEM("item", true),
    AMOUNT("amount", true),
    MEMO("memo", false);

    private final String headerName;
    private final boolean required;
}
