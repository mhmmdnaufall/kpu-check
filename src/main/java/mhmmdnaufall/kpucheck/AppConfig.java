package mhmmdnaufall.kpucheck;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AppConfig {

    private final KpuProperties properties;

    AppConfig(KpuProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public PrintStream printStream() throws IOException {
        final var resultPath = Path.of(properties.resultPath());
        return new PrintStream(Files.newOutputStream(resultPath));
    }

    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}
