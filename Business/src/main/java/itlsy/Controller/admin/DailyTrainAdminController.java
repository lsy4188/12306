//package itlsy.Controller.admin;
//
//
//import itlsy.annotation.DeleteCache;
//import itlsy.annotation.GetCache;
//import itlsy.req.*;
//import itlsy.resp.*;
//import itlsy.service.DailyTrainService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//
//@RestController
//@RequestMapping("/business/admin/daily-train")
//public class DailyTrainAdminController {
//
//    @Autowired
//    private DailyTrainService dailyTrainService;
//
//    @PostMapping("/save")
//    @DeleteCache(cacheName = "dailyTrain",key = "DailyTrainAdminController")
//    public CommonResp<Object> save(@Valid @RequestBody DailyTrainSaveReq req) {
//        dailyTrainService.save(req);
//        return new CommonResp<>();
//    }
//
//    @GetMapping("/query-list")
//    @GetCache(cacheName = "dailyTrain",key = "DailyTrainAdminController",expire = 20,timeUnit = TimeUnit.SECONDS)
//    public CommonResp<PageResp<DailyTrainQueryResp>> queryList(@Valid DailyTrainQueryReq req) {
//        PageResp<DailyTrainQueryResp> list = dailyTrainService.queryList(req);
//        return new CommonResp<>(list);
//    }
//
//    @DeleteMapping("/delete/{id}")
//    public CommonResp<Object> delete(@PathVariable Long id) {
//        dailyTrainService.delete(id);
//        return new CommonResp<>();
//    }
//
//    /**
//     * 生成某一天的所有火车数据(与定时任务连用)
//     * @param date
//     * @return
//     */
//    @GetMapping("/gen-daily/{date}")
//    public CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
//        dailyTrainService.genDaily(date);
//        return new CommonResp<>();
//    }
//
//    /**
//     * 删除2天之前的火车所有数据
//     */
//    @GetMapping("/delete-before-2-days/{date}")
//    public CommonResp<Object> deleteBefore2Days(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
//        dailyTrainService.deleteBefore2Days(date);
//        return new CommonResp<>();
//    }
//}
