package com.PorTracker.PorTrackerBE.domain.currency.service;

import com.PorTracker.PorTrackerBE.domain.currency.dto.CurrencyTypeRequest;
import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.PorTracker.PorTrackerBE.global.common.UserContextHolder;
import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;
import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final SqliteDatabaseManager sqliteManager;

    // 기존 메서드 유지
    // public List<CurrencyTypeRecord> getAllCurrencies(String userId) {
    public List<CurrencyTypeRecord> getAllCurrencies() {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "SELECT %s, %s, %s FROM %s",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CODE,
                        SqliteSchema.TABLE_CURRENCY_TYPE);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new CurrencyTypeRecord(
                                rs.getLong(SqliteSchema.COL_ID),
                                rs.getString(SqliteSchema.COL_PUBLIC_ID),
                                rs.getString(SqliteSchema.COL_CODE)));
    }

    // NPE 버그 수정된 메서드
    // public CurrencyTypeRecord getCurrencyByPublicId(String userId, String publicId) {
    public CurrencyTypeRecord getCurrencyByPublicId(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "SELECT %s, %s, %s FROM %s WHERE %s = ?",
                        SqliteSchema.COL_ID,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CODE,
                        SqliteSchema.TABLE_CURRENCY_TYPE,
                        SqliteSchema.COL_PUBLIC_ID);

        try {
            // queryForObject 대신 query + 람다 세터를 사용하여 NPE 방지
            List<CurrencyTypeRecord> results =
                    jdbcTemplate.query(
                            sql,
                            ps -> {
                                ps.setString(1, publicId);
                            },
                            (rs, rowNum) ->
                                    new CurrencyTypeRecord(
                                            rs.getLong(SqliteSchema.COL_ID),
                                            rs.getString(SqliteSchema.COL_PUBLIC_ID),
                                            rs.getString(SqliteSchema.COL_CODE)));

            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            log.error("Currency 조회 중 에러 발생: {}", e.getMessage());
            return null;
        }
    }

    // public void addCurrency(String userId, String code) {
    // public void addCurrency(String userId, CurrencyTypeRequest request) {
    public void addCurrency(CurrencyTypeRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "INSERT OR IGNORE INTO %s (%s, %s) VALUES (?, ?)",
                        SqliteSchema.TABLE_CURRENCY_TYPE,
                        SqliteSchema.COL_PUBLIC_ID,
                        SqliteSchema.COL_CODE);

        String publicId = UUID.randomUUID().toString();

        String code = request.getCode();

        // update 시에도 람다 세터를 사용하여 파라미터 타입을 명시적으로 지정
        jdbcTemplate.update(
                sql,
                ps -> {
                    ps.setString(1, publicId);
                    ps.setString(2, code.toUpperCase()); // 통화 코드는 대문자로 저장
                });

        log.info(
                "Currency added successfully for user: {}, code: {}, publicId: {}",
                userId,
                code,
                publicId);
    }

    // public void updateCurrency(String userId, String publicId, CurrencyTypeRequest request) {
    public void updateCurrency(String publicId, CurrencyTypeRequest request) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        String sql =
                String.format(
                        "UPDATE %s SET %s = ? WHERE %s = ?",
                        SqliteSchema.TABLE_CURRENCY_TYPE,
                        SqliteSchema.COL_CODE,
                        SqliteSchema.COL_PUBLIC_ID);

        int updated =
                jdbcTemplate.update(
                        sql,
                        ps -> {
                            ps.setString(1, request.getCode().toUpperCase());
                            ps.setString(2, publicId);
                        });

        if (updated == 0) {
            throw new BusinessException(ErrorCode.NO_DATA, "currencies");
        }

        log.info("Currency updated successfully for user: {}, publicId: {}", userId, publicId);
    }

    // public void deleteCurrency(String userId, String publicId) {
    public void deleteCurrency(String publicId) {
        String userId = UserContextHolder.getUserId();
        JdbcTemplate jdbcTemplate = sqliteManager.getJdbcTemplate(userId);

        // Check exists
        // getCurrencyByPublicId(userId, publicId); // This logic might need refinement if get
        getCurrencyByPublicId(publicId); // This logic might need refinement if get
        // returns null instead of throwing
        // Actually getCurrencyByPublicId returns null if not found.
        // Let's optimize:

        String sql =
                String.format(
                        "DELETE FROM %s WHERE %s = ?",
                        SqliteSchema.TABLE_CURRENCY_TYPE, SqliteSchema.COL_PUBLIC_ID);

        int deleted = jdbcTemplate.update(sql, ps -> ps.setString(1, publicId));

        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NO_DATA, "currencies");
        }

        log.info("Currency deleted successfully for user: {}, publicId: {}", userId, publicId);
    }
}
