package ru.yamost.first.agent.core.domain

sealed interface YaResult<Data, Error> {
    class Success<Data, Error>(val data: Data) : YaResult<Data, Error>
    class Failure<Data, Error>(val error: Error) : YaResult<Data, Error>
}