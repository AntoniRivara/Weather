package weathernow.weather;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    Optional<WeatherData> findByCity(String city);
}
