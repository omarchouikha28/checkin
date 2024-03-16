package checkin.appservices.services;


import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.urlaub.UrlaubRef;
import checkin.aggregates.user.User;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PraktikumService {

    private final UrlaubService urlaubService;
    private final UserRepository userRepository;
    private final PraktikumConfigurations praktikumConfigurations;

    public PraktikumService(UrlaubService urlaubService,
                            UserRepository userRepository, PraktikumConfigurations praktikumConfigurations) {
        this.urlaubService = urlaubService;
        this.userRepository = userRepository;
        this.praktikumConfigurations = praktikumConfigurations;
    }

    public void schreibeLog(String log) {
        try (FileWriter writer = new FileWriter("log.txt", true);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write(log);
            bw.newLine();

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    public User userAnlegen(String githubID) {
        if (getUser(githubID) == null) {
            User user = new User(githubID);
            userRepository.save(user);
            return new User(githubID);
        } else
            return getUser(githubID);
    }

    public User getUser(String githubID) {
        return userRepository.findByGithubId(githubID);
    }

    public void urlaubBuchen(Urlaub urlaub, String githubID) {
        if (keineKlausurAmSelbenTag(urlaub.datum(), githubID)) {
            urlaubService.anmelden(urlaub, githubID);
        } else {
            List<Klausur> klausurenAmSelbenTag = klausurenAmSelbenTag(urlaub, githubID);

            if (klausurenAmSelbenTag.size() != 1) {
                Urlaub newUrlaub = new Urlaub(
                        urlaub.id(),
                        urlaub.datum(),
                        klausurfreierBereich(githubID)
                );

                User user = userRepository.findByGithubId(githubID);

                urlaubService.forceUrlaubAnmeldung(newUrlaub, githubID);
                user.addUrlaub(newUrlaub);
                urlaubAuditlogAusfuellen(newUrlaub, githubID, " hat den folgenden Urlaub gebucht: \n");
                if (user.getResturlaub() < 0) {
                    urlaubService.forceUrlaubStornierung(newUrlaub, githubID);
                    urlaubAuditlogAusfuellen(newUrlaub, githubID, " hat den folgenden Urlaub storniert: \n");
                    user.deleteUrlaub(newUrlaub);
                }
                userRepository.update(user);
            } else {

                Zeitraum freistellung = freistellung(
                        klausurenAmSelbenTag.get(0).zeitspanne().von(),
                        klausurenAmSelbenTag.get(0).zeitspanne().bis(),
                        klausurenAmSelbenTag.get(0).praesenz()
                );
                manageFreistellung(githubID, freistellung, urlaub);
            }

        }
    }

    public void urlaubStornieren(Urlaub urlaub, String githubID) {
        urlaubService.stornieren(urlaub.id(), "");
        urlaubAuditlogAusfuellen(urlaub, githubID, " hat den folgenden Urlaub storniert: \n");

        User user = getUser(githubID);
        user.deleteUrlaub(urlaub);
        userRepository.update(user);
    }

    public Urlaub getUrlaubByID(Long id) {
        return urlaubService.getUrlaubByID(id);
    }

    public List<Urlaub> getUserUrlaube(String githubID) {
        return userRepository.findUrlaube(githubID);
    }

    public List<Klausur> getUserKlausuren(String githubID) {
        return userRepository.findKlausuren(githubID);
    }

    public List<Exception> getUrlaubExceptions() {
        return urlaubService.getExceptions();
    }

    public void fuerKlausurAnmelden(Klausur klausur, String githubID) {
        if (keinUrlaubAmSelbenTag(klausur.datum(), githubID)) {
            klausurAuditlogAusfuellen(klausur, githubID, " hat sich fuer folgende Klausur angemeldet: \n");
            addKlausur(klausur, githubID);
        } else {
            List<Urlaub> urlaubeImSelbenTag = userRepository.findUrlaube(githubID)
                    .stream()
                    .filter(u -> u.datum().compareTo(klausur.datum()) == 0)
                    .collect(Collectors.toList());

            Zeitraum freistellung = freistellung(
                    klausur.zeitspanne().von(),
                    klausur.zeitspanne().bis(),
                    klausur.praesenz()
            );

            for (Urlaub urlaub : urlaubeImSelbenTag) {
                manageFreistellung(githubID, freistellung, urlaub);
                klausurAuditlogAusfuellen(klausur, githubID, " hat sich fuer folgende Klausur angemeldet: \n");
                addKlausur(klausur, githubID);
            }
        }
    }

    public void klausurStornieren(Klausur klausur, String githubID) {
        klausurAuditlogAusfuellen(klausur, githubID, " hat folgende Klausur storniert: \n");
        User user = getUser(githubID);
        user.deleteKlausur(klausur);
        userRepository.update(user);
    }

    private List<Klausur> klausurenAmSelbenTag(Urlaub urlaub, String githubID) {
        return userRepository.findKlausuren(githubID)
                .stream()
                .filter(u -> u.datum().compareTo(urlaub.datum()) == 0)
                .collect(Collectors.toList());
    }

    private boolean keinUrlaubAmSelbenTag(LocalDate datum, String githubID) {
        List<Urlaub> urlaube = userRepository.findUrlaube(githubID);
        return urlaube.stream()
                .noneMatch(u -> u.datum().compareTo(datum) == 0);
    }

    private boolean keineKlausurAmSelbenTag(LocalDate datum, String githubID) {
        List<Klausur> klausuren = userRepository.findKlausuren(githubID);
        return klausuren.stream()
                .noneMatch(u -> u.datum().compareTo(datum) == 0);
    }

    private Zeitraum klausurfreierBereich(String githubID) {
        List<Klausur> klausuren = userRepository.findKlausuren(githubID);
        List<Zeitraum> zeitraeume = klausuren
                .stream()
                .map(k -> freistellung(k.zeitspanne().von(), k.zeitspanne().bis(), k.praesenz()))
                .collect(Collectors.toList());

        return new Zeitraum(zeitraeume.get(0).bis(), zeitraeume.get(1).von());
    }

    public Zeitraum freistellung(LocalTime klausurBeginn, LocalTime klausurEnde, boolean praesenz) {
        Zeitraum frei;
        if (praesenz) {
            frei = new Zeitraum(klausurBeginn.minusHours(2), klausurEnde.plusHours(2));

        } else
            frei = new Zeitraum(klausurBeginn.minusMinutes(30), klausurEnde);

        if (frei.von().compareTo(praktikumConfigurations.getStartTime()) < 0)
            frei.setVon(praktikumConfigurations.getStartTime());
        if (frei.bis().compareTo(praktikumConfigurations.getEndTime()) > 0)
            frei.setBis(praktikumConfigurations.getEndTime());

        return frei;
    }

    private void klausurAuditlogAusfuellen(Klausur klausur, String githubID, String message) {
        schreibeLog(
                LocalDateTime.now()
                        + ": \n"
                        + githubID
                        + message
                        + klausur
                        + "\n"
        );
    }

    private void urlaubAuditlogAusfuellen(Urlaub urlaub, String githubID, String message) {
        schreibeLog(
                LocalDateTime.now()
                        + ": "
                        + githubID
                        + message
                        + urlaub
                        + "\n"
        );
    }

    private void addKlausur(Klausur klausur, String githubID) {
        User user = getUser(githubID);
        user.addKlausur(klausur);
        userRepository.update(user);
    }

    private void addUrlaub(Urlaub newUrlaub, String githubID) {
        User user = userRepository.findByGithubId(githubID);

        urlaubService.forceUrlaubAnmeldung(newUrlaub, githubID);
        urlaubAuditlogAusfuellen(newUrlaub, githubID, " hat den folgenden Urlaub gebucht: \n");
        user.addUrlaub(newUrlaub);
        userRepository.update(user);
    }

    private void manageFreistellung(String githubID, Zeitraum freistellung, Urlaub urlaub) {

        if (urlaubLaengerAlsKlausur(freistellung, urlaub))
            urlaubInZweiAufteilen(githubID, freistellung, urlaub);

        else if (klausurLaengerAlsUrlaub(freistellung, urlaub))
            urlaubWaehrendDerKlausur(githubID, urlaub);

        else if (ueberlappungNachRechts(freistellung, urlaub))
            urlaubNachDerKlausur(githubID, freistellung, urlaub);

        else if (uerberlappungNachLinks(freistellung, urlaub))
            urlaubVorDerKlausur(githubID, freistellung, urlaub);

        else if(!urlaubLiegtNichtImZeitraum(urlaub))
            addUrlaub(urlaub, githubID);
    }

    private boolean urlaubLiegtNichtImZeitraum(Urlaub urlaub) {
        return urlaub.zeitspanne().von().compareTo(praktikumConfigurations.getStartTime()) < 0
                || urlaub.zeitspanne().bis().compareTo(praktikumConfigurations.getEndTime()) > 0;
    }

    private boolean uerberlappungNachLinks(Zeitraum freistellung, Urlaub urlaub) {
        return freistellung.von().isAfter(urlaub.zeitspanne().von())
                && urlaub.zeitspanne().bis().isAfter(freistellung.von());
    }

    private boolean ueberlappungNachRechts(Zeitraum freistellung, Urlaub urlaub) {
        return freistellung.bis().isBefore(urlaub.zeitspanne().bis())
                && urlaub.zeitspanne().von().isBefore(freistellung.bis());
    }

    private boolean klausurLaengerAlsUrlaub(Zeitraum freistellung, Urlaub urlaub) {
        return freistellung.von().compareTo(urlaub.zeitspanne().von()) <= 0
                && freistellung.bis().compareTo(urlaub.zeitspanne().bis()) >= 0;
    }

    private boolean urlaubLaengerAlsKlausur(Zeitraum freistellung, Urlaub urlaub) {
        return freistellung.von().compareTo(urlaub.zeitspanne().von()) > 0
                && freistellung.bis().compareTo(urlaub.zeitspanne().bis()) < 0;
    }

    private void urlaubWaehrendDerKlausur(String githubID, Urlaub urlaub) {
        User user = getUser(githubID);

        if (user.getUrlaube().contains(new UrlaubRef(urlaub.id()))) {
            urlaubService.forceUrlaubStornierung(urlaub, githubID);
            urlaubAuditlogAusfuellen(urlaub, githubID, " hat den folgenden Urlaub storniert: \n");

            user.deleteUrlaub(urlaub);
            userRepository.update(user);
        }
    }

    private void urlaubNachDerKlausur(String githubID, Zeitraum freistellung, Urlaub urlaub) {
        User user = getUser(githubID);

        Urlaub newUrlaub = new Urlaub(
                urlaub.id(),
                urlaub.datum(),
                new Zeitraum(freistellung.bis(), urlaub.zeitspanne().bis())
        );

        urlaubService.forceUrlaubAnmeldung(newUrlaub, githubID);
        urlaubAuditlogAusfuellen(newUrlaub, githubID, " hat den folgenden Urlaub gebucht: \n");

        user.addUrlaub(newUrlaub);
        userRepository.update(user);
    }

    private void urlaubVorDerKlausur(String githubID, Zeitraum freistellung, Urlaub urlaub) {
        User user = getUser(githubID);

        Urlaub newUrlaub1 = new Urlaub(
                urlaub.id(),
                urlaub.datum(),
                new Zeitraum(urlaub.zeitspanne().von(), freistellung.von())
        );


        urlaubService.forceUrlaubAnmeldung(newUrlaub1, githubID);
        urlaubAuditlogAusfuellen(newUrlaub1, githubID, " hat den folgenden Urlaub gebucht: \n");

        user.addUrlaub(newUrlaub1);
        userRepository.update(user);
    }

    private void urlaubInZweiAufteilen(String githubID, Zeitraum freistellung, Urlaub urlaub) {
        User user = getUser(githubID);

        Urlaub newUrlaub1 = new Urlaub(
                urlaub.id(),
                urlaub.datum(),
                new Zeitraum(urlaub.zeitspanne().von(), freistellung.von())
        );

        Urlaub newUrlaub2 = new Urlaub(
                urlaub.id(),
                urlaub.datum(),
                new Zeitraum(freistellung.bis(), urlaub.zeitspanne().bis())
        );

        urlaubService.forceUrlaubAnmeldung(newUrlaub1, githubID);
        urlaubService.forceUrlaubAnmeldung(newUrlaub2, githubID);

        user.addUrlaub(newUrlaub1);
        user.addUrlaub(newUrlaub2);

        urlaubAuditlogAusfuellen(newUrlaub1, githubID, " hat den folgenden Urlaub gebucht: \n");
        urlaubAuditlogAusfuellen(newUrlaub2, githubID, " hat den folgenden Urlaub gebucht: \n");

        userRepository.update(user);
    }

}
