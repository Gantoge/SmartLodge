# Booking System API Documentation

## Компенсация

### Эндпойнты

#### Бронирования
- **GET** /bookings/suggestions - Подсказки по номерам
- **GET** /bookings/all - Все бронирования (для администратора)

#### Пользователи (Booking, admin)
| Метод | Эндпойнт                 | Описание                     |
|-------|--------------------------|------------------------------|
| GET   | /admin/users          | Список пользователей         |
| GET   | /admin/users/{id}     | Просмотр пользователя        |
| PUT   | /admin/users/{id}     | Обновление пользователя      |
| DELETE| /admin/users/{id}     | Удаление пользователя        |

#### Отели и номера (Hotel)
| Метод                             | Эндпойнт                       | Описание                        |
|-----------------------------------|--------------------------------|---------------------------------|
| GET                               | /hotels                      | Просмотр отелей                |
| GET                               | /hotels/{id}                | Просмотр отеля                 |
| POST/PUT/DELETE                   | /hotels                      | CRUD для администратора        |
| POST/PUT/DELETE                   | /hotels/{id}                | CRUD для администратора        |
| GET                               | /rooms/{id}                 | Просмотр номера                |
| POST/PUT/DELETE                   | /rooms                       | CRUD для администратора        |
| POST                               | /rooms/{id}/hold            | Удержание номера (по requestId)|
| POST                               | /rooms/{id}/confirm         | Подтверждение удержания        |
| POST                               | /rooms/{id}/release         | Освобождение номера (компенсация)|

#### Статистика
- **GET** /stats/rooms/popular - Статистика популярности номеров

## Согласованность и надёжность
- Локальные транзакции внутри сервисов (@Transactional)
- Двухшаговая логика бронирования:
  - PENDING → (hold в Hotel) → CONFIRM → CONFIRMED
  - При сбое: RELEASE + CANCELLED
- Идемпотентность по requestId (повтор не создаёт дубликатов)
- Повторы с экспоненциальной задержкой при ошибках WebClient
- Логирование по X-Correlation-Id

## Консоль H2
Доступна в Hotel Service по адресу:
- ➡️ /h2-console

Параметры подключения указаны в application.yml соответствующего сервиса.

## Swagger / OpenAPI
Сервисы Swagger UI:
- Booking Service: [http://localhost:<booking-port>/swagger-ui.html](http://localhost:<booking-port>/swagger-ui.html)
- Hotel Service: [http://localhost:<hotel-port>/swagger-ui.html](http://localhost:<hotel-port>/swagger-ui.html)
- Gateway: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Тестирование
### Unit-тесты:
- HotelAvailabilityTests — проверка идемпотентности (hold / confirm / release)

### Интеграционные тесты:
- **Booking Service** (WebTestClient + WireMock):
  - BookingHttpIT#createBooking_Http_Success — успешное бронирование (CONFIRMED)
  
- **Hotel Service** (MockMvc):
  - HotelHttpIT#adminCanCreateHotel — проверка CRUD
  - HotelAvailabilityTests, HotelMoreTests — статистика и занятость

### Запуск всех тестов:
bash
mvn -q -DskipTests=false test

## Ограничения и дальнейшее развитие
- Упрощённые модели и проверки (учебный пример)
- Для высокой конкурентности — добавить блокировки / версионирование в БД
- Возможна интеграция с внешними провайдерами идентификации (Keycloak, OAuth2)
- Усиление отказоустойчивости:
  - Circuit Breaker (Resilience4j)
  
- Централизованное логирование и трассировка
