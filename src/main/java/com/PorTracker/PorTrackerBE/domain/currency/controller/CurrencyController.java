package com.PorTracker.PorTrackerBE.domain.currency.controller;

import com.PorTracker.PorTrackerBE.domain.currency.dto.CurrencyTypeRequest;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.domain.currency.service.CurrencyService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
public class CurrencyController {
    private final CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<CurrencyTypeRecord>> getCurrencies(
            // @RequestHeader("X-USER-ID") String userId) {
            ) {

        // return ResponseEntity.ok(currencyService.getAllCurrencies(userId));
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @PostMapping
    public ResponseEntity<Void> addCurrency(
            // @RequestHeader("X-USER-ID") String userId, @RequestBody String code) {
            // @RequestHeader("X-USER-ID") String userId, @RequestBody CurrencyTypeRequest request)
            // {
            @Valid @RequestBody CurrencyTypeRequest request) {
        // currencyService.addCurrency(userId, code);
        // currencyService.addCurrency(userId, request);
        currencyService.addCurrency(request);

        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.PutMapping("/{publicId}")
    // public ResponseEntity<Void> updateCurrency(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<Void> updateCurrency(
            @PathVariable("publicId") String publicId,
            @Valid @RequestBody CurrencyTypeRequest request) {
        // currencyService.updateCurrency(userId, publicId, request);
        currencyService.updateCurrency(publicId, request);
        return ResponseEntity.ok().build();
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{publicId}")
    // public ResponseEntity<Void> deleteCurrency(@RequestHeader("X-USER-ID") String userId,
    public ResponseEntity<Void> deleteCurrency(@PathVariable("publicId") String publicId) {
        // currencyService.deleteCurrency(userId, publicId);
        currencyService.deleteCurrency(publicId);
        return ResponseEntity.ok().build();
    }
}
