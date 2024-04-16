CREATE DATABASE `ds_task_${tenantName}` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
CREATE DATABASE `gov_${tenantName}` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;
CREATE DATABASE `query_editor_${tenantName}` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin */;

--GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON `ds_task_${tenantName}`.* TO 'ds-task'@'%';
--GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON `gov_${tenantName}`.* TO 'datacake'@'%';
--GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON `query_editor_${tenantName}`.* TO 'query_editor'@'%';