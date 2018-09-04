package ru.iegs.services.groovy.poi

import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ExcelReader {

    private String fileName

    ExcelReader(String fileName) {
        this.fileName = fileName
    }

    def processFile() {

        def inStream = new FileInputStream(fileName)
        // Open the work book
        def wb = new XSSFWorkbook(inStream)
        // Open the sheets
        def sheet = wb.getSheetAt(0)
        // Run through rows and cells
//        XSSFRow row
//        XSSFCell cell
//        def indexRow = 0
//        def indexCell = 0
//        def numberRows = sheet.getPhysicalNumberOfRows()
        // Create a range of integers from 0 to numberRows and exclude the numberRows
        // Apache POI method of sheet, called as getPhysicalNumberOfRows, return the number of non empty rows.
        def rangeNumbers = 0..<sheet.getPhysicalNumberOfRows()
        rangeNumbers.each {
            def currentRow = sheet.getRow(it)
            def position = currentRow.getCell(0).getRowIndex()
            def material = currentRow.getCell(1).getStringCellValue()
            def price = currentRow.getCell(2).getNumericCellValue()
            def currency = currentRow.getCell(3).getStringCellValue()
            println(position + " | " + material + " | " + price + " | " + currency)
        }
        // Close the workbook and the stream
        wb.close()
        inStream.close()

    }

}
