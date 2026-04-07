package com.example.weatherapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {

    private val api: WeatherApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    companion object {
        private val cityTranslations = mapOf(
            // Россия
            "москва" to "Moscow",
            "санкт-петербург" to "Saint Petersburg",
            "питер" to "Saint Petersburg",
            "спб" to "Saint Petersburg",
            "новосибирск" to "Novosibirsk",
            "екатеринбург" to "Yekaterinburg",
            "казань" to "Kazan",
            "нижний новгород" to "Nizhny Novgorod",
            "челябинск" to "Chelyabinsk",
            "самара" to "Samara",
            "омск" to "Omsk",
            "ростов-на-дону" to "Rostov-on-Don",
            "уфа" to "Ufa",
            "красноярск" to "Krasnoyarsk",
            "воронеж" to "Voronezh",
            "пермь" to "Perm",
            "волгоград" to "Volgograd",
            "краснодар" to "Krasnodar",
            "саратов" to "Saratov",
            "тюмень" to "Tyumen",
            "тольятти" to "Tolyatti",
            "ижевск" to "Izhevsk",
            "барнаул" to "Barnaul",
            "иркутск" to "Irkutsk",
            "ульяновск" to "Ulyanovsk",
            "хабаровск" to "Khabarovsk",
            "ярославль" to "Yaroslavl",
            "владивосток" to "Vladivostok",
            "махачкала" to "Makhachkala",
            "томск" to "Tomsk",
            "оренбург" to "Orenburg",
            "кемерово" to "Kemerovo",
            "новокузнецк" to "Novokuznetsk",
            "рязань" to "Ryazan",
            "астрахань" to "Astrakhan",
            "пенза" to "Penza",
            "липецк" to "Lipetsk",
            "тула" to "Tula",
            "киров" to "Kirov",
            "чебоксары" to "Cheboksary",
            "калининград" to "Kaliningrad",
            "брянск" to "Bryansk",
            "курск" to "Kursk",
            "иваново" to "Ivanovo",
            "магнитогорск" to "Magnitogorsk",
            "тверь" to "Tver",
            "сочи" to "Sochi",
            "ставрополь" to "Stavropol",
            "белгород" to "Belgorod",
            "нижний тагил" to "Nizhny Tagil",
            "архангельск" to "Arkhangelsk",
            "владимир" to "Vladimir",
            "смоленск" to "Smolensk",
            "сургут" to "Surgut",
            "чита" to "Chita",
            "волжский" to "Volzhsky",
            "якутск" to "Yakutsk",
            "мурманск" to "Murmansk",
            "петрозаводск" to "Petrozavodsk",
            "кострома" to "Kostroma",
            "нальчик" to "Nalchik",
            "нижневартовск" to "Nizhnevartovsk",
            "череповец" to "Cherepovets",
            "вологда" to "Vologda",
            "орел" to "Oryol",
            "тамбов" to "Tambov",
            "стерлитамак" to "Sterlitamak",
            "грозный" to "Grozny",
            "улан-удэ" to "Ulan-Ude",
            "мытищи" to "Mytishchi",
            "балашиха" to "Balashikha",
            "химки" to "Khimki",
            "подольск" to "Podolsk",
            "королёв" to "Korolev",
            "люберцы" to "Lyubertsy",

            // Украина
            "киев" to "Kyiv",
            "харьков" to "Kharkiv",
            "одесса" to "Odessa",
            "днепр" to "Dnipro",
            "запорожье" to "Zaporizhzhia",
            "львов" to "Lviv",

            // Беларусь
            "минск" to "Minsk",
            "гомель" to "Gomel",
            "витебск" to "Vitebsk",
            "гродно" to "Grodno",
            "брест" to "Brest",

            // Казахстан
            "алматы" to "Almaty",
            "нур-султан" to "Nur-Sultan",
            "астана" to "Astana",
            "шымкент" to "Shymkent",

            "лондон" to "London",
            "париж" to "Paris",
            "берлин" to "Berlin",
            "рим" to "Rome",
            "мадрид" to "Madrid",
            "барселона" to "Barcelona",
            "токио" to "Tokyo",
            "пекин" to "Beijing",
            "шанхай" to "Shanghai",
            "нью-йорк" to "New York",
            "вашингтон" to "Washington",
            "лос-анджелес" to "Los Angeles",
            "чикаго" to "Chicago",
            "торонто" to "Toronto",
            "оттава" to "Ottawa",
            "сидней" to "Sydney",
            "мельбурн" to "Melbourne",
            "канберра" to "Canberra",
            "дубай" to "Dubai",
            "стамбул" to "Istanbul",
            "анкара" to "Ankara",
            "тегеран" to "Tehran",
            "каир" to "Cairo",
            "найроби" to "Nairobi",
            "йоханнесбург" to "Johannesburg",
            "претория" to "Pretoria",
            "лагос" to "Lagos",
            "бангкок" to "Bangkok",
            "сингапур" to "Singapore",
            "сеул" to "Seoul",
            "джакарта" to "Jakarta",
            "мумбаи" to "Mumbai",
            "дели" to "Delhi",
            "нью-дели" to "New Delhi",
            "мехико" to "Mexico City",
            "буэнос-айрес" to "Buenos Aires",
            "сан-паулу" to "Sao Paulo",
            "рио-де-жанейро" to "Rio de Janeiro",
            "бразилиа" to "Brasilia",
            "лима" to "Lima",
            "богота" to "Bogota",
            "сантьяго" to "Santiago",
            "варшава" to "Warsaw",
            "прага" to "Prague",
            "будапешт" to "Budapest",
            "бухарест" to "Bucharest",
            "вена" to "Vienna",
            "амстердам" to "Amsterdam",
            "брюссель" to "Brussels",
            "стокгольм" to "Stockholm",
            "хельсинки" to "Helsinki",
            "копенгаген" to "Copenhagen",
            "осло" to "Oslo",
            "афины" to "Athens",
            "лиссабон" to "Lisbon",
            "цюрих" to "Zurich",
            "женева" to "Geneva"
        )

        private val translitMap = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d",
            'е' to "e", 'ё' to "yo", 'ж' to "zh", 'з' to "z", 'и' to "i",
            'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n",
            'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t",
            'у' to "u", 'ф' to "f", 'х' to "kh", 'ц' to "ts", 'ч' to "ch",
            'ш' to "sh", 'щ' to "sch", 'ъ' to "", 'ы' to "y", 'ь' to "",
            'э' to "e", 'ю' to "yu", 'я' to "ya"
        )

        fun translateCity(input: String): String {
            val lower = input.trim().lowercase()
            cityTranslations[lower]?.let { return it }
            return if (lower.any { it in translitMap }) {
                lower.map { char -> translitMap[char] ?: char.toString() }.joinToString("")
                    .replaceFirstChar { it.uppercase() }
            } else {
                input.trim()
            }
        }
    }

    suspend fun getWeather(city: String): Result<WeatherData> {
        val queryCity = translateCity(city)
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        if (apiKey.isBlank()) {
            return Result.failure(Exception("API key is missing. Set OPEN_WEATHER_API_KEY in local.properties"))
        }
        return try {
            val response = api.getWeatherByCity(queryCity, apiKey)
            Result.success(
                WeatherData(
                    cityName = response.name,
                    country = response.sys.country,
                    temperature = response.main.temp,
                    feelsLike = response.main.feels_like,
                    humidity = response.main.humidity,
                    windSpeed = response.wind.speed,
                    cloudiness = response.clouds.all,
                    description = response.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                    iconCode = response.weather.firstOrNull()?.icon ?: "01d"
                )
            )
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                404 -> Result.failure(Exception("Город не найден: $city"))
                401 -> Result.failure(Exception("Ошибка авторизации API"))
                else -> Result.failure(Exception("Ошибка сети: ${e.code()}"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Нет подключения к интернету"))
        } catch (e: Exception) {
            Result.failure(Exception("Произошла ошибка: ${e.message}"))
        }
    }

    suspend fun getForecast(city: String): Result<ForecastData> {
        val queryCity = translateCity(city)
        val apiKey = BuildConfig.OPEN_WEATHER_API_KEY
        if (apiKey.isBlank()) {
            return Result.failure(Exception("API key is missing. Set OPEN_WEATHER_API_KEY in local.properties"))
        }
        return try {
            val response = api.getForecastByCity(queryCity, apiKey)
            val forecastItems = response.list
                .asSequence()
                .filterIndexed { index, _ -> index % 8 == 0 }
                .take(5)
                .map { item ->
                    ForecastItem(
                        dateTime = item.dt_txt,
                        temperature = item.main.temp,
                        feelsLike = item.main.feels_like,
                        humidity = item.main.humidity,
                        windSpeed = item.wind.speed,
                        cloudiness = item.clouds.all,
                        description = item.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        iconCode = item.weather.firstOrNull()?.icon ?: "01d"
                    )
                }
                .toList()

            Result.success(
                ForecastData(
                    cityName = response.city.name,
                    country = response.city.country,
                    items = forecastItems
                )
            )
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                404 -> Result.failure(Exception("Город не найден: $city"))
                401 -> Result.failure(Exception("Ошибка авторизации API"))
                else -> Result.failure(Exception("Ошибка сети: ${e.code()}"))
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Нет подключения к интернету"))
        } catch (e: Exception) {
            Result.failure(Exception("Произошла ошибка: ${e.message}"))
        }
    }

    suspend fun getWeatherForCitiesSequentially(cities: List<String>): Map<String, WeatherData> {
        val result = linkedMapOf<String, WeatherData>()
        for (city in cities) {
            val weatherResult = getWeather(city)
            weatherResult.onSuccess { data ->
                result[city] = data
            }
        }
        return result
    }

    fun getCapitals(): List<CapitalCity> = listOf(
        CapitalCity("Москва", "Moscow", "RU"),
        CapitalCity("Лондон", "London", "GB"),
        CapitalCity("Париж", "Paris", "FR"),
        CapitalCity("Берлин", "Berlin", "DE"),
        CapitalCity("Рим", "Rome", "IT"),
        CapitalCity("Мадрид", "Madrid", "ES"),
        CapitalCity("Токио", "Tokyo", "JP"),
        CapitalCity("Пекин", "Beijing", "CN"),
        CapitalCity("Вашингтон", "Washington", "US"),
        CapitalCity("Оттава", "Ottawa", "CA"),
        CapitalCity("Канберра", "Canberra", "AU"),
        CapitalCity("Бразилиа", "Brasilia", "BR"),
        CapitalCity("Нью-Дели", "New Delhi", "IN"),
        CapitalCity("Сеул", "Seoul", "KR"),
        CapitalCity("Каир", "Cairo", "EG"),
        CapitalCity("Претория", "Pretoria", "ZA"),
        CapitalCity("Мехико", "Mexico City", "MX"),
        CapitalCity("Буэнос-Айрес", "Buenos Aires", "AR"),
        CapitalCity("Анкара", "Ankara", "TR"),
        CapitalCity("Стокгольм", "Stockholm", "SE")
    )
}
