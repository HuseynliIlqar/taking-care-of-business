# API Documentation — Travel Booking System

Bu sənəd bütün endpoint-ləri, düzgün input dəyərlərini və nümunə cavabları əhatə edir.

> Swagger UI ilə interaktiv sınaq üçün:
> - Flight Service: http://localhost:8081/swagger-ui/
> - Hotel Service:  http://localhost:8082/swagger-ui/
> - Car Rental:     http://localhost:8083/swagger-ui/
>
> Bütün endpoint-lər API Gateway üzərindən də əlçatandır: http://localhost:8080

---

## Flight Service

### `GET /flights`
Bütün mövcud uçuşları qaytarır.

```bash
curl http://localhost:8080/flights
```

---

### `GET /flights/search`
Mənşə və təyinat koduna görə uçuş axtarır.

| Parametr | Tip | Məcburi | Açıqlama |
|----------|-----|---------|---------|
| `origin` | String | ✅ | Çıxış aeroportu/şəhər kodu |
| `destination` | String | ✅ | Varış aeroportu/şəhər kodu |

**Mövcud marşrutlar:**

| origin | destination |
|--------|-------------|
| `NYC` | `LAX` |
| `LAX` | `NYC` |
| `SFO` | `ORD` |
| `ORD` | `SFO` |
| `BOS` | `MIA` |
| `MIA` | `BOS` |
| `NYC` | `CHI` |

> ⚠️ Yalnız yuxarıdakı marşrutlar üçün nəticə qayıdır (mock data rejimində).
> API key konfiqurasiya edilibsə, istənilən IATA kodu işlənir.

**Nümunə:**
```bash
curl "http://localhost:8080/flights/search?origin=NYC&destination=LAX"
```

**Nümunə cavab:**
```json
[
  {
    "id": "FL001",
    "airline": "Delta Airlines",
    "origin": "NYC",
    "destination": "LAX",
    "departureTime": "2024-06-01T08:00:00",
    "arrivalTime": "2024-06-01T11:30:00",
    "flightNumber": "DL101",
    "price": 299.99,
    "availableSeats": 45
  }
]
```

**Xəta halları:**

| Vəziyyət | HTTP Kodu | Səbəb |
|----------|-----------|-------|
| Nəticə tapılmadı | `404 Not Found` | Həmin marşrut mövcud deyil |
| Parametr çatışmır | `400 Bad Request` | `origin` və ya `destination` göndərilməyib |

---

## Hotel Service

### `GET /hotels`
Bütün mövcud otelləri qaytarır.

```bash
curl http://localhost:8080/hotels
```

---

### `GET /hotels/search`
Şəhər adına görə otel axtarır.

| Parametr | Tip | Məcburi | Açıqlama |
|----------|-----|---------|---------|
| `location` | String | ✅ | Şəhər adı |

**Mövcud şəhərlər:**

| Şəhər | Data mənbəyi | Qeyd |
|-------|-------------|------|
| `LosAngeles` | Mock data | |
| `NewYork` | Mock data | |
| `Miami` | Mock data | |
| `Chicago` | Mock data | |
| `SanFrancisco` | Mock data | |
| `Boston` | Mock data | |

> ⚠️ **Şəhər adları case-insensitive-dir** — `losangeles`, `LosAngeles`, `LOSANGELES` hamısı eyni nəticəni verir.
> Boşluq istifadə etmə: `Los Angeles` deyil, `LosAngeles` yaz.

**Nümunə:**
```bash
curl "http://localhost:8080/hotels/search?location=LosAngeles"
curl "http://localhost:8080/hotels/search?location=NewYork"
```

**Nümunə cavab:**
```json
[
  {
    "id": "H001",
    "name": "The Grand Plaza",
    "city": "LosAngeles",
    "address": "123 Sunset Blvd, Los Angeles, CA 90028",
    "rating": 4.5,
    "pricePerNight": 289.99,
    "availableRooms": 15,
    "amenities": "Pool, Spa, Gym, Restaurant"
  }
]
```

**Xəta halları:**

| Vəziyyət | HTTP Kodu | Səbəb |
|----------|-----------|-------|
| Nəticə tapılmadı | `404 Not Found` | Həmin şəhər mövcud deyil — yuxarıdakı siyahıdan istifadə et |
| Parametr çatışmır | `400 Bad Request` | `location` göndərilməyib |

---

## Car Rental Service

### `GET /cars`
Bütün mövcud kirayə avtomobilləri qaytarır.

```bash
curl http://localhost:8080/cars
```

---

### `GET /cars/search`
Lokasiya və/və ya növə görə kirayə avtomobil axtarır. Hər iki parametr isteğe bağlıdır.

| Parametr | Tip | Məcburi | Açıqlama |
|----------|-----|---------|---------|
| `location` | String | ❌ | Şəhər adı |
| `type` | String | ❌ | Avtomobil növü |

**Mövcud lokasiyalar:**

| Şəhər | Data mənbəyi |
|-------|-------------|
| `LosAngeles` | Mock data |
| `NewYork` | Mock data |
| `Miami` | Mock data |
| `Chicago` | Mock data |
| `SanFrancisco` | Mock data |
| `Boston` | Mock data |
| `Prague` | **Booking.com API (canlı data)** |
| `London` | **Booking.com API (canlı data)** |
| `Paris` | **Booking.com API (canlı data)** |
| `Berlin` | **Booking.com API (canlı data)** |
| `Rome` | **Booking.com API (canlı data)** |
| `Amsterdam` | **Booking.com API (canlı data)** |

**Mövcud avtomobil növləri:**
`Economy` · `Compact` · `Sedan` · `SUV` · `Luxury` · `Electric`

**Nümunə sorğular:**
```bash
# Lokasiyaya görə (US — mock data)
curl "http://localhost:8080/cars/search?location=LosAngeles"

# Lokasiyaya görə (Avropa — canlı API data)
curl "http://localhost:8080/cars/search?location=Prague"

# Növə görə
curl "http://localhost:8080/cars/search?type=SUV"

# Hər ikisi birlikdə
curl "http://localhost:8080/cars/search?location=LosAngeles&type=SUV"

# Parametrsiz — bütün mövcud avtomobillər
curl "http://localhost:8080/cars/search"
```

**Nümunə cavab:**
```json
[
  {
    "id": "C001",
    "make": "Toyota",
    "model": "Camry",
    "type": "Sedan",
    "location": "LosAngeles",
    "pricePerDay": 59.99,
    "available": true,
    "seats": 5
  }
]
```

**Xəta halları:**

| Vəziyyət | HTTP Kodu | Səbəb |
|----------|-----------|-------|
| Nəticə tapılmadı | `404 Not Found` | Həmin lokasiya/növ üçün mövcud avtomobil yoxdur |

---

## Tez Sınaq Qaydası (API Key olmadan)

Layihəni clone edib API key konfiqurasiya etmədən belə sınaya bilərsən — bütün servisler mock data ilə işləyir:

```bash
# 1. Discovery Service işə sal
cd discovery-service && mvn spring-boot:run

# 2. Eyni anda hər servisi ayrı terminaldə işə sal
cd flight-service     && mvn spring-boot:run
cd hotel-service      && mvn spring-boot:run
cd car-rental-service && mvn spring-boot:run

# 3. API Gateway işə sal
cd api-gateway && mvn spring-boot:run

# 4. Test et
curl "http://localhost:8080/flights/search?origin=NYC&destination=LAX"
curl "http://localhost:8080/hotels/search?location=NewYork"
curl "http://localhost:8080/cars/search?location=LosAngeles&type=SUV"
```

## Canlı API Data Üçün

`travel-booking/.env` faylını yarat:
```
RAPIDAPI_KEY=your_key_here
RAPIDAPI_HOST=booking-com.p.rapidapi.com
```
RapidAPI-dən pulsuz key al: [rapidapi.com](https://rapidapi.com) → "Booking com" axtar → Basic plan.

> Limit: **530 request/ay**. Hər hotel axtarışı **2 request** sərf edir.
