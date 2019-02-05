package demo

import java.io.File
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

const val header = "Path,User,Timestamp"

class StatsByDay(date: LocalDate) {
    val date = date
    val urlViews = HashMap<String, UrlView>()
    val userViews = HashMap<String, UserView>()

    fun tallyView(url: String, userId: String) {
        if (urlViews.containsKey(url)) {
            urlViews.get(url)?.addUser(userId)
        } else {
            val endpoint = UrlView(url)
            endpoint.addUser(userId)
            urlViews.put(url, endpoint)
        }
        if (userViews.containsKey(userId)) {
            userViews.get(userId)?.addView(url)
        } else {
            val user = UserView(userId)
            user.addView(url)
            userViews.put(userId, user)
        }
    }
}

class UserView(userId: String) : Comparable<UserView> {
    val userId: String = userId
    val uniqueUrlsVisited: MutableSet<String> = HashSet()

    fun addView(url: String) {
        uniqueUrlsVisited.add(url)
    }

    fun getUniqueVisits(): Int {
        return uniqueUrlsVisited.size
    }

    override operator fun compareTo(other: UserView): Int {
        return this.getUniqueVisits() - other.getUniqueVisits()
    }
}

class UrlView(url: String) : Comparable<UrlView> {
    val url: String = url
    val uniqueVisitors: MutableSet<String> = HashSet()

    fun addUser(user: String) {
        uniqueVisitors.add(user)
    }

    fun getUniqueVisitors(): Int {
        return uniqueVisitors.size
    }

    override operator fun compareTo(other: UrlView): Int {
        return this.getUniqueVisitors() - other.getUniqueVisitors()
    }
}

fun readFile(fileName: String, statsByDay: HashMap<LocalDate, StatsByDay>) {
    val fileObject = File(fileName)
    if (fileObject.exists() && fileObject.bufferedReader().readLine().equals(header, true)) {
        fileObject.forEachLine {
            parseLine(it, statsByDay)
        }
    } else {
        println("File does not exist or is formatted incorrectly.")
    }
}

fun parseLine(line: String, statsByDay: HashMap<LocalDate, StatsByDay>) {
    if (line != header) {
        val sections = line.split(",")
        if (sections.size != 3) {
            return
        } else {
            val url = sections[0]
            val userId = sections[1]
            val zonedDateTime = ZonedDateTime.parse(sections[2])
            val localDate = zonedDateTime.toLocalDate()
            if (statsByDay.containsKey(localDate)) {
                statsByDay.get(localDate)?.tallyView(url, userId)
            } else {
                val dailyStats = StatsByDay(localDate)
                dailyStats.tallyView(url, userId)
                statsByDay.put(localDate, dailyStats)
            }
        }
    }
}

fun printUsage(stats: StatsByDay) {
    println("Stats for ${stats.date}:")
    stats.urlViews.values.sortedByDescending { it: UrlView -> it }.forEach {
        println("${it.url}: ${it.getUniqueVisitors()} unique visitors")
    }
    stats.userViews.values.sortedByDescending { it: UserView -> it }.forEach {
        println("${it.userId}: ${it.getUniqueVisits()} unique page views")
    }
}

fun main(arg: Array<String>) {
    val fileName = "log.csv"
    val statsByDay = HashMap<LocalDate, StatsByDay>()
    readFile(fileName, statsByDay)
    statsByDay.forEach { _, stats: StatsByDay ->
        printUsage(stats)
    }
}