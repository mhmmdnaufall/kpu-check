package mhmmdnaufall.kpucheck;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

class CheckingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "https://sirekap-obj-data.kpu.go.id";
    private final String WILAYAH_SEARCH_URL = "/wilayah/pemilu/ppwp";
    private final String TPS_SCORE_URS = "/pemilu/hhcw/ppwp";

    private final PrintStream printResult = new PrintStream(Files.newOutputStream(Path.of("result-2.txt")));

    CheckingTest() throws IOException {
    }

    @Test
    void name() throws JsonProcessingException {
        var kode = "/0.json";
        final var wilayah1 = restTemplate.getForObject(BASE_URL + WILAYAH_SEARCH_URL + kode, Wilayah[].class);
        assert wilayah1 != null;

        for (int i = 0; i < wilayah1.length; i++) {

            kode = "/" + wilayah1[i].kode();
            final var wilayah2 = restTemplate.getForObject(BASE_URL + WILAYAH_SEARCH_URL + kode + ".json", Wilayah[].class);
            assert wilayah2 != null;

            for (int j = 0; j < wilayah2.length; j++) {

                var nextKode2 = new StringBuilder();
                nextKode2.append("/").append(wilayah2[j].kode());
                final var wilayah3 = restTemplate.getForObject(BASE_URL + WILAYAH_SEARCH_URL + kode + nextKode2 + ".json", Wilayah[].class);
                assert wilayah3 != null;

                for (int k = 0; k < wilayah3.length; k++) {

                    var nextKode3 = new StringBuilder(nextKode2);
                    nextKode3.append("/").append(wilayah3[k].kode());
                    final var wilayah4 = restTemplate.getForObject(BASE_URL + WILAYAH_SEARCH_URL + kode + nextKode3 + ".json", Wilayah[].class);
                    assert wilayah4 != null;

                    for (int l = 0; l < wilayah4.length; l++) {

                        var nextKode4 = new StringBuilder(nextKode3);
                        nextKode4.append("/").append(wilayah4[l].kode());
                        final var wilayah5 = restTemplate.getForObject(BASE_URL + WILAYAH_SEARCH_URL + kode + nextKode4 + ".json", Wilayah[].class);
                        assert wilayah5 != null;

                        for (int m = 0; m < wilayah5.length; m++) {

                            final var json = restTemplate.getForObject(BASE_URL + TPS_SCORE_URS + kode + nextKode4 + ".json", String.class);
                            final var aniesScore = objectMapper.readTree(json).get("table").get(wilayah5[m].kode()).get("100025");
                            final var prabowoScore = objectMapper.readTree(json).get("table").get(wilayah5[m].kode()).get("100026");
                            final var ganjarScore = objectMapper.readTree(json).get("table").get(wilayah5[m].kode()).get("100027");

                            try {
                                if (
                                        Integer.parseInt(aniesScore.toString()) >= 300 ||
                                                Integer.parseInt(prabowoScore.toString()) >= 300 ||
                                                Integer.parseInt(ganjarScore.toString()) >= 300
                                ) {

                                    printResult.println("https://pemilu2024.kpu.go.id/pilpres/hitung-suara" + kode + nextKode4 + "/" + wilayah5[m].kode());
                                    printResult.println("Provinsi       : " + wilayah1[i].nama());
                                    printResult.println("Kabupaten/Kota : " + wilayah2[j].nama());
                                    printResult.println("Kecamatan      : " + wilayah3[k].nama());
                                    printResult.println("Kelurahan      : " + wilayah4[l].nama());
                                    printResult.println("TPS            : " + wilayah5[m].nama());
                                    printResult.println("Anies   : " + aniesScore);
                                    printResult.println("Prabowo : " + prabowoScore);
                                    printResult.println("Ganjar  : " + ganjarScore);
                                    printResult.println("=================================================");
                                }
                            } catch (NullPointerException ignored) {

                            }

                        }


                    }

                }

            }

        }
    }
}

record Wilayah(long id, String nama, String kode, int tingkat) { }
