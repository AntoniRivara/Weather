package weathernow.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class WeatherController {
    private final WeatherService weatherService;
    private static final Logger logger = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/weather")
    public String getWeather() {
        logger.info("Получен запрос на получение всех погодных данных.");
        // Получаем данные из базы
        List<WeatherData> weatherDataList = weatherService.getAllWeatherData();
        StringBuilder response = new StringBuilder();

        // Добавляем данные из базы в ответ
        for (WeatherData weatherData : weatherDataList) {
            response.append("Погода в ")
                    .append(weatherData.getCity())
                    .append(": ")
                    .append(weatherData.getTemperature())
                    .append("°C, ")
                    .append(weatherData.getDescription())
                    .append("\n");
        }

        return response.toString();
    }

    @GetMapping("/weather/{city}")
    public WeatherData getWeatherForCity(@PathVariable String city) {
        WeatherData weatherData = weatherService.getWeatherData(city); // Теперь вызываем метод getWeatherData
        if (weatherData == null) {
            return weatherService.fetchWeatherData(city); // Получаем данные из API, если их нет в базе
        }
        return weatherData;
    }

    @PutMapping("/weather/{id}")
    public String updateWeather(@PathVariable Long id, @RequestBody WeatherData updatedData) {
        // Вызываем метод обновления в WeatherService
        weatherService.updateWeatherData(id, updatedData);

        // Возвращаем строку, сообщающую об успешном обновлении
        return "WeatherData для " + id + " успешно обновлена!";
    }

    // Добавление вручную
    @PostMapping("/weather")
    public String addWeather(@RequestBody WeatherData newData) {
        weatherService.saveWeatherData(newData);
        return "WeatherData для " + newData.getCity() + " успешно добавлена";
    }

    // Добавление через название города
    // Добавление через название города
    @PostMapping("/weather/{city}")
    public String addWeatherData(@PathVariable String city) {
        // Проверяем существование данных в базе
        WeatherData existingData = weatherService.findWeatherInDatabase(city);
        if (existingData != null) {
            return "WeatherData для " + city + " уже существует";
        }

        // Если данных нет, вызываем fetchWeatherData для добавления
        WeatherData savedData = weatherService.fetchWeatherData(city);
        return "WeatherData для " + savedData.getCity() + " успешно добавлена";
    }

//    @DeleteMapping("/weather/{id}")
//    public String deleteWeather(@PathVariable Long id) {
//        weatherService.deleteWeatherData(id);
//        return "Weather data deleted successfully!";
//    }

    @DeleteMapping("/weather/{city}")
    public String deleteWeatherByCity(@PathVariable String city) {
        // Проверяем, существует ли запись в базе данных
        Optional<WeatherData> existingData = weatherService.getWeatherDataOptional(city);

        if (existingData.isPresent()) {
            // Удаляем запись
            weatherService.deleteWeatherData(existingData.get());
            return "WeatherData для " + city + " успешно удалена!";
        } else {
            return "WeatherData для " + city + " не найдена";
        }
    }
}
