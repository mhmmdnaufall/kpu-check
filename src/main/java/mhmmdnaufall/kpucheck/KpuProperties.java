package mhmmdnaufall.kpucheck;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("kpu")
public record KpuProperties(String baseUrl, String wilayahSearchUrl, String tpsScoreUrl, String resultPath) { }
