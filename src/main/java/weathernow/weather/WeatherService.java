package weathernow.weather;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class WeatherService {
    private final String API_KEY = "056de0774b33cb4f7c0b80ec3749f8c8";
    private final RestTemplate restTemplate;
    private final WeatherDataRepository weatherDataRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Autowired
    public WeatherService(RestTemplate restTemplate, WeatherDataRepository weatherDataRepository) {
        this.restTemplate = restTemplate;
        this.weatherDataRepository = weatherDataRepository;
    }

    // Кэширование
    // (Нужно доработать, не уверен что оно ваще работает, но раз usage есть - почему бы нет)
    public void updateWeatherData(Long id, WeatherData updatedData) {
        logger.info("Updating weather data for ID: {}", id);
        // Проверяем, существует ли запись в базе данных
        WeatherData existingData = weatherDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Weather data not found for ID: " + id));

        // Обновляем данные
        existingData.setCity(updatedData.getCity());
        existingData.setTemperature(updatedData.getTemperature());
        existingData.setDescription(updatedData.getDescription());

        // Сохраняем изменения
        weatherDataRepository.save(existingData);
        logger.info("Successfully updated weather data for ID: {}", id);
    }

    // Тело программы
    public WeatherData fetchWeatherData(String city) {
        logger.info("Fetching weather data for city: {}", city);
        // Сначала пытаемся получить данные из базы данных
        WeatherData existingWeatherData = weatherDataRepository.findByCity(city).orElse(null);
        if (existingWeatherData != null) {
            logger.info("Weather data found in the database for city: {}", city);
            return existingWeatherData; // Возвращаем данные из базы, если они уже существуют
        }

        // Если данных нет в базе, делаем запрос к API
        String url = UriComponentsBuilder.fromHttpUrl("http://api.openweathermap.org/data/2.5/weather")
                .queryParam("q", city)
                .queryParam("appid", API_KEY)
                .queryParam("units", "metric")
                .toUriString();

        try {
            logger.debug("Sending request to Weather API: {}", url);
            String result = restTemplate.getForObject(url, String.class);

            // Проверяем, успешен ли запрос
            if (result == null || result.isEmpty()) {
                logger.warn("Empty response from Weather API for city: {}", city);
                throw new RuntimeException("Empty response from weather API for " + city);
            }

            // Обрабатываем ответ
            JSONObject json = new JSONObject(result);
            String cityName = json.getString("name");
            double temperature = json.getJSONObject("main").getDouble("temp");
            String weatherDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");

            WeatherData weatherData = new WeatherData();
            weatherData.setCity(cityName);
            weatherData.setTemperature(temperature);
            weatherData.setDescription(weatherDescription);

            logger.info("Successfully fetched weather data for city: {}", city);
            return weatherDataRepository.save(weatherData);
        } catch (RestClientException e) {
            logger.error("Error fetching weather data for city: {}: {}", city, e.getMessage(), e);
            throw new RuntimeException("Error fetching weather data for " + city + ": " + e.getMessage());
        }
    }




    // Запросы //

    // Сохранение (PUT-запрос) города
    public void saveWeatherData(WeatherData newData) {
        logger.info("Saving weather data for city: {}", newData.getCity());
        weatherDataRepository.save(newData);
        logger.info("Successfully saved weather data for city: {}", newData.getCity());
    }

    // Удаление (Delete-запрос) города
    public void deleteWeatherData(WeatherData weatherData) {
        logger.info("Deleting weather data for city: {}", weatherData.getCity());
        weatherDataRepository.delete(weatherData);
        logger.info("Successfully deleted weather data for city: {}", weatherData.getCity());
    }

    // Поиск по названию города (для проверки существования)
    public Optional<WeatherData> getWeatherDataOptional(String city) {
        logger.info("Looking up weather data for city: {}", city);
        return weatherDataRepository.findByCity(city);
    }

    // GET-ALL запрос
    public List<WeatherData> getAllWeatherData() {
        logger.info("Retrieving all weather data from the database");
        return weatherDataRepository.findAll();
    }
    // Get-запрос
    public WeatherData findWeatherInDatabase(String city) {
        return weatherDataRepository.findByCity(city).orElse(null);
    }


    // TRASH //

    public WeatherData getWeatherData(String city) {
        return weatherDataRepository.findByCity(city).orElseGet(() -> fetchWeatherData(city));
    }

}
