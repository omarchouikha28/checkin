package checkin_web.checkin.controller;


import checkin.aggregates.user.User;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.services.PraktikumService;
import checkin.configuration.MethodSecurityConfiguration;
import checkin.controller.UrlaubController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = UrlaubController.class)
@Import({MethodSecurityConfiguration.class})
@ActiveProfiles("test")
@WebMvcTest
class UrlaubControllerTests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    PraktikumService praktikumService;

    MockHttpSession session;

    private static PraktikumConfigurations praktikumConfigurations;

    OAuth2AuthenticationToken buildPrincipal(String role, String name) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", name);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new OAuth2UserAuthority("ROLE_"+ role.toUpperCase(), attributes));
        OAuth2User user = new DefaultOAuth2User(authorities, attributes, "login");
        return new OAuth2AuthenticationToken(user, authorities, "whatever");
    }

    @BeforeAll
    static void setUp() {
        praktikumConfigurations = new PraktikumConfigurations(
                "2022-03-07",
                "2022-03-25",
                "09:30",
                "13:30",
                "240"
        );
    }

    @BeforeEach
    void principalSetUp(){
        OAuth2AuthenticationToken principal = buildPrincipal("student", "Max Mustermann");
        session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(principal));
    }

    @Test
    @DisplayName("Die Seite für die Urlaubsanmeldung wird erfolgreich angezeigt")
    void test_01() throws Exception {

        when(praktikumService.getUrlaubExceptions()).thenReturn(List.of());

        mockMvc.perform(get("/urlaubanmeldung").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("urlaubanmeldung"));
    }

    @Test
    @DisplayName("Nach dem fehlerlosen Anlegen, wird der Benutzer wieder zur Startseite weitergeleitet")
    void test_02() throws Exception {

        when(praktikumService.getUrlaubExceptions()).
                thenReturn(List.of());
        when(praktikumService.userAnlegen(any())).thenReturn(new User("Finalmaestro"));

        mockMvc.perform(post("/urlaubanmeldung")
                        .param("datum", praktikumConfigurations.getEndDate().toString())
                        .param("von", praktikumConfigurations.getEndTime().minusMinutes(30).toString())
                        .param("bis", praktikumConfigurations.getEndTime().toString())
                        .session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Nach der fehlerhaften Anlegung, bleibt der Benutzer auf der aktuellen Seite")
    void test_03() throws Exception {

        when(praktikumService.getUrlaubExceptions()).
                thenReturn(List.of(new Exception("Ist halt ne Exception")));

        when(praktikumService.userAnlegen(any())).thenReturn(new User("Finalmaestro"));

        mockMvc.perform(post("/urlaubanmeldung")
                        .param("datum", praktikumConfigurations.getEndDate().toString())
                        .param("von", praktikumConfigurations.getEndTime().minusMinutes(30).toString())
                        .param("bis", praktikumConfigurations.getEndTime().toString())
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("urlaubanmeldung"));
    }
}