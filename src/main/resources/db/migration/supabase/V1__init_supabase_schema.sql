-- 마스터 테이블
create table currency_type( 
	id integer primary key autoincrement,
	code text(3), -- locale 3글자
	-- 나머지 데이터는 뭐 나중에 추가하든가
);

-- 일반
create table public.profile(
	id uuid unique primary key not null on delete cascade,
	email text unique not null,
	role text not null default 'USER', -- admin, user 등등
	nickname text not null, -- 닉네임은 겹쳐도 뭐.. 상관 없지 않나?
	base_currency_id integer not null, 
	created_at timestamptz default now(),
	updated_at timestamptz default now(),
	-- deleted_at은 프로파일에선 필요 없을 듯. 걍 삭제 시키면 될 듯.
	
	foreign key (id) references auth.user(id), 
	foreign key (base_currency_id) references currency_type(id),
	
	
);

create table public.credential(
	id uuid unique primary key not null on delete cascade,
	provider text not null,
	refresh_token text unique not null,
	encrypted_at timestamptz,
	
	foreign key (id) references auth.user(id), 
);

create table public.group_statistic(
	id bigserial primary key , -- 더 큰수 가능+자동증가
	stat_type text not null, --enum느낌으로 서버에서 종류  내려주기
	period text not null, -- 애도 기간에 대해 일정한 형식.. 2024-02 등
	sample_count integer not null,
    sum_amount)bp bigint default 0, -- view 테스트 위해 임시로 일단 넣음.
	last_updated_at timestamptz default now(),
	
	unique(stat_type, period), -- 이 데이터는 유니크
	
);

create table public.stat_contribution( -- 중복 기여 방지
	contribution_key text unique not null, // userId+period+stat_type 의 해시
	updated_at timestamptz
);

-- View
create or replace view public.v_group_averages as 
select 
    id, stat_type, period, sample_count, (sum_amount_bp - nullif(sample_count,0)) as avg_amount_bp,last_updated_at
from public.group_statistic;

-- Trigger
create or replace function public.handle_new_user()
returns trigger as $$
begin
    insert into public.profile (id, email, nickname)
    values (new.id, new.email, new.raw_user_meta_data ->> 'full_name');
    return new;
end;
$$ language plpgsql security definer;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute procedure public.handle_new_user();