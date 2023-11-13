do $$
    declare table_names text;
begin
    table_names := string_agg('"' || schemaname || '"' || '.' || tablename, ',')
        from 
            pg_tables 
        where 
            schemaname = 's335093';
            
    execute 'drop table ' || table_names;
end $$;