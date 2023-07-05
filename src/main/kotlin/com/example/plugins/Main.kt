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
//import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

suspend fun main(){
    val matrix1 = Matrix(2, 2, listOf(listOf(1, 2), listOf(3, 4)))
    val matrix2 = Matrix(2, 2, listOf(listOf(1, 2), listOf(3, 4)))
    val response = postMatrixMultiplicationRequest(matrix1, matrix2)
//    val json = response.
    val finalResponse: MatrixMultiplicationResponse = response.body()

    println(finalResponse)
}