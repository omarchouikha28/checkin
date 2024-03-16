package checkin.appservices.services;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.urlaub.Urlaub;
import checkin.aggregates.user.User;
import checkin.appservices.configurations.PraktikumConfigurations;
import checkin.appservices.repositories.UrlaubRepository;
import checkin.appservices.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class UrlaubService {


    private final UrlaubRepository urlaubRepo;
    private final UserRepository userRepo;

    private final PraktikumConfigurations praktikumConfigurations;

    private final List<Exception> exceptions = new ArrayList<>();

    private final LocalDate HEUTE = LocalDateTime.now().toLocalDate();
    private final LocalTime JETZT = LocalDateTime.now().toLocalTime();

    public UrlaubService(UrlaubRepository urlaubRepo,
                         UserRepository userRepo,
                         PraktikumConfigurations praktikumConfigurations) {
        this.urlaubRepo = urlaubRepo;
        this.userRepo = userRepo;
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

    public Urlaub getUrlaubByID(Long id) {
        return urlaubRepo.findUrlaubById(id);
    }

    public void forceUrlaubAnmeldung(Urlaub urlaub, String githubID) {
        schreibeLog(LocalDateTime.now() + ": " + githubID + " hat folgenden Urlaub angemeldet: " + urlaub + "\n");
        urlaubRepo.save(urlaub);
    }

    public void forceUrlaubStornierung(Urlaub urlaub, String githubID) {
        urlaubRepo.deleteUrlaubById(urlaub.id());
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public void stornieren(Long id, String githubID) {
        urlaubRepo.deleteUrlaubById(id);
    }

    public void anmelden(Urlaub urlaub, String githubID) {
        exceptions.clear();
        if (existiertNicht(urlaub, githubID)) {
            lookForExceptions(urlaub);
            if (isValid()) {
                ueberlappen(urlaub, githubID);
            }
        }
    }

    private boolean existiertNicht(Urlaub urlaub, String githubID) {
        return userRepo.findUrlaube(githubID)
                .stream()
                .filter(u -> u.datum().compareTo(urlaub.datum()) == 0)
                .noneMatch(
                        u -> u.zeitspanne().von().compareTo(urlaub.zeitspanne().von()) == 0
                                && u.zeitspanne().bis().compareTo(urlaub.zeitspanne().bis()) == 0
                );
    }

    private boolean isValid() {
        return exceptions.isEmpty();
    }

    private boolean liegtAmWochenende(Urlaub urlaub) {
        DayOfWeek d = urlaub.datum().getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private boolean liegtInVergangenheit(Urlaub urlaub) {
        return HEUTE.compareTo(urlaub.datum()) > 0
                || (HEUTE.compareTo(urlaub.datum()) == 0 && JETZT.compareTo(urlaub.zeitspanne().von()) > 0);
    }

    private boolean liegtNachDemPraktikum(Urlaub urlaub) {
        return (praktikumConfigurations.getEndDate().compareTo(urlaub.datum()) < 0);
    }

    private void lookForExceptions(Urlaub urlaub) {

        if (liegtAmWochenende(urlaub))
            exceptions.add(new Exception("Tag liegt am Wochenende"));

        if (liegtNachDemPraktikum(urlaub))
            exceptions.add(new Exception("Das Datum ist nach dem letzten Praktikumstag am "
                    + praktikumConfigurations.getEndDate()));

        if (liegtInVergangenheit(urlaub))
            exceptions.add(new Exception("Urlaubszeitpunkt liegt in Vergangenheit"));

        if (startzeitLiegtNichtImViertelstundenTakt(urlaub) || endzeitLiegtNichtImViertelstundenTakt(urlaub))
            exceptions.add(new Exception("Es sind nur ganze Viertelstunden (d.h. 00, 15, 30 und 45) erlaubt."));

        if (startzeitLiegtNichtVorDerEndzeit(urlaub))
            exceptions.add(new Exception("Das Ende des Urlaubs muss hinter dem Anfang liegen."));

        if (startzeitZuFrueh(urlaub))
            exceptions.add(new Exception("Die frueheste erlaubte Startzeit fuer Sie ist "
                    + praktikumConfigurations.getStartTime()));

        if (endzeitZuSpaet(urlaub))
            exceptions.add(new Exception("Die spaeteste erlaubte Endzeit fuer Sie ist "
                    + praktikumConfigurations.getEndTime()));


    }

    private boolean startzeitLiegtNichtImViertelstundenTakt(Urlaub urlaub) {
        return urlaub.zeitspanne().von().getMinute() % 15 != 0;
    }

    private boolean endzeitLiegtNichtImViertelstundenTakt(Urlaub urlaub) {
        return urlaub.zeitspanne().bis().getMinute() % 15 != 0;
    }

    private boolean startzeitLiegtNichtVorDerEndzeit(Urlaub urlaub) {
        return urlaub.zeitspanne().von().compareTo(urlaub.zeitspanne().bis()) >= 0;
    }

    private boolean startzeitZuFrueh(Urlaub urlaub) {
        return urlaub.zeitspanne().von().compareTo(praktikumConfigurations.getStartTime()) < 0;
    }

    private boolean endzeitZuSpaet(Urlaub urlaub) {
        return urlaub.zeitspanne().bis().compareTo(praktikumConfigurations.getEndTime()) > 0;
    }

    private boolean zuWenigResturlaub(Urlaub urlaub, String githubID) {
        List<Urlaub> genommeneUrlaube = userRepo.findUrlaube(githubID);
        System.out.println(genommeneUrlaube);
        int summeUrlaubeNichtAmSelbenTag= genommeneUrlaube.stream().filter(u-> !u.datum().isEqual(urlaub.datum()))
                .map(Urlaub::zeitspanne)
                .mapToInt(Zeitraum::berechneDauer)
                .sum();
        int summeUrlaubeAmSelbenTag = genommeneUrlaube.stream()
                .filter(u -> u.datum().isEqual(urlaub.datum()))

                .map(Urlaub::zeitspanne)
                .filter(
                        u -> u.von().compareTo(urlaub.zeitspanne().von()) != 0
                                && u.bis().compareTo(urlaub.zeitspanne().bis()) != 0
                )
                .mapToInt(Zeitraum::berechneDauer)
                .sum();
        int summe=summeUrlaubeAmSelbenTag+summeUrlaubeNichtAmSelbenTag;
        return praktikumConfigurations.getMaxHolidays() - summe - urlaub.zeitspanne().berechneDauer() < 0;
    }

    private boolean zeitspanneNichtKonform(Urlaub urlaub) {
        return urlaub.zeitspanne().berechneDauer() > 150 && urlaub.zeitspanne().berechneDauer() != 240;
    }

    private void ueberlappen(Urlaub urlaub, String githubID) {
        List<Urlaub> urlaube = userRepo.findUrlaube(githubID);
        if (urlaube.stream().anyMatch(u -> u.zeitspanne() == urlaub.zeitspanne())) return;
        List<Urlaub> doppelteUrlaube = urlaube
                .stream()
                .filter(c -> c.datum().compareTo(urlaub.datum()) == 0)
                .toList();

        switch (doppelteUrlaube.size()) {
            case 0 -> {
                mergingExceptions(urlaub, githubID);
                saveUrlaubIfPossible(urlaub, githubID);

            }
            case 1 -> {
                Urlaub gespeicherterUrlaub = doppelteUrlaube.get(0);
                einBlock(urlaub, githubID, gespeicherterUrlaub);
            }
            case 2 -> {
                Urlaub linkerUrlaub = doppelteUrlaube.get(0);
                Urlaub rechterUrlaub = doppelteUrlaube.get(1);
                zweiBloecke(urlaub, githubID, linkerUrlaub, rechterUrlaub);
            }
        }

    }

    private void einBlock(Urlaub toBeSaved, String githubID, Urlaub gespeicherterUrlaub) {
        if (ueberlapptNachLinks(toBeSaved, gespeicherterUrlaub)) {
            merge(toBeSaved, githubID, gespeicherterUrlaub, gespeicherterUrlaub.zeitspanne());
            return;
        } else if (ueberlapptNachRechts(toBeSaved, gespeicherterUrlaub)) {
            merge(toBeSaved, githubID, gespeicherterUrlaub, toBeSaved.zeitspanne());
            return;
        }
        if (urlaubAmEnde(toBeSaved) && urlaubAmAnfang(gespeicherterUrlaub)
                || urlaubAmAnfang(toBeSaved) && urlaubAmEnde(gespeicherterUrlaub)) {
            mergingExceptions(toBeSaved, githubID);
            saveUrlaubIfPossible(toBeSaved, githubID);
            return;
        }
        exceptions.add(new Exception("Die Urlaubsbloecke muessen am Anfang und am Ende liegen"));
    }


    private void saveUrlaubIfPossible(Urlaub toBeSaved, String githubID) {
        if (exceptions.isEmpty()) {
            urlaubRepo.save(toBeSaved);
            User user = userRepo.findByGithubId(githubID);
            schreibeLog(LocalDateTime.now() + ": " + githubID + " hat folgenden Urlaub angemeldet: " + toBeSaved + "\n");
            user.addUrlaub(toBeSaved);
            userRepo.update(user);
        }
    }

    private void zweiBloecke(Urlaub toBeSaved, String githubID, Urlaub linkerUrlaub, Urlaub rechterUrlaub) {
        if (kompletteUeberlappung(toBeSaved, linkerUrlaub, rechterUrlaub)) {
            Urlaub ganzenTag = new Urlaub(
                    linkerUrlaub.id(),
                    linkerUrlaub.datum(),
                    new Zeitraum(praktikumConfigurations.getStartTime(), praktikumConfigurations.getEndTime())
            );

            exceptions.clear();
            mergingExceptions(ganzenTag, githubID);
            if (!exceptions.isEmpty()) {
                urlaubRepo.save(linkerUrlaub);
                urlaubRepo.save(rechterUrlaub);
            } else
                saveUrlaubIfPossible(ganzenTag, githubID);
            return;
        } else if (ueberlapptNachLinks(toBeSaved, linkerUrlaub) && genuegendArbeitszeit(toBeSaved, rechterUrlaub)) {
            merge(toBeSaved, githubID, linkerUrlaub, linkerUrlaub.zeitspanne());
            return;
        } else if (ueberlapptNachRechts(toBeSaved, rechterUrlaub) && genuegendArbeitszeit(toBeSaved, linkerUrlaub)) {
            merge(toBeSaved, githubID, rechterUrlaub, toBeSaved.zeitspanne());
            return;
        }
        exceptions.add(new Exception("Die Urlaubsbloecke muessen am Anfang und am Ende liegen"));
    }

    private void merge(Urlaub urlaub, String githubID, Urlaub urlaubEintrag, Zeitraum zeitspanne) {
        Urlaub neuesUrlaubsKonstrukt = new Urlaub(
                urlaubEintrag.id(),
                urlaubEintrag.datum(),
                new Zeitraum(zeitspanne.von(), urlaub.zeitspanne().bis())
        );
        checkIfMergingIsPossible(urlaubEintrag, githubID, neuesUrlaubsKonstrukt);
    }

    private void checkIfMergingIsPossible(Urlaub savedUrlaub1, String githubID, Urlaub neuesUrlaubsKonstrukt) {
        exceptions.clear();

        mergingExceptions(neuesUrlaubsKonstrukt, githubID);
        if (!exceptions.isEmpty()) {
            schreibeLog(LocalDateTime.now() + ": " + githubID + " hat folgenden Urlaub angemeldet: " + savedUrlaub1 + "\n");
            urlaubRepo.save(savedUrlaub1);
        } else
            saveUrlaubIfPossible(neuesUrlaubsKonstrukt, githubID);
        schreibeLog(LocalDateTime.now() + ": " + githubID + " hat folgenden Urlaub angemeldet: " + neuesUrlaubsKonstrukt + "\n");

    }

    private void mergingExceptions(Urlaub neuesUrlaubsKonstrukt, String githubID) {

        if (zuWenigResturlaub(neuesUrlaubsKonstrukt, githubID))
            exceptions.add(new Exception("Sie haben nicht mehr so viel Resturlaub uebrig"));

        if (zeitspanneNichtKonform(neuesUrlaubsKonstrukt))
            exceptions.add(new Exception("Sie koennen entweder den gesamten Tag frei nehmen, oder bis zu 2,5 Stunden"));

    }

    private boolean ueberlapptNachLinks(Urlaub neuerUrlaub, Urlaub gespeichertenUrlaub) {
        return neuerUrlaub.zeitspanne().von().compareTo(gespeichertenUrlaub.zeitspanne().bis()) <= 0
                && neuerUrlaub.zeitspanne().von().compareTo(gespeichertenUrlaub.zeitspanne().von()) > 0;
    }

    private boolean ueberlapptNachRechts(Urlaub neuerUrlaub, Urlaub gespeichertenUrlaub) {
        return neuerUrlaub.zeitspanne().von().compareTo(gespeichertenUrlaub.zeitspanne().von()) <= 0
                && neuerUrlaub.zeitspanne().bis().compareTo(gespeichertenUrlaub.zeitspanne().von()) > 0;
    }

    private boolean kompletteUeberlappung(Urlaub toBeSaved, Urlaub linkerUrlaub, Urlaub rechterUrlaub) {
        return ueberlapptNachLinks(toBeSaved, linkerUrlaub) && ueberlapptNachRechts(toBeSaved, rechterUrlaub);
    }

    private boolean genuegendArbeitszeit(Urlaub urlaub1, Urlaub urlaub2) {
        int comparator = urlaub1.zeitspanne().von().compareTo(urlaub2.zeitspanne().von());
        long duration;
        if (comparator < 0) {
            duration = Duration.between(urlaub1.zeitspanne().bis(), urlaub2.zeitspanne().von()).toMinutes();
        } else {
            duration = Duration.between(urlaub2.zeitspanne().bis(), urlaub1.zeitspanne().von()).toMinutes();
        }
        if (duration < 90) {
            exceptions.add(
                    new Exception("Es muessen mindestens 90 Minuten Arbeitszeit zwischen die Urlaubsblöcke sein"));
        }
        return duration > 90;
    }

    private boolean urlaubAmAnfang(Urlaub urlaub) {
        return urlaub.zeitspanne().von().compareTo(praktikumConfigurations.getStartTime()) == 0;
    }

    private boolean urlaubAmEnde(Urlaub urlaub) {
        return urlaub.zeitspanne().bis().compareTo(praktikumConfigurations.getEndTime()) == 0;
    }

    public UserRepository getUserRepository() {
        return userRepo;
    }
}