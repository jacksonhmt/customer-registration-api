package br.com.loja.config;

import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * Resolves the request language from the Accept-Language header. The MessageSource itself is configured through
 * the spring.messages.* properties (Spring Boot auto-configuration).
 */
@Configuration
public class InternationalizationConfig {

    public static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    public static final Locale EN_US = Locale.forLanguageTag("en-US");

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(PT_BR);
        resolver.setSupportedLocales(List.of(PT_BR, EN_US));
        return resolver;
    }
}
