package com.sap.vcs.server;

import com.sap.vcs.server.repository.ApprovalRepository;
import com.sap.vcs.server.repository.AuditLogRepository;
import com.sap.vcs.server.repository.DocumentRepository;
import com.sap.vcs.server.repository.DocumentVersionRepository;
import com.sap.vcs.server.repository.RoleRepository;
import com.sap.vcs.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(
		properties = {
				"spring.flyway.enabled=false",
				"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
		}
)
class ServerApplicationTests {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private RoleRepository roleRepository;

	@MockBean
	private DocumentRepository documentRepository;

	@MockBean
	private DocumentVersionRepository documentVersionRepository;

	@MockBean
	private ApprovalRepository approvalRepository;

	@MockBean
	private AuditLogRepository auditLogRepository;

	@Test
	void contextLoads() {
	}
}