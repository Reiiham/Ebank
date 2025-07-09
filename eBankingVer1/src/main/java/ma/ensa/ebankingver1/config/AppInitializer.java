package ma.ensa.ebankingver1.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // Root configuration classes (for services, security, etc.)
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{DataConfig.class, SecurityConfig.class};
    }

    // Servlet configuration classes (for MVC, controllers, etc.)
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class}; // Load WebConfig here
    }

    // DispatcherServlet mapping
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}