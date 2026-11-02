package dev.inreach.jobs;

import org.springframework.data.jpa.repository.*;

public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {}
