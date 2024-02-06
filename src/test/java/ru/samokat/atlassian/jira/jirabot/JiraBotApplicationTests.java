package ru.samokat.atlassian.jira.jirabot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"sdk", "default"})
class JiraBotApplicationTests {

	@Test
	void contextLoads() {
	}

}
