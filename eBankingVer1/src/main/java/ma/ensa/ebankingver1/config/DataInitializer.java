package ma.ensa.ebankingver1.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;

import javax.sql.DataSource;

@Configuration
public class DataInitializer {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("data.sql"));
        populator.setContinueOnError(true); // ou false selon ton besoin
        DatabasePopulatorUtils.execute(populator, dataSource);
        System.out.println("✅ Script data.sql exécuté.");
    }
}

