-- 마스터 테이블
create table currency_type( 
	id integer primary key autoincrement,
	code text(3) -- locale 3글자
	-- 나머지 데이터는 뭐 나중에 추가하든가
);

-- 일반

create table asset_type(
	id integer primary key autoincrement,
	public_id text unique not null,
	created_at text default (datetime('now')),
	updated_at text default (datetime('now')),
	deleted_at text default null,
	name text (8) -- 적당히..
);

create table asset(
	id integer primary key autoincrement,
	public_id text unique not null,
	created_at text default (datetime('now')),
	updated_at text default (datetime('now')),
	deleted_at  text default null,
	name text (15) not null, -- sqlite에선 잘 안먹히지만 일반적으론 db에서도 길이제한 같이 해주는게 좋다고 함.
	description text(30), -- 역시 일단 길이 지정
	currency_id integer not null,
	type_id integer not null,
	
	foreign key (currency_id) references currency_type(id),
	foreign key (type_id) references asset_type(id)
);

create table actual_portfolio(
	id integer primary key autoincrement,
	public_id text unique not null, -- uuid,
	asset_id text not null,

	date text not null,
	created_at text default (datetime('now')),
	updated_at text default (datetime('now')),
	deleted_at text default null,
	transaction_type text not null,
	change_ratio_bp integer, -- %는 소수점 2까지
	accumulated_ratio_bp integer, -- %는 소수점 2까지
	currency_id integer,
	price_bp integer, -- 일반은 소수점 4까지
	amount_bp integer, -- 일반은 소수점 4까지, 소수점 거래 대비
	exchange_rate_bp integer, -- 일반은 소수점 4까지


	foreign key (asset_id) references asset(id),
	foreign key (currency_id) references currency_type(id)
);

create table target_portfolio(
	id integer primary key autoincrement,
	public_id text unique not null,
	name text(20) not null, -- 적당히
	date text not null, -- iso string
	created_at text default (datetime('now')),
	deleted_at text default null
);

create table target_portfolio_snapshot(
	id  integer primary key autoincrement,
	portfolio_id integer not null,
	created_at text default (datetime('now')),
	
	foreign key (portfolio_id) references target_portfolio(id)
);

create table target_portfolio_item(
	asset_id integer not null,
	snapshot_id integer not null,
	current_ratio_bp integer not null,
	ratio_delta_bp integer not null,
	
	foreign key (asset_id) references asset(id),
	foreign key (snapshot_id) references target_portfolio_snapshot(id),
	
	primary key (asset_id, snapshot_id)
);

create table memo(
	id integer primary key autoincrement,
	public_id text unique not null,
	created_at text default (datetime('now')),
	updated_at text default (datetime('now')),
	deleted_at text default null,
	importance text not null,
	title text (20) not null, -- 적당히
	content text(500) , -- 적당히?
	evaluation text, -- null 가능하게
	date text not null, -- iso string
	memo_type text not null, -- actual, target, event
	actual_id integer , -- null 가능
	target_id integer ,-- null 가능
	
	foreign key (actual_id) references actual_portfolio(id),
	foreign key (target_id) references target_portfolio(id)
	
);


create table tag(
	id integer primary key autoincrement,
	content text(15) not null -- 적당히..
);

create table memo_tag(
	memo_id integer not  null,
	tag_id integer not null,
	create_at text default (datetime('now')), 
	updated_at text default (datetime('now')), -- 넣어야 하나?
	deleted_at text default null, -- 넣어야 하나?
	
	foreign key (memo_id) references memo(id),
	foreign key (tag_id) references tag(id),
	
	primary key (memo_id, tag_id)
);

-- 인덱스
CREATE INDEX idx_actual_date ON actual_portfolio(date);
CREATE INDEX idx_actual_asset_id ON actual_portfolio(asset_id);
CREATE INDEX idx_memo_actual_id ON memo(actual_id) WHERE actual_id IS NOT NULL;
CREATE INDEX idx_memo_target_id ON memo(target_id) WHERE target_id IS NOT NULL;

-- 테이블에 데이터 기본 삽입
insert or ignore into currency_type(code) values ('USD');