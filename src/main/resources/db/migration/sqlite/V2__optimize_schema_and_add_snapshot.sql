-- -- 스냅샷 테이블 (체크포인트) 생성
-- create table if not exists actual_portfolio_snapshot(
--     id integer primary key autoincrment,
--     last_covered_actual_portfolio_id integer not null,-- 스냅샷이 커버하는 마지막 거래 id
--     snapshot_date text not null,
--     total_assets_value_bp integer not null, -- 포폴 전체 자산 가치 합계(bp)
--     created_at  text default (datetime('now')),
    
--     foreign key (last_covered_actual_id) references actual_portfolio(id)
-- );

-- -- 스냅샷 상세 테이블 (자산별 체크포인트) 생성
-- create table if not exists actual_portfolio_snapshot_item(
--     id integer primary key autoincrement;
--     snapshot_id integer not null,
--     asset_id integer not null,
--     asset_value_bp integer not null, -- 자산에 대한 당시 가치

--     foreign key (snapshot_id) references actual_portfolio_snapshot(id)
--     foreign key (asset_id) references asset(id)
-- );

-- actual_portfolio 테이블 수정 (컬럼 정리하기) -> 교체 후 이름 원복할거임.
create table actual_portfolio_v2(
	id integer primary key autoincrement,
	public_id text unique not null, -- uuid,
	asset_id interger not null,

	date text not null,
	created_at text default (datetime('now')),
	updated_at text default (datetime('now')),
	deleted_at text default null,
	transaction_type text not null,
	currency_id integer,
	price_bp integer, -- 일반은 소수점 4까지
	amount_bp integer, -- 일반은 소수점 4까지, 소수점 거래 대비
	exchange_rate_bp integer, -- 일반은 소수점 4까지


	foreign key (asset_id) references asset(id),
	foreign key (currency_id) references currency_type(id)
);

-- 기존 데이터는 없으니, 이전 테이블로부터의 데이터 복사는 생략

-- 기존 테이블 삭제 및 교체
drop table actual_portfolio;
alter table actual_portfolio_v2 rename to actual_portfolio;

-- 인덱스 추가
create index idx_actual_portfolio_date on actual_portfolio(date);