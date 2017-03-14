package cn.whaley.datawarehouse.dimension.whaley

import cn.whaley.datawarehouse.dimension.DimensionBase
import cn.whaley.datawarehouse.dimension.constant.SourceType._
import cn.whaley.datawarehouse.util.MysqlDB


/**
  * Created by czw on 17/3/14.
  *
  * 微鲸端歌手维度表
  */
object Singer extends DimensionBase {
  columns.skName = "singer_sk"
  columns.primaryKeys = List("singer_sid")
  columns.trackingColumns = List()
  columns.otherColumns = List("singer_name")

  readSourceType = jdbc

  //维度表的字段对应源数据的获取方式
  sourceColumnMap = Map(
    "singer_sid" -> "sid",
    "singer_name" -> "name"
  )

  sourceFilterWhere = "singer_sid is not null and singer_sid <> ''"
  sourceDb = MysqlDB.whaleyTvService("mtv_singer", "sid", 1, 2010000000, 500)

  dimensionName = "dim_whaley_singer"
}
