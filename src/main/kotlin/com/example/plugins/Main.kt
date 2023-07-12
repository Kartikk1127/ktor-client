import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jfree.data.category.DefaultCategoryDataset
import java.time.LocalDateTime
import kotlin.random.Random

@Serializable
data class Matrix(val rows: Int, val cols: Int, val values: List<List<Int>>)

@Serializable
data class MatrixMultiplicationRequest(
    val matrix1: Matrix,
    val matrix2: Matrix
)

suspend fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
    val responseList = mutableListOf<Deferred<HttpResponse>>()
    val successfullRequestList = mutableListOf<HttpResponse>()
    val latencyList = mutableListOf<Long>()

    val totalTime = 60
    val totalRequests = 180000
    val requestsPerSecond = totalRequests / totalTime

    var requestCount = 0
    val startTime = System.currentTimeMillis()
    val endTime = startTime + totalTime * 1000

    while (System.currentTimeMillis() < endTime && requestCount < totalRequests) {
        coroutineScope {
            val remainingRequests = totalRequests - requestCount
            val requestsToSend = minOf(requestsPerSecond, remainingRequests)

            repeat(requestsToSend) {
                val matrix1 = generateRandomMatrix(2, 2)
                val matrix2 = generateRandomMatrix(2, 2)
                val response = async {
                    postMatrixMultiplication(client, matrix1, matrix2, latencyList, successfullRequestList)
                }
                responseList.add(response)
                requestCount++
            }
            delay(1)
        }
    }

    responseList.awaitAll()

    println("Total requests sent: $requestCount")
    println("Minimum response time: ${latencyList.min()} ms")
    println("Maximum response time: ${latencyList.max()} ms")
    println("Average response time: ${latencyList.average()} ms")
    println("Latency list: $latencyList")
    println("Successful requests: ${successfullRequestList.size}")

    client.close()
}

suspend fun postMatrixMultiplication(
    client: HttpClient,
    matrix1: Matrix,
    matrix2: Matrix,
    latency: MutableList<Long>,
    successfullList: MutableList<HttpResponse>
): HttpResponse {
    val request = MatrixMultiplicationRequest(matrix1, matrix2)
    val startTime = System.currentTimeMillis()
    val response: HttpResponse = client.post("http://localhost:8080/api/matrix-multiply") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    val endTime = System.currentTimeMillis()

    if (response.status == HttpStatusCode.OK) {
        successfullList.add(response)
        latency.add(endTime - startTime)
    } else {
//        println(response.status)
    }
    println(response.bodyAsText())

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


    val dataSet = DefaultCategoryDataset()
//    responseTimeList.forEachIndexed { index, responseTime ->
//        dataSet.addValue(responseTime, "Response Time", "Request ${index + 1}")
//    }

//    val chart: JFreeChart = ChartFactory.createBarChart(
//        "Response Time Distribution",
//        "Request",
//        "Response Time (ms)",
//        dataSet,
//        PlotOrientation.VERTICAL,
//        true,
//        true,
//        false
//    )

//    val output = File("chart.png")
//    ChartUtils.saveChartAsPNG(output,chart,800,600)
//
//    println("Minimum response time : ${responseTimeList.min()} \n Maximum response time :  ${responseTimeList.max()} \n" +
//            " Average response time : ${responseTimeList.average()}");

//    println(chart.plot)
