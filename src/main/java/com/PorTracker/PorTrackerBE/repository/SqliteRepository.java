package com.PorTracker.PorTrackerBE.repository;

import com.PorTracker.PorTrackerBE.constant.SheetSchema;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SqliteRepository {
    public List<TransactionDto> findAllTransactions(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "SELECT date, category, amount, memo FROM transactions ORDER BY date DESC";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new TransactionDto(
                                rs.getString(SheetSchema.DATE.getHeaderName()),
                                rs.getString(SheetSchema.CATEGORY.getHeaderName()),
                                rs.getString(SheetSchema.ITEM.getHeaderName()),
                                rs.getLong(SheetSchema.AMOUNT.getHeaderName()),
                                rs.getString(SheetSchema.MEMO.getHeaderName())));
    }

    public void insertTmpTransaction(DataSource dataSource, TransactionDto dto) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        // 이게 된다고? db선에서 막힐 것 같은데
        String sql =
                String.format(
                        " INSERT INTO transactions (date, category, item, amount, memo) VALUES (?, ?, ?, ?, ?)");

        jdbc.update(sql, dto.date(), dto.category(), dto.item(), dto.amount(), dto.memo());
    }
}
