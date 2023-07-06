package com.example.plugins

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
//import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

@Serializable
data class Matrix(val rows: Int, val columns: Int, val values: List<List<Int>>)

@Serializable
data class MatrixMultiplicationRequest(
    val matrix1: Matrix,
    val matrix2: Matrix
)

@Serializable
data class MatrixMultiplicationResponse(
    val result: Matrix
)






suspend fun postMatrixMultiplicationRequest(matrix1: Matrix, matrix2: Matrix): HttpResponse{
    val client = HttpClient(CIO){
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    } //a client is represented by the http client class
    val request = MatrixMultiplicationRequest(matrix1, matrix2)
//    val requestBody = Matrix(matrix1.rows, matrix2.columns, matrix1.values)
    val response: HttpResponse = client.post("http://localhost:8080/api/matrix-multiply"){
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    println(response.status)
    client.close()

    return response
}

fun generateRandomMatrix(rows: Int, columns: Int): Matrix {
    val values = List(rows) {
        List(columns) {
            Random.nextInt(1, 10) // Generate random values between 1 and 10
        }
    }
    return Matrix(rows, columns, values)
}

suspend fun main() {
    val numOfConcurrentRequests = 10000

    val responses = mutableListOf<Deferred<HttpResponse>>()
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    val job = coroutineScope {
        repeat(numOfConcurrentRequests) {
            val matrix1 = generateRandomMatrix(2, 2)
            println("Matrix 1 is" + matrix1)
            val matrix2 = generateRandomMatrix(2, 2)
            println("Matrix 2 is" + matrix2)
            val response = async { postMatrixMultiplicationRequest(matrix1, matrix2) }
            responses.add(response)
        }
        responses.awaitAll()
    }

    responses.forEach { response ->
        val finalResponse: HttpResponse = response.await()
        val result: MatrixMultiplicationResponse? = finalResponse.body<MatrixMultiplicationResponse?>()
        println(result?.result)
    }
}
