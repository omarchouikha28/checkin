package checkin.controller;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;
import checkin.appservices.services.KlausurService;
import checkin.appservices.services.PraktikumService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CheckinController {

    private final PraktikumService praktikumService;
    private final KlausurService klausurService;

    public CheckinController(PraktikumService praktikumService,
                             KlausurService klausurService) {
        this.praktikumService = praktikumService;
        this.klausurService = klausurService;
    }

    @GetMapping("/")
    public String main(Model model, @AuthenticationPrincipal OAuth2User userObject) {
        String id = userObject.getAttribute("login");
        User user = praktikumService.userAnlegen(id);

        List<Urlaub> urlaube = praktikumService.getUserUrlaube(id);
        List<Klausur> klausuren = praktikumService.getUserKlausuren(id);
        List<Zeitraum> freistellungen = klausuren.stream()
                .map(k -> praktikumService.freistellung(k.zeitspanne().von(), k.zeitspanne().bis(), k.praesenz()))
                .collect(Collectors.toList());

        model.addAttribute("freistellungen", freistellungen);
        model.addAttribute("user", user);
        model.addAttribute("urlaube", urlaube);
        model.addAttribute("klausuren", klausuren);

        return "uebersicht";
    }

    @PostMapping("/urlaub/{id}/delete")
    public String urlaubStornieren(@PathVariable Long id,
                                   @AuthenticationPrincipal OAuth2User userObject) {

        User user = praktikumService.userAnlegen(userObject.getAttribute("login"));
        praktikumService.urlaubStornieren(praktikumService.getUrlaubByID(id), user.getGithubID());

        return "redirect:/";
    }

    @PostMapping("/klausur/{id}/delete")
    public String klausurStornieren(@PathVariable Long id,
                                    @AuthenticationPrincipal OAuth2User userObject) {

        User user = praktikumService.userAnlegen(userObject.getAttribute("login"));
        praktikumService.klausurStornieren(klausurService.klausurByID(id), user.getGithubID());
        return "redirect:/";
    }

    @GetMapping("/tutor")
    public String tutor(Model model, @AuthenticationPrincipal OAuth2User userObject) {
        User user = praktikumService.userAnlegen(userObject.getAttribute("login"));
        model.addAttribute("user", user);
        return "tutor";
    }

    @GetMapping("/organisator")
    public String organisator(Model model, @AuthenticationPrincipal OAuth2User userObject) {
        User user = praktikumService.userAnlegen(userObject.getAttribute("login"));
        model.addAttribute("user", user);
        return "organisator";
    }

}
