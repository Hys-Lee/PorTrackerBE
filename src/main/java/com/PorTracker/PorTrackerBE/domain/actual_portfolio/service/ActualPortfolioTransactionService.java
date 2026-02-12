package com.PorTracker.PorTrackerBE.domain.actual_portfolio.service;

import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto.ActualPortfolioTransactionRequest;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.repository.SupabaseRepository;
import com.PorTracker.PorTrackerBE.service.sqlite.SqliteDatabaseManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActualPortfolioTransactionService {
        private final SqliteDatabaseManager sqliteDatabaseManager;
        private final SupabaseRepository supabaseRepository;

        @Transactional
        public void insertTransaction(String userId, ActualPortfolioTransactionRequest request) {
                Map<String, Object> profile = supabaseRepository.getUserProfile(userId);



                String userBaseCurrency = (String) profile.get("base_currency"); // 예: 'KRW'
                try {



                        JdbcTemplate jdbcTemplate = sqliteDatabaseManager
                                        .getJdbcTemplateOfDataSource(userId, userBaseCurrency);


                        // internal id 찾기
                        String sqlForAsset = String.format("SELECT %s FROM %s WHERE %s = ?",
                                        SqliteSchema.COL_ID, SqliteSchema.TABLE_ASSET,
                                        SqliteSchema.COL_PUBLIC_ID);
                        Long internalAssetId = jdbcTemplate.queryForObject(sqlForAsset, Long.class,
                                        request.getAssetPublicId());

                        String sqlForCurrency = String.format("SELECT %s FROM %s WHERE %s = ?",
                                        SqliteSchema.COL_ID, SqliteSchema.TABLE_CURRENCY_TYPE,
                                        SqliteSchema.COL_PUBLIC_ID);
                        Long internalCurrencyId = jdbcTemplate.queryForObject(sqlForCurrency,
                                        Long.class, request.getCurrencyPublicId());



                        ActualPortfolioRecord record = ActualPortfolioRecord.builder()
                                        .publicId(UUID.randomUUID().toString())
                                        .date(request.getDate().toString())
                                        .transactionType(request.getTransactionType())
                                        .currencyId(internalCurrencyId).assetId(internalAssetId)
                                        .priceBp(request.getPriceBp())
                                        .amountBp(request.getAmountBp())
                                        .exchangeRateBp(request.getExchangeRateBp()).build();

                        String sql = String.format(
                                        " INSERT INTO %s (%s, %s, %s, %s,%s,%s,%s,%s) VALUES (?,?,?,?,?,?,?,?)",
                                        SqliteSchema.TABLE_ACTUAL_PORTFOLIO,
                                        SqliteSchema.COL_PUBLIC_ID, SqliteSchema.COL_DATE,
                                        SqliteSchema.COL_TRANSACTION_TYPE,
                                        SqliteSchema.COL_CURRENCY_ID, SqliteSchema.COL_ASSET_ID,
                                        SqliteSchema.COL_PRICE_BP, SqliteSchema.COL_AMOUNT_BP,
                                        SqliteSchema.COL_EXCHANGE_RATE_BP

                        );

                        // jdbcTemplate.update(sql, request.getDate(), request.getTransactionType(),
                        // request.getCurrencyId(), request.getPriceBp(), request.getAmountBp(),
                        // request.getExchangeRateBp());
                        jdbcTemplate.update(sql, record);



                        log.info("transaction recored successfully for user:{}", userId);


                } catch (

                Exception e) {
                        log.error("Failed to record transaction for user:{} ", userId, e);
                        throw new BusinessException(ErrorCode.DATA_SAVE_FAILED);
                }
        }

}
