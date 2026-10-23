package com.gestionrh.sirh.config.recrutement;

import jakarta.servlet.ServletContext;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;

/** Garde les anciennes vues du bloc 1 accessibles après le déplacement du préfixe JSP. */
@Configuration
public class VuesRecrutementConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ViewResolver vuesHistoriques(ServletContext servletContext) {
        return (nomVue, locale) -> resoudreVueHistorique(nomVue, locale, servletContext);
    }

    private View resoudreVueHistorique(String nomVue, Locale locale, ServletContext servletContext) {
        if (nomVue == null || nomVue.startsWith("recrutement/")) {
            return null;
        }
        String chemin = "/WEB-INF/jsp/" + nomVue + ".jsp";
        try {
            if (servletContext.getResource(chemin) == null) {
                return null;
            }
            InternalResourceView vue = new InternalResourceView(chemin);
            vue.setServletContext(servletContext);
            return vue;
        } catch (Exception e) {
            return null;
        }
    }
}
