package mhmmdnaufall.kpucheck;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(KpuProperties.class)
public class KpuCheckApplication {

	private final KpuProperties properties;

	public final RestTemplate restTemplate;

	public final Scrap scrap;

	KpuCheckApplication(Scrap scrap, KpuProperties properties, RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.properties = properties;
		this.scrap = scrap;
	}

	public static void main(String[] args) {
		SpringApplication.run(KpuCheckApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			var kode = "/0.json";
			final var wilayahs = restTemplate.getForObject(properties.baseUrl() + properties.wilayahSearchUrl() + kode, Wilayah[].class);
			assert wilayahs != null;

            for (var wilayah : wilayahs) {
                kode = "/" + wilayah.kode();
                final var innerWilayah = restTemplate.getForObject(properties.baseUrl() + properties.wilayahSearchUrl() + kode + ".json", Wilayah[].class);
                assert innerWilayah != null;

                scrap.wilayahTrack(innerWilayah, kode, "");
            }
		};
	}



}
