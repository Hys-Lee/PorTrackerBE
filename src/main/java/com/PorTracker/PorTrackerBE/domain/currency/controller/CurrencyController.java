package com.PorTracker.PorTrackerBE.domain.currency.controller;

import com.PorTracker.PorTrackerBE.domain.currency.dto.CurrencyTypeRequest;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyTypeRecord>> getCurrencies(
            @RequestHeader("X-USER-ID") String userId) {

        return ResponseEntity.ok(currencyService.getAllCurrencies(userId));
    }

    @PostMapping
    public ResponseEntity<Void> addCurrency(
            // @RequestHeader("X-USER-ID") String userId, @RequestBody String code) {
            @RequestHeader("X-USER-ID") String userId, @RequestBody CurrencyTypeRequest request) {
        // currencyService.addCurrency(userId, code);
        currencyService.addCurrency(userId, request);

        return ResponseEntity.ok().build();
    }
}
