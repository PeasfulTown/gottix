package xyz.peasfultown.gottix.ticket_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objMapper;

	// contants
	protected static final String HEADER_USER_ID   = "X-User-Id";
	protected static final String HEADER_USER_ROLE = "X-User-Role";

	protected static final String ADMIN_ID     = "00000000-0000-0000-0000-000000000001";
	protected static final String AGENT_ID     = "00000000-0000-0000-0000-000000000002";
	protected static final String AGENT_ID_2   = "00000000-0000-0000-0000-000000000003";
	protected static final String CUSTOMER_ID  = "00000000-0000-0000-0000-000000000004";
	protected static final String CUSTOMER_ID_2 = "00000000-0000-0000-0000-000000000005";

	protected static final String ROLE_ADMIN    = "ROLE_ADMIN";
	protected static final String ROLE_AGENT    = "ROLE_AGENT";
	protected static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";

	// helper mock http request builder
	protected MockHttpServletRequestBuilder withAdmin(MockHttpServletRequestBuilder builder) {
		return builder
				.header(HEADER_USER_ID,   ADMIN_ID)
				.header(HEADER_USER_ROLE, ROLE_ADMIN)
				.contentType(APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder withAgent(MockHttpServletRequestBuilder builder) {
		return builder
				.header(HEADER_USER_ID,   AGENT_ID)
				.header(HEADER_USER_ROLE, ROLE_AGENT)
				.contentType(APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder withAgent2(MockHttpServletRequestBuilder builder) {
		return builder
				.header(HEADER_USER_ID,   AGENT_ID_2)
				.header(HEADER_USER_ROLE, ROLE_AGENT)
				.contentType(APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder withCustomer(MockHttpServletRequestBuilder builder) {
		return builder
				.header(HEADER_USER_ID,   CUSTOMER_ID)
				.header(HEADER_USER_ROLE, ROLE_CUSTOMER)
				.contentType(APPLICATION_JSON);
	}

	protected MockHttpServletRequestBuilder withCustomer2(MockHttpServletRequestBuilder builder) {
		return builder
				.header(HEADER_USER_ID,   CUSTOMER_ID_2)
				.header(HEADER_USER_ROLE, ROLE_CUSTOMER)
				.contentType(APPLICATION_JSON);
	}

	// obj to json helper

	protected String toJson(Object obj) throws Exception {
		return objMapper.writeValueAsString(obj);
	}

	// common ticket req bodies

	protected String createTicketBody(String title, String description, String priority) throws Exception {
		return toJson(java.util.Map.of(
				"title",       title,
				"description", description,
				"priority",    priority
		));
	}

	protected String defaultTicketBody() throws Exception {
		return createTicketBody("Cannot login to my account",
				"I have been trying to login since this morning and keep getting invalid credentials.",
				"MEDIUM");
	}

	protected String extractId(ResultActions result) throws Exception {
		String response = result.andReturn().getResponse().getContentAsString();
		return objMapper.readTree(response).get("id").asText();
	}

}
