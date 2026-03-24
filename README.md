# Демо проект для проверки spqr

## Структура проекта
- files 
  - db_scr - таблицы, необходимые для работы
  - spqr - конфигурация роутера и дистрибьюшенов
- proto-models - модуль с основными моделями
- proto-rest-api - модуль с основной логикой
  - src/main/java/org/example/spqr/config - классы конфигурации (в т.ч. SqlConfig - настройка подключения к БД)
  - src/main/java/org/example/spqr/rest - классы для обработки rest запросов
  - src/main/java/org/example/spqr/sql - классы для работы с БД
  - src/main/java/org/example/spqr/WebApp - Main Class для запуска приложения

## Основные запросы

- POST /entities - добавление новой сущности. Если preconditions не пустые, но по этим сущностям создаются подписки в таблице SUBSCRIPTIONS.

```
    {
        "entityId": "ent-2",
        "scenarioName": "scenario1",
        "preconditions": ["ent-1"] // необязательный параметр
    }
```

- POST /entities/bulk - добавление пачки сущностей

```
    [{
        "entityId": "ent-3",
        "scenarioName": "scenario2",
        "preconditions": ["ent-1"]
    },
    {
        "entityId": "ent-4",
        "scenarioName": "scenario3"
    }]
```

- GET /entities?limit=2&offset=0 - поиск сущностей с пагинацией

- PUT /suspensions - перевод сущностей в статус suspension с генерацией запроса в таблице REQUESTS

```
    {
        "entityIds": ["ext-5", "ext-1"]
    }
```

- POST /interactions/responses/bulk - регистрация пачки ответов по запросам сущностей в статусе suspension

```
    [
        {
            "requestId": "c8865e1c-df3d-4638-a185-81262e5bd6f9",
            "body": "somebody11"
        }
    ]
```

- POST /interactions/events/bulk - регистрация пачки событий по заданным сущностям

```
    {
        "ext-1": {
            "eventName": "name4"
        },
        "ext-2": {
            "eventName": "name4"
        }
    }
```

- GET /interactions/events - поиск последних зарегистрированных событий по заданным сущностям

```
    {
        "entityIds": ["ext-5", "ext-1"]
    }
```

- POST /activations/suspended?maxCount={maxCount} - активация сущностей в статусе suspension, у которых истекло время ожидания ответа (не больше maxCount)

- POST /activations/conditional?entityId={entityId} - активация сущностей, у которых есть подписки на сущность с идентификатором entityId