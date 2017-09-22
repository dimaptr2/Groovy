package ru.velkomfood.mysap.dms.core

import groovy.transform.CompileStatic

@CompileStatic
class Server {

    private DataProcessor dataProcessor;
    private final long TIME_RANGE = 86400000

    void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor
    }

    void startup() {
        def timer = new Timer()
        Executor executor = new Executor()
        executor.setDataProc(dataProcessor)
        // When will start the task?
//        Calendar startMoment = Calendar.getInstance()

        timer.scheduleAtFixedRate(executor, 1000, TIME_RANGE)
    }

}

@CompileStatic
class Executor extends TimerTask {

    private DataProcessor dataProc

    void setDataProc(DataProcessor dataProc) {
        this.dataProc = dataProc
    }

    @Override
    void run() {
        dataProc.openConnections()
        println('Execute the data uploading')
        dataProc.readAllDocuments()
        dataProc.readDocumentDetails()
        println('End of data uploading')
        dataProc.closeConnections()
    }
}
