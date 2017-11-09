-- Create view section based on Arcopole Models

CREATE MATERIALIZED VIEW #schema_cadastrapp.section AS 
	SELECT 
		section.cgocommune, 
		section.ccosec, 
		section.ccopre
   FROM dblink('host=#DBHost_arcopole port=#DBPort_arcopole dbname=#DBName_arcopole user=#DBUser_arcopole password=#DBpasswd_arcopole'::text, 
   		'select 
	   		codcomm as cgocommune,
	   		ltrim(substr(id_sect,10,2),''0'') as ccosec ,
	   		regexp_replace(substr(id_sect,7,3), ''0{3}'', '''') as ccopre
		from #DBSchema_arcopole.edi_sectio '::text) 
	section(
		cgocommune character varying(6), 
		ccosec character varying(2), 
		ccopre character varying(3));

ALTER TABLE #schema_cadastrapp.section OWNER TO #user_cadastrapp;
