package stu.jc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import stu.jc.entity.Order;
import stu.jc.entity.Stock;
import stu.jc.entity.User;
import stu.jc.mapper.OrderMapper;
import stu.jc.mapper.StockMapper;
import stu.jc.mapper.UserMapper;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class OrderService {

    @Autowired
    StockMapper stockMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    UserMapper userMapper;

    public int killB(Integer id) {
        Stock stock = selectStock(id);
        updateStockB(stock);
        return createOrder(stock);
    }

    public int killL(Integer id) {
        Stock stock = selectStock(id);
        updateStock(stock);
        return createOrder(stock);
    }

    // 检查库存
    private Stock selectStock(Integer id) {
        Stock stock = stockMapper.selectStockById(id);
        if (stock.getSold().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    // 悲观锁扣除库存
    private void updateStockB(Stock stock) {
        stockMapper.updateStockB(stock);
    }

    // 乐观锁扣除库存
    private void updateStock(Stock stock) {
        int res = stockMapper.updateStock(stock);
        if (res == 0) {
            throw new RuntimeException("抢购失败");
        }
    }

    // 创建订单
    private Integer createOrder(Stock stock) {
        Order order = new Order();
        order.setSid(stock.getId()).setName(stock.getName()).setCreateTime(new Date());
        orderMapper.createOrder(order);
        return order.getId();
    }

    public String getMD5(Integer id, Integer userid) {
        // 校验用户
        User user = userMapper.findUserById(userid);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        log.info("用户信息:" + user.toString());
        // 校验商品
        Stock stock = stockMapper.selectStockById(id);
        if (stock == null) {
            throw new RuntimeException("商品不存在");
        }
        log.info("商品信息:" + stock.toString());
        // 生成md5写入redis
        String Key = "key_" + userid + "_" + id;
        String value = DigestUtils.md5DigestAsHex((userid + id + "1F0C90A7").getBytes());
        stringRedisTemplate.opsForValue().set(Key, value, 3600, TimeUnit.SECONDS);
        log.info("写入redis" + "hashkey:" + Key + "key:" + value);
        return value;
    }

    public int killmd5(Integer id, Integer userid, String md5) {
        // 判断redis中是否存在商品
        if (!stringRedisTemplate.hasKey("key_" + userid + "_" + id)) {
            throw new RuntimeException("当前抢购活动已经结束");
        }
        // 校验md5
        String key = "key_" + userid + "_" + id;
        String s = stringRedisTemplate.opsForValue().get(key);
        if (s == null)
            throw new RuntimeException("没有签名");
        if (!stringRedisTemplate.opsForValue().get(key).equals(md5))
            throw new RuntimeException("当前请求数据不合法");
        Stock stock = selectStock(id);
        updateStock(stock);
        return createOrder(stock);
    }
}
