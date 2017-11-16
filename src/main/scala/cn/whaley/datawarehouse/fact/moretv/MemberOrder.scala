package cn.whaley.datawarehouse.fact.moretv

import cn.whaley.datawarehouse.common.{DimensionColumn, DimensionJoinCondition, UserDefinedColumn}
import cn.whaley.datawarehouse.fact.FactEtlBase
import cn.whaley.datawarehouse.util.{DataExtractUtils, DateFormatUtils, MysqlDB}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.udf

/**
  * Created by xia.jun on 2017/11/9.
  */
object MemberOrder extends FactEtlBase{

  topicName = "fact_medusa_member_order"

  columnsFromSource = List(
    ("order_code","order_code"),
    ("order_desc","order_desc"),
    ("vip_pay_time","vip_pay_time"),
    ("vip_start_time","vip_start_time"),
    ("vip_end_time","vip_end_time"),
    ("activity_id","activity_id"),
    ("total_price","total_price"),
    ("real_price", "real_price"),
    ("payment_amount","payment_amount"),
    ("business_type","business_type"),
    ("pay_channel","pay_channel"),
    ("order_channel","order_channel"),
    ("order_type","order_type"),
    ("valid_status","valid_status"),
    ("trade_status","trade_status"),
    ("pay_status","pay_status"),
    ("duration","duration"),
    ("create_time","create_time"),
    ("update_time","update_time"),
    ("dim_date","dim_date"),
    ("dim_time","dim_time")
  )

  override def readSource(sourceDate: String): DataFrame = {
    val sourceDB = MysqlDB.medusaMemberDB("business_order")
    val businessOrderDF = DataExtractUtils.readFromJdbc(sqlContext,sourceDB)
    businessOrderDF.filter(s"substring(create_time,0,10) = '${DateFormatUtils.cnFormat.format(DateFormatUtils.readFormat.parse(sourceDate))}'")
  }

  addColumns = List(
    UserDefinedColumn("dim_date", udf(getDimDate: String => String), List("create_time")),
    UserDefinedColumn("dim_time", udf(getDimTime: String => String), List("create_time"))
  )


  dimensionColumns = List(

    /** 基于订单中的real_price获取对应的商品维度good_sk */
    new DimensionColumn("dim_medusa_member_goods",
      List(DimensionJoinCondition(Map("payment_amount" -> "good_price", "member_code" -> "member_code"))),
      "good_sk","good_sk"),

    /** 基于订单中的account_id获取账号表中的账号维度account_sk */
    new DimensionColumn("dim_medusa_account",
      List(DimensionJoinCondition(Map("account_id" -> "account_id"))),
      "account_sk","account_sk")
  )


  def getDimDate(dateTime: String): String = {
    try {
      val dateTimeInfo = dateTime.split(" ")
      if (dateTimeInfo.length >= 2) {
        dateTimeInfo(0)
      } else ""
    } catch {
      case ex: Exception => ""
    }
  }

  def getDimTime(dateTime: String): String = {
    try {
      val dateTimeInfo = dateTime.split(" ")
      if (dateTimeInfo.length >= 2) {
        dateTimeInfo(1)
      } else ""
    } catch {
      case ex: Exception => ""
    }
  }


}
