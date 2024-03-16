package checkin.controller;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.user.User;
import checkin.appservices.services.KlausurService;
import checkin.appservices.services.PraktikumService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Controller
public class KlausurController {

    private final KlausurService klausurService;
    private final PraktikumService praktikumService;

    public KlausurController(KlausurService klausurService, PraktikumService praktikumService) {
        this.klausurService = klausurService;
        this.praktikumService = praktikumService;
    }

    @GetMapping("/klausuranmeldung")
    public String klausurAnmeldung(Model model) {
        List<Klausur> klausuren = klausurService.alleKlausuren();
        List<String> options = new ArrayList<>();

        for (Klausur klausur : klausuren)
            options.add(klausur.toString());

        model.addAttribute("options", options);
        return "klausuranmeldung";
    }

    @PostMapping("/klausuranmeldung")
    public String addKlausur(Model model,
                             @AuthenticationPrincipal OAuth2User userObject,
                             @ModelAttribute("klausurName") String klausurName) {
        String id = userObject.getAttribute("login");
        User user = praktikumService.userAnlegen(id);

        var gewaehlteKlausur =
                Arrays.stream(
                        Objects.requireNonNull(model.getAttribute("klausurName"))
                                .toString()
                                .split("id= ")
                ).toList();
        long klausurID = Long.parseLong(gewaehlteKlausur.get(1));

        Klausur klausur = klausurService.klausurByID(klausurID);
        praktikumService.fuerKlausurAnmelden(klausur, user.getGithubID());

        return "redirect:/";
    }

    @GetMapping("/klausuranlegung")
    public String klausuranlegung(Model model) {
        List<Exception> klausurFehler = klausurService.getExceptions();
        model.addAttribute("klausurFehler", klausurFehler);
        return "klausuranlegung";
    }

    @PostMapping("/klausuranlegung")
    public String klausurInDatenbankSchreiben(Model model,
                                              @RequestParam("lsfid") String id,
                                              @RequestParam(value = "vor_ort", defaultValue = "false") String praesenz,
                                              @RequestParam("datum") String datum,
                                              @RequestParam("von") String von,
                                              @RequestParam("bis") String bis) {

        klausurService.klausurAnlegen(
                Long.parseLong(id),
                Boolean.parseBoolean(praesenz),
                LocalDate.parse(datum),
                new Zeitraum(LocalTime.parse(von), LocalTime.parse(bis))
        );

        List<Exception> klausurFehler = klausurService.getExceptions();
        model.addAttribute("klausurFehler", klausurFehler);

        if (klausurFehler.isEmpty()) {
            return "redirect:/klausuranlegungerfolg";
        } else {
            return "klausuranlegung";
        }
    }

    @GetMapping("/klausuranlegungerfolg")
    public String klausuranlegungerfolg() {

        return "klausuranlegungerfolg";
    }
}
