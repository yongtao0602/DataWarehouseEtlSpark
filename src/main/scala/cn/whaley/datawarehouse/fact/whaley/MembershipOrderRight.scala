package cn.whaley.datawarehouse.fact.whaley

import java.util.Calendar

import cn.whaley.datawarehouse.fact.FactEtlBase
import cn.whaley.datawarehouse.fact.common.{DimensionColumn, DimensionJoinCondition}
import cn.whaley.datawarehouse.util.{DateFormatUtils, MysqlDB}
import org.apache.spark.sql.DataFrame
/**
  * 创建人：郭浩
  * 创建时间：2017/4/18
  * 程序作用：会员订单权益表
  * 数据输入：订单表，发货表，商品表，会员权益表
  * 数据输出：会员订单权益表
  */
object MembershipOrderRight extends FactEtlBase{
  topicName = "fact_whaley_membership_order_right"

  addColumns = List(
  )

  columnsFromSource = List(
    ("product_sn", "sn"),
    ("membership_account", "whaleyAccount"),
    ("order_id", "whaleyOrder"),
    ("product_id", "whaleyProduct"),
    ("prime_price", "case when dim_whaley_membership_order_delivered.is_buy = 0 then 0 else source.totalPrice end  "),
    ("payment_amount", "case when dim_whaley_membership_order_delivered.is_buy = 0 then 0 else source.paymentAmount end "),
    ("duration", "duration"),
    ("duration_day", "duration_day")
  )

  dimensionColumns = List(
    DimensionColumn("dim_whaley_membership_account_order",
      List(DimensionJoinCondition(Map("sn" -> "product_sn","whaleyOrder" -> "order_id"))), "membership_order_sk"),
    DimensionColumn("dim_whaley_membership_order_delivered",
      List(DimensionJoinCondition(Map("sn" -> "product_sn","whaleyOrder" -> "order_id","whaleyProduct" -> "product_id"))), "membership_order_delivered_sk"),
    DimensionColumn("dim_whaley_membership_goods",
      List(DimensionJoinCondition(Map("goodsNo" -> "goods_no"))), "membership_goods_sk"),
    DimensionColumn("dim_whaley_membership_right",
      List(DimensionJoinCondition(Map("sn" -> "product_sn","whaleyAccount" -> "membership_account","whaleyProduct" -> "product_id"))), "membership_right_sk"),
    DimensionColumn("dim_whaley_product_sn",
      List(DimensionJoinCondition(Map("sn" -> "product_sn"))), "product_sn_sk")
  )

  override def readSource(startDate: String): DataFrame = {


    val cal = Calendar.getInstance()
    cal.setTime(DateFormatUtils.readFormat.parse(startDate))
    val day = DateFormatUtils.cnFormat.format(cal.getTime)
    //账号订单表
    val dolphin_whaley_account_order = MysqlDB.whaleyDolphin("dolphin_whaley_account_order","id",1, 1000000000, 10)
    sqlContext.read.format("jdbc").options(dolphin_whaley_account_order).load()
      .filter("orderStutas ='1' and substr(sn,2) not in ('XX','XY','XZ','YX','YY','YZ','ZX')").registerTempTable("account_order")

    //账号发货订单表
    val dolphin_whaley_delivered_order = MysqlDB.whaleyDolphin("dolphin_whaley_delivered_order","id",1, 1000000000, 10)
    sqlContext.read.format("jdbc").options(dolphin_whaley_delivered_order).load()
      .filter("status = 1 and substr(sn,2) not in ('XX','XY','XZ','YX','YY','YZ','ZX')").registerTempTable("delivered_order")
    val sql1 =
      s"""
         | select a.sn,a.whaleyAccount,a.whaleyOrder,a.goodsNo,
         |    b.whaleyProduct,a.totalPrice,a.paymentAmount,
         |    b.duration,b.duration_day
         |  from
         |     account_order a
         |  left join
         |  delivered_order  b
         |  on a.sn = b.sn and a.whaleyOrder = b.orderId
         |  where (a.orderStatus = 4 and substr(b.create_time,1,10) = '${day}')
         |    or (a.orderStatus != 4 and substr(a.overTime,1,10) = '${day}')
       """.stripMargin

    val sql =
      s"""
         | select a.sn,a.whaleyAccount,a.whaleyOrder,a.goodsNo,
         |    b.whaleyProduct,a.totalPrice,a.paymentAmount,
         |    b.duration,b.duration_day
         |  from
         |     account_order a
         |  left join
         |  delivered_order  b
         |  on a.sn = b.sn and a.whaleyOrder = b.orderId
       """.stripMargin
    sqlContext.sql(sql)
  }

}