package com.fraudoperations;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.util.*;

public class FraudRuleLoader {

    public static List<FraudRule> loadRules(String filePath) throws IOException {
        List<FraudRule> rules = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet("Transaction Rules");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row 0
                Row row = sheet.getRow(i);
                if (row == null) continue;

                FraudRule rule = new FraudRule();
                rule.setRuleId(getCellString(row, 0));
                rule.setRuleName(getCellString(row, 1));
                rule.setCategory(getCellString(row, 2));
                rule.setFieldMonitored(getCellString(row, 3));
                rule.setCondition(getCellString(row, 4));
                rule.setOperator(getCellString(row, 5));
                rule.setValue(getCellString(row, 6));
                rule.setFlagType(getCellString(row, 7));
                rule.setRiskScore((int) row.getCell(8).getNumericCellValue());
                rule.setActionTriggered(getCellString(row, 9));
                rule.setOnlineOnly(getCellString(row, 10).equalsIgnoreCase("Yes"));
                rule.setNotes(getCellString(row, 11));

                rules.add(rule);
            }
        }
        return rules;
    }

    private static String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return cell.getCellType() == CellType.NUMERIC
            ? String.valueOf((int) cell.getNumericCellValue())
            : cell.getStringCellValue().trim();
    }
}