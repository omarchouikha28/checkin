package checkin.controller;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;
import checkin.appservices.services.PraktikumService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class UrlaubController {

    private final PraktikumService praktikumService;

    public UrlaubController(PraktikumService praktikumService) {
        this.praktikumService = praktikumService;
    }

    @GetMapping("/urlaubanmeldung")
    public String urlaubanmeldung(Model model) {
        List<Exception> fehler = praktikumService.getUrlaubExceptions();
        model.addAttribute("urlaubFehler", fehler);
        return "urlaubanmeldung";
    }

    @PostMapping("/urlaubanmeldung")
    public String urlaubBuchung(Model model, @AuthenticationPrincipal OAuth2User userObject,
                                @RequestParam("datum") String datum,
                                @RequestParam("von") String von,
                                @RequestParam("bis") String bis) {
        User user = praktikumService.userAnlegen(userObject.getAttribute("login"));
        Urlaub urlaub = new Urlaub(null,
                LocalDate.parse(datum),
                new Zeitraum(
                        LocalTime.parse(von),
                        LocalTime.parse(bis)
                )
        );
        praktikumService.urlaubBuchen(urlaub, user.getGithubID());
        List<Exception> urlaubFehler = praktikumService.getUrlaubExceptions();
        model.addAttribute("urlaubFehler", urlaubFehler);
        if (urlaubFehler.isEmpty())
            return "redirect:/";
        else
            return "urlaubanmeldung";
    }
}