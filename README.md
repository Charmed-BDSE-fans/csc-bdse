# csc-bdse
Базовый проект для практики по курсу "Программная инженерия больших данных".

## О команде

Здесь находятся решения команды *Charmed BDSE fans* в составе:
- Лапшин Дмитрий (@LDVSOFT),
- Валин Глеб (@the7winds),
- Третьякова Елизаветта (@LizaTretyakova).

## Задания
- [Подготовка](INSTALL.md)
- [Часто задаваемые вопросы](FAQ.md)
- [Задание 1](TASK1.md)
- [Задание 2](TASK2.md)

## Структура решения задания №1
- Структура приложения несколько разнесена, чтобы можно было запускать Spring Boot приложение
  с выбранной реализацией хранилища с помощью профилей. В данный момент профилей два:
  - `in-memory`: хранилище в памяти из предоставленного кода.
  - `postgres`: хранилище на основе СУБД PostgreSQL, связка с которым осуществляется на
    основе Spring Data и JPA.

- Несмотря на название `PostgresKeyValueApi`, возможно расширение
  реализации на любую другую базу, с которой можно связать JPA: в решении
  не используется SQL.
- Сама нода пакуется в контейнер `charmed-bdse-fans/bdse-node`.
- В следствие реализации выше, приложение ожидает, что необходимая СУБД будет уже запущена
  к моменту запуска приложения. Для запуска всего приложения потребуется, например, Docker Compose
  (см. `in-memory-docker-compose.yaml` и `postgres-docker-compose.yaml`).
- В тестах применяются следующие подходы:
  - запуск базы данных с помощью `@ClassRule`, с последующим локальным поднятием `SpringRunner`ом
    всего необходимого контекста приложения, подключающегося к этой базе;
  - запуск приложения и его базы данных в отдельных контейнерах, само тестирование
    идёт через REST API (`KeyValueApiHttpClient`).

  В силу такой архитектуры, команд выше хватает для корректного запуска всех тестов.

### Проблемы

- Мы реализовали API про включение-выключение ноды заглушечно, поскольку по нашей архитектуре
  приложение не управляет базой данных. Мы считаем, что оно возымеет смысл при реализации кластерного взаимодействия.
- Возникла проблема реализации теста персистентности хранения, поскольку для этого нужно запустить приложение,
  убить и перезапустить на тех же данных. Для этого достаточно при создании БД указать подключаемый носитель (volume),
  но выставляемый библиотекой testcontainers API несколько скуден, и мы вместе (по крайней мере, на данный момент) не
  смогли через него надёжно выразить желаемые операции.
