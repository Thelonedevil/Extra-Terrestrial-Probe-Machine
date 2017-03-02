package uk.tldcode.extraterrestrialprobemachine.plugins

import uk.tldcode.extraterrestrialprobemachine.api.utils.pluralize
import org.pircbotx.hooks.events.MessageEvent
import org.slf4j.LoggerFactory
import uk.tldcode.extraterrestrialprobemachine.api.UserLevel
import uk.tldcode.extraterrestrialprobemachine.api.Command
import uk.tldcode.extraterrestrialprobemachine.api.Plugin
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.TimeUnit

class PointsPlugin(name:String) : Plugin(name) {
    override fun PostInit() {

    }

    override fun Init() {
        val PointsName = readConfigValue("name","point")

        Scheduler.scheduleAtFixedRate({
            getUsers().parallelStream().forEach { AddPoints(it, 5) }
        }, 5L, 5L, TimeUnit.MINUTES)
        Scheduler.scheduleAtFixedRate({ messages.parallelStream().forEach { AddPoints(it, 1) };messages.clear() }, 5L, 5L, TimeUnit.MINUTES)
        Commands.put(PointsName, object : Command {
            override fun invoke(event: MessageEvent, respond: (String) -> Unit) {
                when (event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").first()) {
                    "add" -> {
                        val args = event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").drop(1)
                        if (args.count() == 2) {
                            if (args.first().startsWith("+")) {
                                when (args.first()) {
                                    "+viewers" -> {
                                        respond("Started giving ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} to viewers")
                                        event.channel.usersNicks.parallelStream().forEach { AddPoints(it, args.last().toInt()) }
                                        respond("Finished giving ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} to viewers")
                                    }
                                }
                            } else {
                                AddPoints(args.first(), args.last().toInt())
                                respond("Gave ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} to ${args.first()}")
                            }
                        }
                    }
                    "remove" -> {
                        val args = event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").drop(1)
                        if (args.count() == 2) {
                            if (args.first().startsWith("+")) {
                                when (args.first()) {
                                    "+viewers" -> {
                                        respond("Started taking ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} from viewers")
                                        event.channel.usersNicks.parallelStream().forEach { RemovePoints(it, args.last().toInt()) }
                                        respond("Finished taking ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} from viewers")
                                    }
                                }
                            } else {
                                RemovePoints(args.first(), args.last().toInt())
                                respond("Took ${args.last()} ${PointsName.pluralize(args.last().toInt()).capitalize()} from ${args.first()}")
                            }
                        }
                    }
                    "top" -> {
                        val args = event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").drop(1)
                        if (args.count() == 1) {
                            respond(GetTop(args.first().toInt()).entries.mapIndexed { index, entry -> "#${index + 1} ${entry.key}: ${entry.value}" }.joinToString(", ", "Top ${args.first()}: "))
                        }
                    }
                    else -> {
                        var user = event.user?.nick;
                        if (!event.message.removePrefix("!${PointsName.pluralize()}").isNullOrEmpty()) {
                            user = event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").first()
                        }
                        val points = GetPoints(user!!)
                        respond("$user has $points ${PointsName.pluralize(points).capitalize()}")
                    }
                }
            }

            override fun UserLevel(event: MessageEvent): UserLevel {
                when (event.message.removePrefix("!${PointsName.pluralize()}").trim().split(" ").first()) {
                    "add", "remove" -> return UserLevel.Mod
                    else -> return UserLevel.Viewer
                }
            }

            override fun match(message: String): Boolean {
                return message.startsWith("!${PointsName.pluralize()}")
            }

        })
    }

    init {
        //Dependencies.add("Heists")

    }

    val messages: HashSet<String> = HashSet()
    override fun onMessage(event: MessageEvent) {
        val user = event.user
        if (user != null && !event.message.startsWith("!")) {
            messages.add(user.nick)
        }
    }

    fun GetTop(amount: Int): Map<String, Int> {
        Class.forName(ConnectionDriver)
        val map: HashMap<String, Int> = HashMap()
        try {
            DriverManager.getConnection(ConnectionString).use {
                val statement = it.prepareStatement("select top $amount User, Points from UserPoints order by points desc")
                //statement.setInt(1, amount)
                val rs = statement.executeQuery()
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2))
                }
            }
        } catch(e: SQLException) {
            Logger.error("Urgh", e)
        }
        return map
    }

    fun GetPoints(user: String): Int {
        Class.forName(ConnectionDriver)
        try {
            DriverManager.getConnection(ConnectionString).use {
                val statement = it.prepareStatement("select top 1 points from UserPoints where User = ?")
                statement.setString(1, user)
                val rs = statement.executeQuery()
                var points = 0
                if (rs.next()) {
                    points = rs.getInt(1)
                }
                return points;
            }
        } catch(e: SQLException) {
            Logger.error("Urgh", e)
        }
        return 0
    }

    fun AddPoints(user: String, points: Int) {
        Class.forName(ConnectionDriver)
        try {
            DriverManager.getConnection(ConnectionString).use {
                val pointsStmnt = it.prepareStatement("select top 1 points from UserPoints where User = ?")
                pointsStmnt.setString(1, user)
                val rs = pointsStmnt.executeQuery()
                var oldPoints = 0
                if (rs.next()) {
                    oldPoints = rs.getInt(1)
                }
                val statement = it.prepareStatement("merge into UserPoints(Points,User) key(User) values(?,?)")
                statement.setInt(1, oldPoints + points)
                statement.setString(2, user)
                statement.execute()
            }
        } catch(e: SQLException) {
            Logger.error("Urgh", e)
        }
    }

    fun RemovePoints(user: String, points: Int) {
        Class.forName(ConnectionDriver)
        try {
            DriverManager.getConnection(ConnectionString).use {
                val pointsStmnt = it.prepareStatement("select top 1 points from UserPoints where User = ?")
                pointsStmnt.setString(1, user)
                val rs = pointsStmnt.executeQuery()
                var oldPoints = 0
                if (rs.next()) {
                    oldPoints = rs.getInt(1)
                }
                val statement = it.prepareStatement("merge into UserPoints(Points,User) key(User) values(?,?)")
                statement.setInt(1, Math.max(oldPoints - points, 0))
                statement.setString(2, user)
                statement.execute()
            }
        } catch(e: SQLException) {
            Logger.error("Urgh", e)
        }
    }
}