CREATE INDEX idx_actual_asset_date ON actual_portfolio(asset_id, date DESC);
CREATE INDEX idx_memo_date ON memo(date DESC) ;
CREATE INDEX idx_target_date ON target_portfolio(date DESC);