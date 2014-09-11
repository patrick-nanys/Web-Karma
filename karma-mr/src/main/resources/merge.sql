CREATE TEMPORARY FUNCTION MergeJSON as 'edu.isi.karma.mapreduce.function.MergeJSON';
INSERT TABLE ${TABLE_NAME}
select MergeJSON(d.json, source_table.json, ${JSON_PATH}) as result from ${SOURCE_TABLE_NAME} source_table right outer join 
	(select b.json as json, regexp_replace(trim(c.uri_to_merge),'\\"', '') as uri from
		(select a.json, split(regexp_replace(if(a.raw_uris_to_merge IS NULL, "", a.uris_to_merge_string),'\\[|\\]','') ,',') as uris_to_merge_array from 
			(select json, get_json_object(json, concat("$.", ${JSON_PATH})) as uris_to_merge_string from 
				${TARGET_TABLE_NAME} 
			) a
		) b  lateral view explode(uris_to_merge_array) c as uri_to_merge
	) d 
on d.uri== get_json_object(source_table.json, "$.id") ;