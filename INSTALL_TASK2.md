# Что нужно для старта разработки:
- Необходимо установить Java SE Development Kit 8 <http://www.oracle.com/technetwork/java/javase/downloads/2133151>
- Для первоначальной сборки проекта необходим доступ до maven-репозиториев зависимостей (может быть неожиданно много,
 но скачиваются один раз)
- Необходимо установить Docker CE <https://www.docker.com/community-edition>
- Для первоначальной сборки контейнеров интеграционных тестов необходим доступ до Docker HUB (`openjdk:8-jdk-alpine`)
- На Linux необходимо добавить текущего пользователя в группу `docker` <https://docs.docker.com/install/linux/linux-postinstall/>
- Рекомендуется использовать IntelliJ IDEA <https://www.jetbrains.com/idea/download/>
- Не рекомендуется использовать Windows <https://www.testcontainers.org/usage/windows_support.html>

# Структура модулей проекта
- `bdse-app` содержит код реализуемого бизнес-приложения
- `bdse-kvnode` содержит код Persistent Storage Unit
- `bdse-integration-tests` содержит утилиты и тесты для интеграционного тестирования

# Сборка и запуск интеграционных тестов
```
./mvnw --projects bdse-kvnode clean package # В том числе соберёт контейнер
./mvnw --projects bdse-app clean package
./mvnw --projects bdse-integration-tests --also-make test
```
