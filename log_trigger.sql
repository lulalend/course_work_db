DROP FUNCTION IF EXISTS deactivate_user;
CREATE FUNCTION deactivate_user(id integer) RETURNS void AS $$
    UPDATE user_account SET active_status = ('disabled')
    WHERE user_account.id = :id;
    $$
    LANGUAGE SQL
    ;
--DROP TRIGGER IF EXISTS deactivate_user_on_delete;
-- CREATE TRIGGER deactivate_user_on_delete INSTEAD OF DELETE ON user_account
--     DECLARE id integer;
--     SELECT :id = id FROM DELETED;
--     EXECUTE FUNCTION deactivate_user(:id);

CREATE TABLE IF NOT EXISTS user_account_audit
(
    operation char(1)  NOT NULL,
    stamp    timestamp NOT NULL,
    user_id integer,
    first_name varchar(25) not null,
    last_name varchar(25) not null,
    email varchar(50) not null,

    constraint user_id_fk
    foreign key (user_id) references user_account(id)
);

CREATE OR REPLACE FUNCTION process_user_account_audit() RETURNS TRIGGER AS $user_account_audit$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO user_account_audit (operation, stamp, user_id, first_name, last_name, email)
        SELECT 'D', now(), old_table.id, old_table.first_name, old_table.last_name, old_table.email FROM old_table;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO user_account_audit (operation, stamp, user_id, first_name, last_name, email)
        SELECT 'U', now(), new_table.id, new_table.first_name, new_table.last_name, new_table.email FROM new_table;
    ELSIF (TG_OP = 'INSERT') THEN
        INSERT INTO user_account_audit (operation, stamp, user_id, first_name, last_name, email)
        SELECT 'I', now(), new_table.id, new_table.first_name, new_table.last_name, new_table.email FROM new_table;
    END IF;
    RETURN NULL;
END;
$user_account_audit$ LANGUAGE plpgsql;

DROP TRIGGER if exists user_account_audit_ins on  user_account;
CREATE TRIGGER user_account_audit_ins
    AFTER INSERT ON user_account
    REFERENCING NEW TABLE AS new_table
    FOR EACH STATEMENT EXECUTE FUNCTION process_user_account_audit();
DROP TRIGGER if exists user_account_audit_upd on  user_account;
CREATE TRIGGER user_account_audit_upd
    AFTER UPDATE ON user_account
    REFERENCING OLD TABLE AS old_table NEW TABLE AS new_table
    FOR EACH STATEMENT EXECUTE FUNCTION process_user_account_audit();
DROP TRIGGER if exists user_account_audit_del on  user_account;
CREATE TRIGGER user_account_audit_del
    AFTER DELETE ON user_account
    REFERENCING OLD TABLE AS old_table
    FOR EACH STATEMENT EXECUTE FUNCTION process_user_account_audit();