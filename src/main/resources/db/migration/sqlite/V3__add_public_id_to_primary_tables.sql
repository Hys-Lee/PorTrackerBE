create table currency_type_v2( 
	id integer primary key autoincrement,
    public_id text unique not null, -- 추가됨.
	code text(3) -- locale 3글자
	-- 나머지 데이터는 뭐 나중에 추가하든가
);

-- 기존 데이터 복붙
insert into currency_type_v2 (id, public_id, code)
select id, cast(id as text),code from currency_type;

-- 새로운 public_id 컬럼에 데이터 채우기
UPDATE currency_type_v2 SET public_id = 'bd960953-45cd-434c-8350-7dba939e53f1' WHERE code = 'USD';

-- 기존 테이블 삭제 및 교체
drop table currency_type;
alter table currency_type_v2 rename to currency_type;