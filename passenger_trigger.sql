drop trigger if exists dd_user_account_as_passenger on user_account cascade;

create or replace function add_new_passenger()
    returns trigger language plpgsql as $$
    begin
	insert into passenger (id) values (new.id);
	return null;
    end; $$;

create trigger add_user_account_as_passenger
    after update on user_account
    for each row
    execute function add_new_passenger();
