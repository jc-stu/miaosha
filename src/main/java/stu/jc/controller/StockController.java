package stu.jc.controller;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stu.jc.service.OrderService;
import stu.jc.service.UserService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/stock")
@Slf4j
public class StockController {

    // 创建令牌桶
    private final RateLimiter rateLimiter = RateLimiter.create(1000);
    @Autowired
    OrderService orderService;
    @Autowired
    UserService userService;

    // 悲观锁
    @GetMapping("/killB/{id}")
    public String killB(@PathVariable("id") Integer id) {
        // 秒杀的商品id
        System.out.println(id);
        try {
            synchronized (this) {
                int orderId = orderService.killB(id);
                return "商品秒杀成功，订单id为" + orderId;
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 乐观锁
    @GetMapping("/killL/{id}")
    public String killL(@PathVariable("id") Integer id) {
        // 秒杀的商品id
        System.out.println(id);
        try {
            int orderId = orderService.killL(id);
            return "商品秒杀成功，订单id为" + orderId;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 乐观锁+令牌桶实现限流
    @GetMapping("/killtoken/{id}")
    public String killtoken(@PathVariable("id") Integer id) {
        System.out.println(id);
        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) {
            return "抢购失败";
        }
        try {
            int orderId = orderService.killL(id);
            return "商品秒杀成功，订单id为" + orderId;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 获取md5，实现接口隐藏
    @GetMapping("/md5")
    public String getMD5(Integer id, Integer userid) {
        String md5;
        try {
            md5 = orderService.getMD5(id, userid);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取md5失败：" + e.getMessage();
        }
        return "获取md5：" + md5;
    }

    // 乐观锁+令牌桶+md5接口隐藏
    @GetMapping("/killtokenmd5")
    public String killtokenmd5(Integer id, Integer userid, String md5) {
        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) {
            return "抢购失败";
        }
        try {
            int orderId = orderService.killmd5(id, userid, md5);
            return "商品秒杀成功，订单id为" + orderId;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // 乐观锁防止超卖+令牌桶限流+md5接口隐藏+单用户访问频率限制
    @GetMapping("/killtokenlimit")
    public String killtokenlimit(Integer id, Integer userid, String md5) {
        // 令牌桶限流
        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) {
            return "抢购失败,活动过于火爆，请重试";
        }
        try {
            // 单用户调用接口频率限制
            int count = userService.setRate(userid);
            log.info("用户访问次数" + count);
            boolean isBanned = userService.isRateExceeded(userid);
            if (isBanned) {
                log.info("用户超过频率限制");
                return "购买失败,超过频率限制";
            }
            int orderId = orderService.killmd5(id, userid, md5);
            return "商品秒杀成功，订单id为" + String.valueOf(orderId);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
