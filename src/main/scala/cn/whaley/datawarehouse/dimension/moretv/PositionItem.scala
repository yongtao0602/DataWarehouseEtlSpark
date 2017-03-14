package cn.whaley.datawarehouse.dimension.moretv

import cn.whaley.datawarehouse.dimension.DimensionBase
import cn.whaley.datawarehouse.dimension.constant.SourceType._
import cn.whaley.datawarehouse.util.MysqlDB


/**
  * Created by Tony on 17/3/8.
  *
  * 电视猫账号维度表
  */
object PositionItem extends DimensionBase {
  columns.skName = "position_item_sk"
  columns.primaryKeys = List("position_item_id")
  columns.trackingColumns = List()
  columns.otherColumns = List("position_code", "position_index","item_sid","item_name","type","status")

  readSourceType = jdbc

  //维度表的字段对应源数据的获取方式
  sourceColumnMap = Map(
    "position_item_id"->"id",
    "position_code" -> "position_code",
    "position_index"->"index",
    "item_sid"->"item_sid",
    "item_name"->"item_title",
    "type"->"type",
    "status"->"status"
  )

  sourceFilterWhere = "position_item_id is not null"
  sourceDb = MysqlDB.medusaCms("mtv_positionItem", "id", 1, 2010000000, 1)

  dimensionName = "dim_meusa_position_item"
}
