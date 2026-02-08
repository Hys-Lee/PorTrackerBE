package com.PorTracker.PorTrackerBE.repository;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.PorTracker.PorTrackerBE.constant.SheetSchema;
import com.PorTracker.PorTrackerBE.dto.TransactionDto;

@Repository
public class SqliteRepository {
    public List<TransactionDto> findAllTransactions(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "SELECT date, category, amount, memo FROM transactions ORDER BY date DESC";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> new TransactionDto(rs.getString(SheetSchema.DATE.getHeaderName()),
                        rs.getString(SheetSchema.CATEGORY.getHeaderName()),
                        // rs.getString(SheetSchema.ITEM.getHeaderName()),
                        rs.getLong(SheetSchema.AMOUNT.getHeaderName()),
                        rs.getString(SheetSchema.MEMO.getHeaderName())));
    }
}
