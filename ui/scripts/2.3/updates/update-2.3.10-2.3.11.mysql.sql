UPDATE ObmInfo SET obminfo_value = '2.3.11-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE Campaign DROP FOREIGN KEY campaign_email_fkey;
ALTER TABLE Campaign ADD CONSTRAINT campaign_email_fkey FOREIGN KEY (campaign_email) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;

UPDATE ObmInfo SET obminfo_value = '2.3.11' WHERE obminfo_name = 'db_version';
