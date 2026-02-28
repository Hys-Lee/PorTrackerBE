package com.PorTracker.PorTrackerBE.global.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.management.RuntimeErrorException;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.stereotype.Service;

import com.PorTracker.PorTrackerBE.global.error.BusinessException;
import com.PorTracker.PorTrackerBE.global.error.ErrorCode;
import com.PorTracker.PorTrackerBE.global.infra.sqlite.SqliteDatabaseManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInitializationService {
    private final SqliteDatabaseManager sqliteManager;

    public void initializeUserDatabase(String userId){
        ensureDatabaseDirectoryExists();
         // 뭐 try안에 있어야 하는거아닌가? 사용측에 있어야 하나?
        runFlywayMigration(userId);
    }

    // private void ensureDatabaseFileExists(String userId){
    //     String dbPath = "db/"+userId+".db";
    //     File dbFile = new File(dbPath);

    //     if(!dbFile.exists()){
    //         try{
    //             Files.createDirectories(Paths.get("db") );
    //             if(dbFile.createNewFile()){
    //                 log.info("[Init] Created new SQLite file for User:{} ",userId);

    //             }
    //         }catch(IOException e){
    //             log.error("[Init] Failed to cretaed db file for user:{}",userId, e);
    //             throw new BusinessException(ErrorCode.DATA_CREATE_FAILED);
    //         }
    //     }
    // }

    private void ensureDatabaseDirectoryExists(){
        Path dbFolderPath = Paths.get("db").toAbsolutePath();
        try{
            if(!Files.exists(dbFolderPath)){
                Files.createDirectories(dbFolderPath);
                log.info("[Init] Created database directory: {}",dbFolderPath);
            }
        }catch (IOException e){
            log.error("[Init] Failed to create db directory",e);
            throw new BusinessException(ErrorCode.DATA_CREATE_FAILED);
        }
    }

    private void runFlywayMigration(String userId){
        DataSource dataSource = sqliteManager.getDataSource(userId);

        // Sqlite flyway 설정
        Flyway flyway = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration/sqlite").baselineOnMigrate(true).load();

        flyway.migrate();
        
        log.info("[Init] Flyway migration successful for user:{} ",userId);
    }
}
