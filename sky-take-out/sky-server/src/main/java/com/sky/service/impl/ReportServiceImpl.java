package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;


    //营业额统计
    @Override
    public TurnoverReportVO getturnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("查询营业额数据：{}到{}",begin,end);
        //使用集合存储从第一天到最后一天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }


         List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getTurnoverStatistics(map);
            //判断营业额是否为空，为空则设为0
            turnover = turnover == null ? 0.0 : turnover; turnoverList.add(turnover);
        }
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ",")) .build();

    }

    //用户统计
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //存放新用户数
        List<Integer> newUserList = new ArrayList<>();
        //存放用户数
        List<Integer> totalUserList = new ArrayList<>();


        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();

            //获取总用户数
            map.put("end",endTime);
            Integer totalUser = userMapper.getUserStatistics(map);//获取用户数

            //获取新增用户数
            map.put("begin",beginTime);
            Integer newUser = userMapper.getUserStatistics(map);

            //封装数据
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        //1.存放日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放的集合
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        //2.查询每一天的订单数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
           Integer totalOrderCount = orderMapper.countByMap(map);

            //查询每一天的有效订单数
            map.put("status", Orders.COMPLETED);
           Integer validOrderCount = orderMapper.countByMap(map);

           orderCountList.add(totalOrderCount);
           validOrderCountList.add(validOrderCount);

        }
        //计算订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    //销量排名top10,
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
         LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
         LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
         log.info("查询top10商品销量数据：{}到{}",begin,end);
         //查询时间段内所有的订单名称和销量
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        //把salesTop10集合转换为SalesTop10ReportVO，使用stream 流
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(names, ",");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }
     // 导出营业数据
    @Override
    public void exportBusinessData(HttpServletResponse  response) {

        //1.查询数据库，最近30天
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        //查询概览数据

        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        //2.通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模板创建Excel文件
            XSSFWorkbook workbook = new XSSFWorkbook(in);
            //获取第一个sheet
            //填充时间
            XSSFSheet sheet = workbook.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);


            //获取第四行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            //获取第五行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for(int i =0; i<30 ;i++){
                LocalDate date = begin.plusDays(i);
                //获取日期对应的营业数据
                BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                //设置某一行单元格数据
                row = sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(data.getTurnover());
                row.getCell(3).setCellValue(data.getValidOrderCount());
                row.getCell(4).setCellValue(data.getOrderCompletionRate());
                row.getCell(5).setCellValue(data.getUnitPrice());
                row.getCell(6).setCellValue(data.getNewUsers());
            }

            //3.通过输出流把Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);

            //释放资源
            out.close();
            workbook.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
