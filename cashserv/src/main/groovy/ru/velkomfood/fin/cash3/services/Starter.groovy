package ru.velkomfood.fin.cash3.services

import groovy.transform.CompileStatic
import ru.velkomfood.fin.cash3.services.core.Pump

@CompileStatic
class Starter {

    @Override
    boolean equals(Object obj) {
        return super.equals(obj)
    }

    static void main(String[] args) {

        BigInteger t1 = new Date().getTime()

        println('Start Cash Journal service')
        Pump pump = new Pump()
        pump.initSAPdestinations()
        pump.openDbConnection()
        pump.processDataStream()
        pump.closeDbConnection()
        println('Stop Cash Journal Service')

        BigInteger t2 = new Date().getTime()

    }

}
