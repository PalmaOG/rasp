import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.io.OutputStream
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import java.io.File

data class ScheduleItem(
    val numbLesson: Int,
    val time: String,
    val endTime: String,
    val lessonCall: String,
    val lessonType: String,
    val roomNumb: String,
    val buildsLocation: String,
    val teacher: String,
    val teacherId: Int
)

fun generateHtml(scheduleItem: ScheduleItem): String {
    return """
        <div class="row align-items-center fs-5 m-0 py-3 rasp-cell" style="width: 100%;">
            <div class="col-1" style="border-right: 1px solid gray;">${scheduleItem.numbLesson}</div>
            <div class="col">${scheduleItem.time} - ${scheduleItem.endTime}</div>
            <div class="col"><b>${scheduleItem.lessonCall}</b><br><span class="badge bg-primary">${scheduleItem.lessonType}</span></div>
            <div class="col"><b>ауд. ${scheduleItem.roomNumb}</b><br><span class="text-body-secondary">${scheduleItem.buildsLocation}</span></div>
            
            <div class="col"><a href="/static/Profile.html?id=${scheduleItem.teacherId}" class="teacher-link">${scheduleItem.teacher}</a></div>
        <button type="button" class="btn btn-secondary" style="width:80px;" data-bs-toggle="modal" data-bs-target="#exampleModal" data-bs-whatever="3 пара">Не прийду</button>
        </div>
    """.trimIndent()
}

fun getScheduleItemsFromDatabase(date: String): List<ScheduleItem> {
    val url = "jdbc:mysql://localhost:3306/rasp"
    val user = "root"
    val password = "root"

    val connection: Connection = DriverManager.getConnection(url, user, password)
    val statement = connection.createStatement()
    val query = """
        SELECT 
            raspis.Numb_lesson, 
            raspis.Time, 
            raspis.EndTime, 
            lessons.Lessons_call, 
            raspis.Lessons_type, 
            rooms.Rooms_numb, 
            builds.Builds_location, 
            CONCAT(teachers.Surname, ' ', teachers.Name, ' ', teachers.Father_name) AS Teacher,
            teachers.idTeachers
        FROM 
            raspis
        JOIN 
            lessons ON raspis.idLessons = lessons.idLessons
        JOIN 
            rooms ON raspis.idRooms = rooms.idRooms
        JOIN 
            builds ON raspis.idBuilds = builds.idBuilds
        JOIN 
            teachers ON raspis.idTeachers = teachers.idTeachers
        WHERE 
            raspis.date = '$date';
    """
    val resultSet: ResultSet = statement.executeQuery(query)

    val scheduleItems = mutableListOf<ScheduleItem>()
    while (resultSet.next()) {
        val numbLesson = resultSet.getInt("Numb_lesson")
        val time = resultSet.getString("Time")
        val endTime = resultSet.getString("EndTime")
        val lessonCall = resultSet.getString("Lessons_call")
        val lessonType = resultSet.getString("Lessons_type")
        val roomNumb = resultSet.getString("Rooms_numb")
        val buildsLocation = resultSet.getString("Builds_location")
        val teacher = resultSet.getString("Teacher")
        val teacherId = resultSet.getInt("idTeachers")

        scheduleItems.add(ScheduleItem(numbLesson, time, endTime, lessonCall, lessonType, roomNumb, buildsLocation, teacher, teacherId))
    }

    resultSet.close()
    statement.close()
    connection.close()

    return scheduleItems
}

fun getDatesFromDatabase(): List<String> {
    val url = "jdbc:mysql://localhost:3306/rasp"
    val user = "root"
    val password = "root"

    val connection: Connection = DriverManager.getConnection(url, user, password)
    val statement = connection.createStatement()
    val query = ("SELECT Dates FROM alldates")
    val resultSet: ResultSet = statement.executeQuery(query)

    val dates = mutableListOf<String>()
    while (resultSet.next()) {
        dates.add(resultSet.getString("Dates"))
    }

    resultSet.close()
    statement.close()
    connection.close()

    return dates
}

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)
    server.createContext("/", RootHandler())
    server.createContext("/login", LoginHandler())
    server.createContext("/static", StaticFileHandler("static"))
    server.createContext("/ads", AdsHandler())
    server.createContext("/student-name", StudentNameHandler())
    server.createContext("/schedule", ScheduleHandler())
    server.createContext("/dates", DatesHandler())
    server.createContext("/student-profile", StudentProfileHandler())
    server.createContext("/teacher-profile", TeacherProfileHandler())
    server.createContext("/current-user", CurrentUserHandler())

    server.executor = null
    server.start()
    println("Server started on port 8080")
}

class RootHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val response = this::class.java.getResource("/static/Login.html").readText()
        exchange.sendResponseHeaders(200, response.length.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(response.toByteArray())
        os.close()
    }
}

class LoginHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        if (exchange.requestMethod.equals("POST", ignoreCase = true)) {
            val requestBody = exchange.requestBody.bufferedReader().use { it.readText() }
            val params = parseQuery(requestBody)
            val login = params["login"]
            val passwd = params["passwd"]

            val userId = checkCredentials(login, passwd)
            if (userId != null) {
                // Устанавливаем куки для авторизации
                exchange.responseHeaders.add("Set-Cookie", "auth=true; Path=/")
                exchange.responseHeaders.add("Set-Cookie", "userId=$userId; Path=/")
                // Устанавливаем заголовок Location для редиректа
                exchange.responseHeaders.add("Location", "/static/index.html")
                // Отправляем статус 302 для редиректа
                exchange.sendResponseHeaders(302, -1) // 302 Found для редиректа
            } else {
                val response = "Invalid login or password"
                exchange.sendResponseHeaders(200, response.length.toLong())
                val os: OutputStream = exchange.responseBody
                os.write(response.toByteArray())
                os.close()
            }
        }
    }

    private fun parseQuery(query: String): Map<String, String> {
        return query.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }
    }

    private fun checkCredentials(login: String?, passwd: String?): Int? {
        if (login == null || passwd == null) {
            return null
        }

        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            if (connection.isValid(2)) {
                statement = connection.createStatement()
                resultSet = statement.executeQuery("SELECT idStudents FROM students WHERE Login='$login' AND Passwd='$passwd'")
                if (resultSet.next()) {
                    resultSet.getInt("idStudents")
                } else {
                    null
                }
            } else {
                println("Failed to establish a valid connection to the database.")
                null
            }
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            null
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}

class StaticFileHandler(private val basePath: String) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val requestedPath = exchange.requestURI.path.removePrefix("/static")
        val file = File(basePath, requestedPath)

        // Проверка авторизации
        val cookies = exchange.requestHeaders["Cookie"]
        val isAuthenticated = cookies?.any { it.contains("auth=true") } ?: false

        if (!isAuthenticated && (requestedPath == "/index.html" || requestedPath.startsWith("/Profile.html"))) {
            // Перенаправление на страницу логина
            exchange.responseHeaders.add("Location", "/static/Login.html")
            exchange.sendResponseHeaders(302, -1)
        } else if (file.exists() && file.isFile) {
            val response = file.readBytes()
            exchange.sendResponseHeaders(200, response.size.toLong())
            val os: OutputStream = exchange.responseBody
            os.write(response)
            os.close()
        } else {
            exchange.sendResponseHeaders(404, 0)
            exchange.responseBody.close()
        }
    }
}

class AdsHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val response = getAdsContent()
        exchange.responseHeaders.add("Content-Type", "text/html; charset=UTF-8")
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(response.toByteArray())
        os.close()
    }

    private fun getAdsContent(): String {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            statement = connection.createStatement()
            val query = ("""
                SELECT 
                    specialties.little_speciality, 
                    groups.Groups_call
                FROM 
                    students
                JOIN 
                    `groups ON students.idGroups = `groups`.idGroups
                JOIN 
                    specialties ON students.idSpecialities = specialties.idSpecialties
                WHERE 
                    students.idSpecialities = 2 
                    AND students.idGroups = 2;
            """)
            resultSet = statement.executeQuery(query)
            val contentBuilder = StringBuilder()
            while (resultSet.next()) {
                val littleSpeciality = resultSet.getString("little_speciality")
                val groupsCall = resultSet.getString("Groups_call")
                contentBuilder.append("<span class='badge'>$littleSpeciality - $groupsCall</span><br>")
            }
            contentBuilder.toString()
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            "Error retrieving data"
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}

class StudentNameHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val response = getStudentName()
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=UTF-8")
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(response.toByteArray())
        os.close()
    }

    private fun getStudentName(): String {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            statement = connection.createStatement()
            val query = ("SELECT Name, Surname FROM students ") // Adjust the query as needed
            resultSet = statement.executeQuery(query)
            if (resultSet.next()) {
                val name = resultSet.getString("Name")
                val surname = resultSet.getString("Surname")
                "$name $surname"
            } else {
                "Student not found"
            }
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            "Error retrieving data"
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}

class ScheduleHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val queryParams = exchange.requestURI.query
        val date = queryParams?.split("=")?.get(1) ?: getCurrentDate() // Default to current date if not provided
        val scheduleItems = getScheduleItemsFromDatabase(date)
        val htmlContent = scheduleItems.joinToString("\n") { generateHtml(it) }
        exchange.responseHeaders.add("Content-Type", "text/html; charset=UTF-8")
        exchange.sendResponseHeaders(200, htmlContent.toByteArray().size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(htmlContent.toByteArray())
        os.close()
    }

    private fun getCurrentDate(): String {
        val currentDate = java.time.LocalDate.now()
        return currentDate.toString()
    }
}

class DatesHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val dates = getDatesFromDatabase()
        val jsonResponse = dates.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        exchange.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
        exchange.sendResponseHeaders(200, jsonResponse.toByteArray().size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(jsonResponse.toByteArray())
        os.close()
    }
}

class StudentProfileHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val response = getStudentProfileHtml()
        exchange.responseHeaders.add("Content-Type", "text/html; charset=UTF-8")
        exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
        val os: OutputStream = exchange.responseBody
        os.write(response.toByteArray())
        os.close()
    }

    private fun getStudentProfileHtml(): String {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            statement = connection.createStatement()
            val query = "SELECT Surname, Name FROM students WHERE idStudents = 1"
            resultSet = statement.executeQuery(query)
            if (resultSet.next()) {
                val surname = resultSet.getString("Surname")
                val name = resultSet.getString("Name")
                "$surname $name"
            } else {
                "Student not found"
            }
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            "Error retrieving data"
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}

class TeacherProfileHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val queryParams = exchange.requestURI.query
        val teacherId = queryParams?.split("=")?.get(1)?.toIntOrNull()

        if (teacherId != null) {
            val response = getTeacherProfileHtml(teacherId)
            exchange.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            val os: OutputStream = exchange.responseBody
            os.write(response.toByteArray())
            os.close()
        } else {
            exchange.sendResponseHeaders(400, 0)
            exchange.responseBody.close()
        }
    }

    private fun getTeacherProfileHtml(teacherId: Int): String {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            statement = connection.createStatement()
            val query = "SELECT Surname, Name, Father_name, Post, Login FROM teachers WHERE idTeachers = $teacherId"
            resultSet = statement.executeQuery(query)
            if (resultSet.next()) {
                val surname = resultSet.getString("Surname")
                val name = resultSet.getString("Name")
                val fatherName = resultSet.getString("Father_name")
                val post = resultSet.getString("Post")
                val login = resultSet.getString("Login")
                """{"name": "$surname $name $fatherName", "post": "$post", "login": "$login"}"""
            } else {
                """{"name": "Преподаватель не найден", "post": "", "login": ""}"""
            }
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            """{"name": "Ошибка при получении данных", "post": "", "login": ""}"""
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}
class CurrentUserHandler : HttpHandler {
    override fun handle(exchange: HttpExchange) {
        val cookies = exchange.requestHeaders["Cookie"]
        val userId = cookies?.find { it.startsWith("userId=") }?.split("=")?.get(1)?.toIntOrNull()

        if (userId != null) {
            val response = getUserData(userId)
            exchange.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
            exchange.sendResponseHeaders(200, response.toByteArray().size.toLong())
            val os: OutputStream = exchange.responseBody
            os.write(response.toByteArray())
            os.close()
        } else {
            exchange.sendResponseHeaders(401, 0)
            exchange.responseBody.close()
        }
    }

    private fun getUserData(userId: Int): String {
        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null
        return try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/rasp", "root", "root")
            statement = connection.createStatement()
            val query = "SELECT Surname, Name FROM students WHERE idStudents = $userId"
            resultSet = statement.executeQuery(query)
            if (resultSet.next()) {
                val surname = resultSet.getString("Surname")
                val name = resultSet.getString("Name")
                """{"surname": "$surname", "name": "$name"}"""
            } else {
                """{"error": "User not found"}"""
            }
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
            """{"error": "Error retrieving data"}"""
        } finally {
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }
}
