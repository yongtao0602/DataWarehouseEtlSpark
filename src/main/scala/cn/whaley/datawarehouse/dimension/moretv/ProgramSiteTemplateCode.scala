package cn.whaley.datawarehouse.dimension.moretv

import cn.whaley.datawarehouse.dimension.DimensionBase
import cn.whaley.datawarehouse.dimension.constant.SourceType._
import cn.whaley.datawarehouse.util.MysqlDB


/**
  * Created by Tony on 17/3/8.
  *
  * 电视猫账号维度表
  */
object ProgramSiteTemplateCode extends DimensionBase {
  columns.skName = "program_site_template_code_sk"
  columns.primaryKeys = List("program_site_template_id")
  columns.trackingColumns = List()
  columns.otherColumns = List("program_site_template_code","program_site_template_name", "status")

  readSourceType = jdbc

  //维度表的字段对应源数据的获取方式
  sourceColumnMap = Map(
    "program_site_template_id"->"id",
    "program_site_template_code" -> "code",
    "program_site_template_name" -> "name",
    "status"->"status"
  )

  sourceFilterWhere = "program_site_template_id is not null"
  sourceDb = MysqlDB.medusaCms("mtv_program_site_templateCode", "id", 1, 2010000000, 1)

  dimensionName = "dim_meusa_program_site_template_code"
}
