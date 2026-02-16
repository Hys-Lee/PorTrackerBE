create table target_portfolio_item_v2(
	asset_id integer not null,
	snapshot_id integer not null,
	current_ratio_bp integer not null,
	-- ratio_delta_bp integer not null,
	
	foreign key (asset_id) references asset(id),
	foreign key (snapshot_id) references target_portfolio_snapshot(id),
	
	primary key (asset_id, snapshot_id)
);

-- 기존 테이블 삭제 및 교체
drop table target_portfolio_item;
alter table target_portfolio_item_v2 rename to target_portfolio_item;