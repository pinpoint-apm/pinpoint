-- Spring Batch 6.0 migration for an existing job repository created with the Spring Batch 5.x schema.
-- Spring Batch 6 renamed the job instance sequence table (see spring-batch-core migration/6.0/migration-mysql.sql).
RENAME TABLE BATCH_JOB_SEQ TO BATCH_JOB_INSTANCE_SEQ;
