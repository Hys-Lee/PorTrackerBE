package com.PorTracker.PorTrackerBE.global.constant;

public class ValidationConstants {
    public static final String UUID_REGEXP =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    /*
       pattern 설명:
       yyyy-MM-dd : 날짜
       ['T'HH:mm:ss] : 시간 (선택사항)
       [.SSS] : 밀리초 (선택사항)
       [XXX] : 타임존 오프셋 (예: +09:00, 선택사항)
    */
    public static final String FLEXIBLE_DATETIME_PATTERN = "yyyy-MM-dd['T'HH:mm:ss[.SSS][XXX]]";
}
