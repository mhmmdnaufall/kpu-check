package mhmmdnaufall.kpucheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class Scrap {

    private static final AtomicInteger ANIES_FALSE = new AtomicInteger(0);
    private static final AtomicInteger PRABOWO_FALSE = new AtomicInteger(0);
    private static final AtomicInteger GANJAR_FALSE = new AtomicInteger(0);
    private final Logger log = LoggerFactory.getLogger(Scrap.class);
    private final KpuProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final PrintStream printResult;

    Scrap(KpuProperties properties, ObjectMapper objectMapper, RestTemplate restTemplate, PrintStream printStream) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.printResult = printStream;
    }

    @Async
    public void wilayahTrack(Wilayah[] wilayahs, String kode, String nextKode) throws JsonProcessingException {

        log.info(Thread.currentThread().toString());

        for (var wilayah : wilayahs) {

            if (wilayah.tingkat() == 5) {
                printResult(wilayahs, kode, nextKode);
                break;
            }

            var nextKode2 = new StringBuilder(nextKode);
            nextKode2.append("/").append(wilayah.kode());
            final var innerWilayah = restTemplate.getForObject(properties.baseUrl() + properties.wilayahSearchUrl() + kode + nextKode2 + ".json", Wilayah[].class);
            assert innerWilayah != null;

            wilayahTrack(innerWilayah, kode, nextKode2.toString());
        }
    }

    private void printResult(Wilayah[] wilayahs, String kode, String nextKode4) throws JsonProcessingException {
        for (var wilayah : wilayahs) {
            final var json = restTemplate.getForObject(properties.baseUrl() + properties.tpsScoreUrl() + kode + nextKode4 + ".json", String.class);
            final var aniesScore = objectMapper.readTree(json).get("table").get(wilayah.kode()).get("100025");
            final var prabowoScore = objectMapper.readTree(json).get("table").get(wilayah.kode()).get("100026");
            final var ganjarScore = objectMapper.readTree(json).get("table").get(wilayah.kode()).get("100027");

            try {
                if (
                        Integer.parseInt(aniesScore.toString()) > 300 ||
                                Integer.parseInt(prabowoScore.toString()) > 300 ||
                                Integer.parseInt(ganjarScore.toString()) > 300
                ) {

                    if (Integer.parseInt(aniesScore.toString()) > 300) ANIES_FALSE.incrementAndGet();
                    if (Integer.parseInt(prabowoScore.toString()) > 300) PRABOWO_FALSE.incrementAndGet();
                    if (Integer.parseInt(ganjarScore.toString()) > 300) GANJAR_FALSE.incrementAndGet();

                    synchronized (this) {
                        printResult.println("https://pemilu2024.kpu.go.id/pilpres/hitung-suara" + kode + nextKode4 + "/" + wilayah.kode());
                        printResult.println("Anies   : " + aniesScore);
                        printResult.println("Prabowo : " + prabowoScore);
                        printResult.println("Ganjar  : " + ganjarScore);
                        printResult.println("=================================================");
                    }

                }
            } catch (NullPointerException ignored) {
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        printResult.println("SELESAI");
        printResult.println("Total Kesalahan ANIES   : " + ANIES_FALSE);
        printResult.println("Total Kesalahan PRABOWO : " + PRABOWO_FALSE);
        printResult.println("Total Kesalahan GANJAR  : " + GANJAR_FALSE);
        printResult.println("=================================================");
    }

}
