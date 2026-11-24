# ✅ Регрессионные тесты Hotelio

Этот каталог содержит **автоматические тесты**, проверяющие корректность REST API до и после миграции.  
Они помогают убедиться, что система работает стабильно при любых изменениях архитектуры или кода.

---

## 🧪 validate-before-migration.sh

Скрипт `regress.sh`:

1. Загружает тестовые данные в базу с помощью `init-fixtures.sql`
2. Вызывает все основные REST API
3. Проверяет, что ответы соответствуют ожидаемым
4. Удаляет тестовые данные после завершения

Выводит краткий лог с результатами проверок.

---

## 🧱 init-fixtures.sql

Содержит **фикстурные данные**, которые гарантированно:

- Не пересекаются с прод-данными
- Используют "префиксы" вроде `test-user-*`, `test-hotel-*` и `TESTPROMO*`
- Позволяют воспроизводить и проверять систему независимо от содержимого реальной базы

---

## 🧹 Очистка

В конце скрипта `regress.sh` автоматически вызывается cleanup, который удаляет все данные. (только для использования на тестовой базе!)

---

## 📌 Назначение

Тесты позволяют:

- Проверить, что система ведёт себя одинаково до и после миграции
- Не бояться рефакторинга или замены компонентов
- Создать основу для CI/CD автотестов

---

## 🧪 Пример запуска

```bash
docker build --no-cache -t hotelio-tester .
docker run --rm --add-host=host.docker.internal:host-gateway \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=hotelio \
  -e DB_USER=hotelio \
  -e DB_PASSWORD=hotelio \
  -e DB_BS_PORT=5433 \
  -e DB_BS_NAME=booking \
  -e DB_BS_USER=booking \
  -e DB_BS_PASSWORD=booking \
  -e DB_BHS_PORT=5434 \
  -e DB_BHS_NAME=hoteliostat \
  -e DB_BHS_USER=hoteliostat \
  -e DB_BHS_PASSWORD=hoteliostat \
  -e API_URL=http://host.docker.internal:8084 \
  hotelio-tester
```

