package com.PorTracker.PorTrackerBE.domain.profile.repository;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.PorTracker.PorTrackerBE.global.constant.SqliteSchema;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CredentialRepository {
    private final JdbcTemplate jdbcTemplate;
    public void saveGoogleToken(UUID userId, String accessToken){
        String provider = "google";
        String sql=String.format("INSERT INTO public.credential (%s,%s,%s,%s)" +
         " VALUES (?, $s, ?, NOW())"+
         " ON CONFLICT (%s)" + 
         " DO UPDATE SET %s=EXCLUDED.%s, %s = NOW()",
        // insert
        SqliteSchema.COL_ID, SqliteSchema.COL_PROVIDER, SqliteSchema.COL_ACCESS_TOKEN, SqliteSchema.COL_UPDATED_AT,

        // values
        provider,
        // on
        SqliteSchema.COL_ID,
        // do
        SqliteSchema.COL_ACCESS_TOKEN, SqliteSchema.COL_ACCESS_TOKEN, SqliteSchema.COL_UPDATED_AT
        );

        jdbcTemplate.update(sql, userId, accessToken);
    }

    public String getAccessToken(UUID userId){
        String sql = String.format("SELECT %s"+
        " FROM public.credential"+
        " WHERE %s = ?",
        // select
        SqliteSchema.COL_ACCESS_TOKEN,
        // where
        SqliteSchema.COL_ID
    );

        try{
            return jdbcTemplate.queryForObject(sql,String.class, userId);

        }catch(Exception e){
            return null;
        }
    }
}
