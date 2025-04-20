![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Обмен сообщений реализован на `Kafka`.

# Инструкция по запуску

Перед запуском проекта запустите docker-compose.yml.
Для запуска проекта нужно запустить отдельно bot и scrapper через файлы BotApplication и
ScrapperApplication. При запуске в качестве переменных окружения указывать токены Телеграма, Github-а,
ключ SO, POSTGRES_USER, POSTGRES_PASSWORD, POSTGRES_DB.
Bot и scrapper можно запустить командой `mvn spring-boot:run` из директорий backend.academy.bot и
backend.academy.scrapper. При этом в той же строке указывать переменные окружения и их значения через "=".
После запуска модулей можно пользоваться проектом - писать телеграм-боту и получать от него ответ.
Принцип работы с ботом - регистрация, добавление ссылок для отслеживания. Бот будет присылать уведомления при
появлении обновлений по ссылкам.
