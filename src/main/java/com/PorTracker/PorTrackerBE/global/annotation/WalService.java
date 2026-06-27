package com.PorTracker.PorTrackerBE.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Kafka WAL(Write-Ahead Log) 트랜잭션 로깅 대상 서비스 클래스임을 명시하는 클래스 레벨 커스텀 어노테이션입니다. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WalService {}
