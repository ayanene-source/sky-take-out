package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;


    // 营业额统计
    @RequestMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        TurnoverReportVO vo = reportService.getturnoverStatistics(begin,end);
        return Result.success(vo);
    }

    // 用户统计
    @RequestMapping("/userStatistics")
    public Result<UserReportVO> UserStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        UserReportVO vo = reportService.getUserStatistics(begin,end);
        return Result.success(vo);
    }

    // 订单统计
    @RequestMapping("/ordersStatistics")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        OrderReportVO vo = reportService.getOrdersStatistics(begin,end);
        return Result.success(vo);
    }
    //top10 商品
    @GetMapping("/top10")
    public Result top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("查找这段日期内top10：{},{}",begin,end);
        SalesTop10ReportVO vo = reportService.getTop10(begin,end);
        return Result.success(vo);
    }

    //导出数据
    @GetMapping("/export")
    public Result export(HttpServletResponse response){
        reportService.exportBusinessData(response);
        return Result.success();
    }

}
