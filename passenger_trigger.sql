drop trigger if exists dd_user_account_as_passenger on user_account cascade;

create or replace function add_new_passenger()
    returns trigger language plpgsql as $$
    begin
        if new.available_seats = 0 then
			insert into passenger (id) values (new.id);
        end if;
	    return new;
    end; $$;

create trigger add_user_account_as_passenger
    after update on user_account
    for each row
    execute function add_new_passenger();