package ru.velkomfood.mysap.dms

import groovy.transform.CompileStatic
import ru.velkomfood.mysap.dms.core.DataProcessor
import ru.velkomfood.mysap.dms.core.Server

@CompileStatic
class Starter {

    static void main(String[] args) {

        DataProcessor dp = new DataProcessor()
        dp.getSapDestination()

        Server server = new Server()
        server.dataProcessor = dp
        server.startup()

    }

}
