// package com.PorTracker.PorTrackerBE.global.config;

// import java.lang.reflect.Method;

// import org.springframework.core.convert.converter.Converter;
// import org.springframework.core.convert.converter.ConverterFactory;

// @SuppressWarnings({"rawtypes","unchecked"})
// public class GenericEnumConverterFactory implements ConverterFactory<String, Enum<?>>{
//     @Override
//     public <T extends Enum<?>> Converter<String,T> getConverter(Class<T> targetType){
//         return new StringToEnumConverter<>(targetType);
//     }

//     private static class StringToEnumConverter<T extends Enum<?>> implements Converter<String,T>
// {
//         private final Class<T> enumType;

//         public StringToEnumConverter(Class<T> enumType){
//             this.enumType = enumType;
//         }

//         @Override
//         public T convert(String source){
//             if(source == null || source.isBlank()){
//                 return null;
//             }

//             // try{
//             //     Method method = enumType.getMethod("from", String.class);

//             //     return (T) method.invoke(null,source.trim());
//             // }catch(NoSuchMethodException e){
//             //     try{
//             //         return (T) Enum.valueOf((Class<Enum>)enumType,
// source.trim().toUpperCase());
//             //     }catch(IllegalAccessException ex){
//             //         return null;
//             //     }
//             // }
//             try {
//                 // 1. 우리가 만든 'from(String)' 메서드가 있는지 확인
//                 Method method = enumType.getMethod("from", String.class);
//                 // 2. 존재하면 from 메서드를 호출하여 변환 (예: "event" -> MemoType.EVENT)
//                 return (T) method.invoke(null, source.trim());

//             } catch (NoSuchMethodException e) {
//                 // 3. from 메서드가 아예 없는 다른 일반 Enum인 경우 (스프링 기본 방식 사용)
//                 try {
//                     return (T) Enum.valueOf(enumType, source.trim().toUpperCase());
//                 } catch (IllegalArgumentException ex) {
//                     throw new IllegalArgumentException("유효하지 않은 Enum 값입니다: " + source);
//                 }
//             } catch (Exception e) {
//                 // 4. from 메서드는 찾았지만 변환에 실패한 경우 (우리가 던진 IllegalArgumentException 등)
//                 throw new IllegalArgumentException("유효하지 않은 Enum 값입니다: " + source);
//             }
//         }

//     }
// }
