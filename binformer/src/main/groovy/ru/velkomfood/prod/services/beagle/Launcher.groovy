package ru.velkomfood.prod.services.beagle

import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString
class Event {

    private BigInteger year
    private BigInteger month
    private BigInteger day
    private BigInteger hour
    private BigInteger minute
    private BigInteger second

    Event() { }

    Event(BigInteger year, BigInteger month,
          BigInteger day, BigInteger hour,
          BigInteger minute, BigInteger second) {
        this.year = year
        this.month = month
        this.day = day
        this.hour = hour
        this.minute = minute
        this.second = second
    }

}

class DbEngine {

    private final String DB_URL = 'jdbc:sqlite:events.db'
    private final String DRIVER = 'org.sqlite.JDBC'
    private Sql sql

    void openSqlConnection() {
        sql = Sql.newInstance(DB_URL, DRIVER)
        sql.execute("CREATE TABLE IF NOT EXISTS events(year INTEGER NOT NULL, month INTEGER NOT NULL, " +
                "day INTEGER NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, " +
                "second INTEGER NOT NULL, PRIMARY KEY (year, month, day, hour, minute, second))")
    }

    void closeSqlConnection() {
        sql.close()
    }

    void runInsertCommand(String command, Event event, BigInteger row) {
        def params = [event.year, event.month, event.day, event.hour, event.minute, event.second]
        sql.executeInsert command, params
        println("Row number ${row} inserted")
    }

    boolean isExist(Event event) {

        boolean flag = false

        String selectCommand = "SELECT * FROM events WHERE year = ${event.year} AND " +
                "month = ${event.month} AND day = ${event.day} AND " +
                "hour = ${event.hour} AND minute = ${event.minute} AND second = ${event.second}"

        sql.query(selectCommand) {
            rs -> while (rs.next()) {
                flag = true
                break
            }
        }

        return flag
    }

}

@CompileStatic
class Launcher {

    static void main(String[] args) {

        BigInteger index = 0

        DbEngine db = new DbEngine()
        db.openSqlConnection()

        final String COMMAND = "INSERT INTO events VALUES (?, ?, ?, ?, ?, ?)"
        def events = []
        File fd = new File('beaglebone/events.txt')
        !fd.exists() ?: fd.text.eachLine {
            String[] line = it.split(' ')
            def year = line[5].toBigInteger()
            def month = convertMonth(line[1])
            def day = line[3].toBigInteger()
            String[] moments = line[4].split(':')
            def hour = moments[0].toBigInteger()
            def minute = moments[1].toBigInteger()
            def second = moments[2].toBigInteger()
            Event event = new Event(year, month, day, hour, minute, second)
            if (!db.isExist(event)) {
                index++
                db.runInsertCommand(COMMAND, event, index)
            }
        }

        db.closeSqlConnection()

    }

    private static BigInteger convertMonth(String name) {
        switch (name) {
            case 'Jan':
                1
                break
            case 'Feb':
                2
                break
            case 'Mar':
                3
                break
            case 'Apr':
                4
                break
            case 'May':
                5
                break
            case 'Jun':
                6
                break
            case 'Jul':
                7
                break
            case 'Aug':
                8
                break
            case 'Sep':
                9
                break
            case 'Oct':
                10
                break
            case 'Nov':
                11
                break
            case 'Dec':
                12
                break

        }
    }

}
