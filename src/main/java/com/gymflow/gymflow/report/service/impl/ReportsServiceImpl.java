package com.gymflow.gymflow.report.service.impl;



import com.gymflow.gymflow.attendance.entity.Attendance;
import com.gymflow.gymflow.attendance.repository.AttendanceRepository;
import com.gymflow.gymflow.common.exception.GymNotFoundException;
import com.gymflow.gymflow.gym.repository.GymRepository;
import com.gymflow.gymflow.member.repository.MemberRepository;
import com.gymflow.gymflow.report.dto.AttendanceReportDTO;
import com.gymflow.gymflow.report.dto.MemberReportDTO;
import com.gymflow.gymflow.report.dto.RevenueReportDTO;
import com.gymflow.gymflow.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Month;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportsServiceImpl implements ReportsService {

    private final GymRepository gymRepository;
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    // --- DASHBOARD DTO METHODS (Original) ---

    @Override
    public RevenueReportDTO getRevenueReport(Long gymId, int year) {
        Map<String, BigDecimal> monthlyRevenue = fetchMonthlyRevenueData(gymId, year);
        BigDecimal totalRevenue = monthlyRevenue.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return RevenueReportDTO.builder().totalRevenue(totalRevenue).monthlyRevenue(monthlyRevenue).build();
    }

    @Override
    public MemberReportDTO getMemberReport(Long gymId) {
        return MemberReportDTO.builder()
                .totalMembers(memberRepository.countByGymId(gymId))
                .activeMembers(memberRepository.countByGymIdAndStatus(gymId, "ACTIVE"))
                .expiredMembers(memberRepository.countByGymIdAndStatus(gymId, "EXPIRED"))
                .pendingMembers(memberRepository.countByGymIdAndStatus(gymId, "PENDING"))
                .build();
    }

    @Override
    public AttendanceReportDTO getAttendanceReport(Long gymId) {
        List<String> recentLogs = attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId)
                .stream().limit(5).map(a -> a.getMember().getName() + " checked in at " + a.getCheckInTime()).toList();
        return AttendanceReportDTO.builder()
                .totalSessions(attendanceRepository.countByGymId(gymId))
                .activeSessions(attendanceRepository.countByGymIdAndCheckOutTimeIsNull(gymId))
                .recentAttendanceLogs(recentLogs).build();
    }

    // --- EXCEL GENERATION METHODS ---

    @Override
    public byte[] generateAttendanceExcel(Long gymId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance Logs");
            createHeader(workbook, sheet, new String[]{"Member Name", "Check-In Time", "Check-Out Time"});

            List<Attendance> logs = attendanceRepository.findByGymIdOrderByCheckInTimeDesc(gymId);
            int rowIdx = 1;
            for (Attendance log : logs) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(log.getMember().getName());
                row.createCell(1).setCellValue(log.getCheckInTime().toString());
                row.createCell(2).setCellValue(log.getCheckOutTime() != null ? log.getCheckOutTime().toString() : "In Gym");
            }
            return finalizeWorkbook(workbook, out);
        } catch (IOException e) { throw new RuntimeException("Excel generation failed", e); }
    }

    @Override
    public byte[] generateMemberExcel(Long gymId) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Member Summary");
            createHeader(workbook, sheet, new String[]{"Category", "Count"});

            MemberReportDTO data = getMemberReport(gymId);
            String[][] rows = {
                    {"Total Members", String.valueOf(data.getTotalMembers())},
                    {"Active Members", String.valueOf(data.getActiveMembers())},
                    {"Expired Members", String.valueOf(data.getExpiredMembers())},
                    {"Pending Members", String.valueOf(data.getPendingMembers())}
            };

            for (int i = 0; i < rows.length; i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(rows[i][0]);
                row.createCell(1).setCellValue(rows[i][1]);
            }
            return finalizeWorkbook(workbook, out);
        } catch (IOException e) { throw new RuntimeException("Excel generation failed", e); }
    }

    @Override
    public byte[] generateRevenueExcel(Long gymId, int year) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Revenue Report " + year);
            createHeader(workbook, sheet, new String[]{"Month", "Revenue (INR)"});

            Map<String, BigDecimal> revenueData = fetchMonthlyRevenueData(gymId, year);
            int rowIdx = 1;
            for (Map.Entry<String, BigDecimal> entry : revenueData.entrySet()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue().doubleValue());
            }

            // Add Total Row
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(0).setCellValue("TOTAL ANNUAL REVENUE");
            BigDecimal total = revenueData.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            totalRow.createCell(1).setCellValue(total.doubleValue());

            return finalizeWorkbook(workbook, out);
        } catch (IOException e) { throw new RuntimeException("Excel generation failed", e); }
    }

    // --- HELPER METHODS ---

    private Map<String, BigDecimal> fetchMonthlyRevenueData(Long gymId, int year) {
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            BigDecimal revenue = memberRepository.calculateMonthlyRevenue(gymId, month); // Ensure your repo supports year if needed
            monthlyRevenue.put(Month.of(month).name(), revenue != null ? revenue : BigDecimal.ZERO);
        }
        return monthlyRevenue;
    }

    private void createHeader(Workbook workbook, Sheet sheet, String[] columns) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
    }

    private byte[] finalizeWorkbook(Workbook workbook, ByteArrayOutputStream out) throws IOException {
        workbook.write(out);
        return out.toByteArray();
    }
}