package ru.iegs.services.groovy.poi

class Launcher {

    static void main(String[] args) {
        def reader = new ExcelReader("prices.xlsx")
        reader.processFile()
    }

}
