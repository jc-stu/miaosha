package stu.jc.mapper;

import stu.jc.entity.Stock;

public interface StockMapper {
    Stock selectStockById(Integer id);
    int updateStock(Stock stock);
    void updateStockB(Stock stock);
}
