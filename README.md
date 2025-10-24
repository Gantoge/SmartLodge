# 🏨 MIPHI Microservices Demo — Система бронирования отелей

**MIPHI Microservices Demo** — демонстрационный многомодульный проект распределённого приложения на **Spring Boot / Spring Cloud**, реализующий систему бронирований отелей.

---

## ⚙️ Архитектура проекта

| Сервис | Назначение | Порт |
|--------|-------------|------|
| **eureka-server** | Реестр сервисов | `8761` |
| **api-gateway** | API-шлюз (маршрутизация и аутентификация) | `8080` |
| **hotel-service** | Управление отелями и номерами | случайный (`0`), регистрируется в Eureka |
| **booking-service** | Управление бронированиями и пользователями | случайный (`0`), регистрируется в Eureka |

Все сервисы используют встроенную базу данных **H2**.  
Взаимодействие между ними осуществляется через локальные транзакции (без глобальных распределённых).

---

## 🚀 Возможности

- 🔐 Регистрация и авторизация пользователей (JWT, через `Booking Service`)
- 🏷️ Создание бронирований с двухшаговой согласованностью  
  *(PENDING → CONFIRMED/CANCELLED, с компенсацией при сбое)*
- ♻️ Идемпотентность запросов (`requestId`)
- ⏱️ Повторные вызовы с экспоненциальной паузой и таймаутами
- 💡 Подсказки по выбору номера (сортировка по `timesBooked`, затем по `id`)
- 🧑‍💼 Администрирование пользователей, отелей и номеров (CRUD)
- 📊 Агрегаты по популярности номеров (`timesBooked`)
- 🔗 Сквозная корреляция запросов через `X-Correlation-Id`

---

## 🧩 Требования

- **Java 17+**
- **Maven 3.9+**

---

## 🏗️ Сборка и запуск

1. **Запустить Eureka Server**
   ```bash
   mvn -pl eureka-server spring-boot:run
Запустить API Gateway

bash
Копировать код
mvn -pl api-gateway spring-boot:run
Запустить Hotel и Booking Service (в отдельных терминалах)

bash
Копировать код
mvn -pl hotel-service spring-boot:run
mvn -pl booking-service spring-boot:run
После запуска все сервисы будут зарегистрированы в Eureka:
➡️ http://localhost:8761

# 🔑 Конфигурация JWT

Для демонстрации используется **симметричный ключ HMAC**.  
Секрет задаётся свойством `security.jwt.secret`  
(по умолчанию — `dev-secret-please-change`) в файлах:

- `api-gateway/src/main/resources/application.yml`
- `hotel-service/src/main/resources/application.yml`
- `booking-service/src/main/resources/application.yml`

> 💡 **Рекомендация для продакшна:**  
> заменить секрет и/или использовать **OAuth2 / Keycloak** для централизованной авторизации.

---

## ⚡ Быстрый сценарий (через Gateway: порт `8080`)

### 1. Регистрация пользователя
```bash
curl -X POST http://localhost:8080/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass"}'
  2. Вход и получение JWT
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"user1","password":"pass"}' | jq -r .access_token)

3. Создание отеля и номера (для администратора)
curl -X POST http://localhost:8080/hotels \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"name":"Hotel A","city":"Moscow","address":"Red Square, 1"}'

curl -X POST http://localhost:8080/rooms \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"number":"101","capacity":2,"available":true}'

4. Получение подсказок по номерам
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/bookings/suggestions

5. Создание бронирования (с requestId)
curl -X POST http://localhost:8080/bookings \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"roomId":1,"startDate":"2025-10-20","endDate":"2025-10-22","requestId":"req-123"}'

6. Просмотр истории бронирований
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/bookings

🧭 Основные эндпойнты (через Gateway)
🔐 Аутентификация (Booking)
Метод	Эндпойнт	Описание
POST	/auth/register	Регистрация пользователя (для админа — "admin": true)
POST	/auth/login	Получение JWT-токена
📅 Бронирования (Booking)
Метод	Эндпойнт	Описание
GET	/bookings	Мои бронирования
POST	/bookings	Создание бронирования (PENDING → CONFIRMED / компенсация)
GET	/bookings/suggestions	Подсказки по номерам
GET	/bookings/all	Все бронирования (для администратора)
👥 Пользователи (Booking, admin)
GET /admin/users
GET /admin/users/{id}
PUT /admin/users/{id}
DELETE /admin/users/{id}

🏨 Отели и номера (Hotel)
Метод	Эндпойнт	Описание
GET	/hotels, /hotels/{id}	Просмотр отелей
POST/PUT/DELETE	/hotels, /hotels/{id}	CRUD для администратора
GET	/rooms/{id}	Просмотр номера
POST/PUT/DELETE	/rooms	CRUD для администратора
POST	/rooms/{id}/hold	Удержание номера (по requestId)
POST	/rooms/{id}/confirm	Подтверждение удержания
POST	/rooms/{id}/release	Освобождение номера (компенсация)
📊 Статистика
GET /stats/rooms/popular — статистика популярности номеров

🔁 Согласованность и надёжность

Локальные транзакции внутри сервисов (@Transactional)

Двухшаговая логика бронирования:

PENDING → (hold в Hotel) → CONFIRM → CONFIRMED
при сбое → RELEASE + CANCELLED


Идемпотентность по requestId (повтор не создаёт дубликатов)

Повторы с экспоненциальной задержкой при ошибках WebClient

Логирование по X-Correlation-Id

🗄️ Консоль H2

Доступна в Hotel Service по адресу:
➡️ /h2-console

Параметры подключения указаны в application.yml соответствующего сервиса.

📘 Swagger / OpenAPI
Сервис	Swagger UI
Booking Service	http://localhost:<booking-port>/swagger-ui.html

Hotel Service	http://localhost:<hotel-port>/swagger-ui.html

Gateway (агрегатор)	http://localhost:8080/swagger-ui.html
🧪 Тестирование

Unit-тесты:

HotelAvailabilityTests — проверка идемпотентности (hold / confirm / release)

Интеграционные тесты:

Booking Service (WebTestClient + WireMock):
BookingHttpIT#createBooking_Http_Success — успешное бронирование (CONFIRMED)

Hotel Service (MockMvc):
HotelHttpIT#adminCanCreateHotel — проверка CRUD
HotelAvailabilityTests, HotelMoreTests — статистика и занятость

Запуск всех тестов:

mvn -q -DskipTests=false test

🚧 Ограничения и дальнейшее развитие

Упрощённые модели и проверки (учебный пример)

Для высокой конкурентности — добавить блокировки / версионирование в БД

Возможна интеграция с внешними провайдерами идентификации (Keycloak, OAuth2)

Усиление отказоустойчивости:

Circuit Breaker (Resilience4j)

Централизованное логирование и трассировка
