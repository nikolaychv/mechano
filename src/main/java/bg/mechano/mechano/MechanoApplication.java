package bg.mechano.mechano;

import bg.mechano.mechano.config.MediaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MediaProperties.class)
public class MechanoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MechanoApplication.class, args);
	}
}
