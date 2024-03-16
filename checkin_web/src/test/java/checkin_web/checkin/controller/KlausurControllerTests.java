package checkin_web.checkin.controller;

import checkin.aggregates.user.User;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.services.KlausurService;
import checkin.appservices.services.PraktikumService;
import checkin.configuration.MethodSecurityConfiguration;
import checkin.controller.KlausurController;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = KlausurController.class)
@Import({MethodSecurityConfiguration.class})
@ActiveProfiles("test")
@WebMvcTest
public class KlausurControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PraktikumService praktikumService;

    @MockBean
    KlausurService klausurService;

    MockHttpSession session;


    private static PraktikumConfigurations praktikumConfigurations;

    OAuth2AuthenticationToken buildPrincipal(String role, String name) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("login", name);

        List<GrantedAuthority> authorities = Collections.singletonList(
                new OAuth2UserAuthority("ROLE_" + role.toUpperCase(), attributes));
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
    void principalSetUp() {
        OAuth2AuthenticationToken principal = buildPrincipal("student", "Max Mustermann");
        session = new MockHttpSession();
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                new SecurityContextImpl(principal));
    }

    @Test
    @DisplayName("Seite für die Klausuranmeldung wird korrekt aufgerufen")
    void test_01() throws Exception {

        mockMvc.perform(get("/klausuranmeldung").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("klausuranmeldung"));
    }

    @Test
    @DisplayName("Nach der Klausuranmeldung kommt man erfolgreich auf die Startseite")
    void test_02() throws Exception {
        when(praktikumService.userAnlegen(any())).thenReturn(new User("Finalmaestro"));

        mockMvc.perform(post("/klausuranmeldung").session(session)
                        .param("klausurName", "dasd id= 214312")
                        .session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("Seite zum Anlegen einer Klausur wird korrekt angezeigt")
    void test_03() throws Exception {
        mockMvc.perform(get("/klausuranlegung").session(session)
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("klausuranlegung"));
    }

    @Test
    @DisplayName("Dem Benutzer wird angezeigt, dass das Anlegen einer Klausur erfolgreich war")
    void test_04() throws Exception {
        mockMvc.perform(get("/klausuranlegungerfolg").session(session)
                        .session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("klausuranlegungerfolg"));
    }

    @Test
    @DisplayName("Nach der fehlerhaften Eingabe bleibt der Benutzer auf der aktuellen Seite")
    void test_05() throws Exception {
        when(klausurService.getExceptions()).
                thenReturn(List.of(new Exception("Ist halt ne Exception")));

        mockMvc.perform(post("/klausuranlegung")

                .param("lsfid", "214321")
                .param("praesenz", "vor_ort")
                .param("datum", praktikumConfigurations.getEndDate().toString())
                .param("von", praktikumConfigurations.getEndTime().minusMinutes(30).toString())
                .param("bis", praktikumConfigurations.getEndTime().toString()).session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("klausuranlegung"));
    }

    @Test
    @DisplayName("Nach der erfolgreichen Klausuranlegung wird dem Benutzer angezeigt, dass die Klausuranlegung erfolgreich war")
    void test_06() throws Exception {
        when(klausurService.getExceptions()).
                thenReturn(List.of());

        mockMvc.perform(post("/klausuranlegung")
                        .param("lsfid", "214321")
                        .param("praesenz", "vor_ort")
                        .param("datum", praktikumConfigurations.getEndDate().toString())
                        .param("von", praktikumConfigurations.getEndTime().minusMinutes(30).toString())
                        .param("bis", praktikumConfigurations.getEndTime().toString()).session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/klausuranlegungerfolg"));
    }
}
