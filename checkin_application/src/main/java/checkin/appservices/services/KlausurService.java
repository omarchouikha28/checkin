package checkin.appservices.services;

import checkin.aggregates.helper_classes.Zeitraum;
import checkin.aggregates.klausur.Klausur;
import checkin.appservices.repositories.KlausurRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class KlausurService {

    private final List<Exception> exceptions = new ArrayList<>();

    private final KlausurRepository klausurRepo;

    public KlausurService(KlausurRepository klausurRepo) {

        this.klausurRepo = klausurRepo;
    }

    public List<Exception> getExceptions() {
        return exceptions;
    }

    public boolean isValid() {
        return getExceptions().isEmpty();
    }

    public void klausurAnlegen(Long id, boolean praesenz, LocalDate datum, Zeitraum zeitspanne) {
        String url = "https://lsf.hhu.de/qisserver/rds?state=verpublish&status=init&vmfile=no&publishid=" +
                id + "&moduleCall=webInfo&publishConfFile=webInfo&publishSubDir=veranstaltung";
        try {
            Document doc = Jsoup.connect(url).get();

            if (doc.body().html().contains("<h1>")) {
                String lsfVeranstaltungsName = doc.getElementsByTag("h1").html();
                lsfVeranstaltungsName = lsfVeranstaltungsName.split("</div>")[1].strip();
                lsfVeranstaltungsName = lsfVeranstaltungsName.substring(0, lsfVeranstaltungsName.length() - 16);
                Klausur klausur = new Klausur(
                        id,
                        klausurArt(praesenz) + lsfVeranstaltungsName,
                        praesenz,
                        datum,
                        zeitspanne
                );
                sucheNachFehlern(klausur);
                if (isValid()) {
                    klausurRepo.save(klausur);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sucheNachFehlern(Klausur klausur) {
        exceptions.clear();

        if (!idExistiertImLSF(klausur)) {
            exceptions.add(new Exception("Die von Ihnen angegebene Klausur-ID existiert nicht!"));
        }

        if (klausurIDSchonVorhanden(klausur)) {
            exceptions.add(new Exception("Eine Klausur mit der identischen ID existiert bereits!"));
        }

        if (lsfEintragIstNichtKlausur(klausur)) {
            exceptions.add(new Exception("Der Veranstaltungstyp ist im LSF nicht als 'Klausur' angegeben!"));
        }
    }

    public Klausur klausurByID(long id) {
        return klausurRepo.findKlausurById(id);
    }

    public List<Klausur> alleKlausuren() {
        return klausurRepo.findAll();
    }

    private String klausurArt(boolean praesenz) {
        if (praesenz)
            return "Praesenzklausur ";
        else
            return "Onlineklausur ";
    }

    private boolean lsfEintragIstNichtKlausur(Klausur klausur) {
        String url = "https://lsf.hhu.de/qisserver/rds?state=verpublish&status=init&vmfile=no&publishid=" +
                klausur.id() + "&moduleCall=webInfo&publishConfFile=webInfo&publishSubDir=veranstaltung";
        try {
            Document doc = Jsoup.connect(url).get();
            if (doc.body().html().contains("headers=\"basic_1\"")) {
                String veranstaltungstyp = doc.getElementsByTag("td").html();
                return !veranstaltungstyp.startsWith("Klausur");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean klausurIDSchonVorhanden(Klausur klausur) {
        List<Klausur> alleKlausuren = klausurRepo.findAll();
        for (Klausur aktKlausur : alleKlausuren) {
            if (aktKlausur.id().compareTo(klausur.id()) == 0)
                return true;
        }
        return false;
    }

    private boolean idExistiertImLSF(Klausur klausur) {
        String url = "https://lsf.hhu.de/qisserver/rds?state=verpublish&status=init&vmfile=no&publishid=" +
                klausur.id() + "&moduleCall=webInfo&publishConfFile=webInfo&publishSubDir=veranstaltung";
        try {
            Document doc = Jsoup.connect(url).get();
            if (doc.body().html().contains("headers=\"basic_3\"")) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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

}

