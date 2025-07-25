-- Replace with your schema if needed, e.g. public.authors
-- 1. Remove child records first (foreign key constraints)
DELETE FROM authors WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM external_ids WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM publication_venues WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM paper_metrics WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM extracted_documents WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM human_summaries WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';
DELETE FROM structured_facts WHERE paper_id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';

-- 2. Remove the paper itself
DELETE FROM papers WHERE id = 'e67f214c-cf23-40b3-bf57-dae8cf39fd01';

-- 3. Optionally, remove any web_search_operations that reference this paper's correlation_id
-- (if you know the correlation_id, e.g. 'uploaded-...'; otherwise, you can look it up first)
-- Example:
-- DELETE FROM web_search_operations WHERE correlation_id = '<correlation_id_of_this_paper>';